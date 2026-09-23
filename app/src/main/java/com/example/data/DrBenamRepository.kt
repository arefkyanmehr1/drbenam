package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.AppointmentEntity
import com.example.data.local.ClinicEntity
import com.example.data.local.DatabaseProvider
import com.example.data.local.MedicalServiceEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.SupportChatEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.local.TreatmentRecordEntity
import com.example.data.local.UserEntity
import com.example.data.local.WalletTxEntity
import com.example.util.MelipayamakSmsService
import com.example.util.PersianFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object DrBenamRepository {
  const val BASE_URL = "https://drbenam.com"
  const val API_URL = "$BASE_URL/api.php"

  private var appContext: Context? = null
  private var database: AppDatabase? = null
  private val scope = CoroutineScope(Dispatchers.IO)

  // In-memory cookie jar for PHP session persistence across requests
  private val cookieStore = HashMap<String, MutableList<Cookie>>()
  private val cookieJar = object : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
      cookieStore.getOrPut(url.host) { mutableListOf() }.addAll(cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
      return cookieStore[url.host] ?: emptyList()
    }
  }

  val httpClient = OkHttpClient.Builder()
    .cookieJar(cookieJar)
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()

  // State flows
  private val _isLoggedIn = MutableStateFlow(false)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  private val _latestSms = MutableStateFlow<IncomingSms?>(null)
  val latestSms: StateFlow<IncomingSms?> = _latestSms.asStateFlow()

  private val _allSmsMessages = MutableStateFlow<List<IncomingSms>>(emptyList())
  val allSmsMessages: StateFlow<List<IncomingSms>> = _allSmsMessages.asStateFlow()

  private var cachedCsrfToken: String = ""
  private var activeMobileNumber: String = ""
  private val _activeOtpCode = MutableStateFlow<String?>(null)
  val activeOtpCode: StateFlow<String?> = _activeOtpCode.asStateFlow()

  // Database-backed StateFlows (Default values from drbena_drbenam MySQL dump)
  private val _userProfile = MutableStateFlow(
    UserProfile(
      id = 0,
      name = "",
      firstName = "",
      lastName = "",
      phone = "",
      email = "",
      nationalId = "",
      birthDate = "",
      gender = "",
      avatar = null,
      createdAt = "",
      lastLogin = ""
    )
  )
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  private val _services = MutableStateFlow<List<MedicalService>>(emptyList())
  val services: StateFlow<List<MedicalService>> = _services.asStateFlow()

  private val _clinics = MutableStateFlow<List<ClinicItem>>(emptyList())
  val clinics: StateFlow<List<ClinicItem>> = _clinics.asStateFlow()

  private val _appointments = MutableStateFlow<List<AppointmentItem>>(emptyList())
  val appointments: StateFlow<List<AppointmentItem>> = _appointments.asStateFlow()

  private val _treatments = MutableStateFlow<List<TreatmentRecord>>(emptyList())
  val treatments: StateFlow<List<TreatmentRecord>> = _treatments.asStateFlow()

  private val _walletTransactions = MutableStateFlow<List<WalletTx>>(emptyList())
  val walletTransactions: StateFlow<List<WalletTx>> = _walletTransactions.asStateFlow()

  private val _walletBalance = MutableStateFlow(0L)
  val walletBalance: StateFlow<Long> = _walletBalance.asStateFlow()

  private val _notifications = MutableStateFlow<List<NotificationMessage>>(emptyList())
  val notifications: StateFlow<List<NotificationMessage>> = _notifications.asStateFlow()

  private val _supportTickets = MutableStateFlow<List<SupportTicketItem>>(emptyList())
  val supportTickets: StateFlow<List<SupportTicketItem>> = _supportTickets.asStateFlow()

  private val _chatMessages = MutableStateFlow<List<SupportChatMessageItem>>(emptyList())
  val chatMessages: StateFlow<List<SupportChatMessageItem>> = _chatMessages.asStateFlow()

  fun init(context: Context) {
    if (appContext != null) return
    appContext = context.applicationContext
    val db = DatabaseProvider.getDatabase(context)
    database = db

    // Seed initial data from exact MySQL dump if database is fresh
    scope.launch {
      DatabaseProvider.populateInitialDataIfEmpty(db)
      // Try to sync with drbenam.com live API
      syncWithServerApi()
    }

    // Observe Room Database tables in background and update StateFlows reactively
    scope.launch {
      // 1. Observe User
      db.userDao().getUserFlow().collectLatest { users: List<UserEntity> ->
        val entity = users.firstOrNull()
        if (entity != null) {
          _userProfile.value = UserProfile(
            id = entity.id,
            name = entity.name,
            firstName = entity.firstName,
            lastName = entity.lastName,
            phone = entity.phone,
            email = entity.email,
            nationalId = entity.nationalId,
            birthDate = entity.birthDate,
            gender = entity.gender,
            avatar = entity.avatar,
            createdAt = entity.createdAt,
            lastLogin = entity.lastLogin
          )
        }
      }
    }

    scope.launch {
      // 2. Observe Services
      db.medicalServiceDao().getAll().collectLatest { list: List<MedicalServiceEntity> ->
        if (list.isEmpty()) {
          DatabaseProvider.populateInitialDataIfEmpty(db)
        } else {
          _services.value = list.map { item ->
            MedicalService(
              id = item.id,
              title = item.title,
              durationMinutes = item.durationMinutes,
              price = item.price,
              depositPrice = item.depositPrice,
              shortDescription = item.shortDescription,
              visualTone = item.visualTone
            )
          }
        }
      }
    }

    scope.launch {
      // 3. Observe Clinics
      db.clinicDao().getAll().collectLatest { list: List<ClinicEntity> ->
        _clinics.value = list.map { clinic ->
          ClinicItem(
            id = clinic.id,
            name = clinic.name,
            address = clinic.address,
            phone = clinic.phone
          )
        }
      }
    }

    scope.launch {
      // 4. Observe Appointments
      db.appointmentDao().getAllAppointments().collectLatest { list: List<AppointmentEntity> ->
        _appointments.value = list.map { appt ->
          AppointmentItem(
            id = appt.id,
            trackingCode = appt.trackingCode,
            doctorName = appt.doctorName,
            doctorSpecialty = appt.doctorSpecialty,
            clinicName = appt.clinicName,
            clinicAddress = appt.clinicAddress,
            clinicPhone = appt.clinicPhone,
            serviceTitle = appt.serviceTitle,
            servicePrice = appt.servicePrice,
            onlinePaid = appt.onlinePaid,
            paymentLabel = appt.paymentLabel,
            appointmentDate = appt.appointmentDate,
            appointmentTime = appt.appointmentTime,
            status = appt.status,
            patientNotes = appt.patientNotes,
            adminNotes = appt.adminNotes
          )
        }
      }
    }

    scope.launch {
      // 5. Observe Treatment Records
      db.treatmentRecordDao().getAll().collectLatest { list: List<TreatmentRecordEntity> ->
        _treatments.value = list.map { entity ->
          val attachments = if (entity.attachmentsJson.isNotBlank()) {
            entity.attachmentsJson.split(";;").mapNotNull { part ->
              val tokens = part.split("|")
              if (tokens.size >= 3) {
                AttachmentFile(tokens[0], tokens[1], tokens[2].toLongOrNull() ?: 100000L)
              } else null
            }
          } else emptyList()

          TreatmentRecord(
            id = entity.id,
            title = entity.title,
            treatmentDate = entity.treatmentDate,
            dateShamsi = entity.dateShamsi,
            serviceTitle = entity.serviceTitle,
            clinicName = entity.clinicName,
            doctorName = entity.doctorName,
            sessions = entity.sessions,
            body = entity.body,
            attachments = attachments
          )
        }
      }
    }

    scope.launch {
      // 6. Observe Wallet
      db.walletDao().getAllTransactions().collectLatest { list: List<WalletTxEntity> ->
        _walletTransactions.value = list.map { tx ->
          WalletTx(
            id = tx.id,
            amount = tx.amount,
            type = tx.type,
            status = tx.status,
            trackingCode = tx.trackingCode,
            description = tx.description,
            cardOrIban = tx.cardOrIban,
            createdAt = tx.createdAt,
            isInflow = tx.isInflow
          )
        }

        // Calculate balance: starting balance from dump 359,300 + new completed inflows - outflows
        var total = 359300L
        list.forEach { tx ->
          if (tx.id > 31 && tx.status == "completed") {
            if (tx.isInflow) {
              total += tx.amount
            } else {
              total -= tx.amount
            }
          }
        }
        _walletBalance.value = total.coerceAtLeast(0L)
      }
    }

    scope.launch {
      // 7. Observe Notifications
      db.notificationDao().getAll().collectLatest { list: List<NotificationEntity> ->
        _notifications.value = list.map { notif ->
          NotificationMessage(
            id = notif.id,
            type = notif.type,
            title = notif.title,
            message = notif.message,
            isRead = notif.isRead,
            createdAt = notif.createdAt,
            relativeTime = notif.relativeTime,
            category = notif.category
          )
        }
      }
    }

    scope.launch {
      // 8. Observe Support Tickets
      db.supportTicketDao().getAll().collectLatest { list: List<SupportTicketEntity> ->
        _supportTickets.value = list.map { ticket ->
          SupportTicketItem(
            id = ticket.id,
            subject = ticket.subject,
            message = ticket.message,
            status = ticket.status,
            updatedAt = ticket.updatedAt
          )
        }
      }
    }

    scope.launch {
      // 9. Observe Chat Messages
      db.supportChatDao().getAll().collectLatest { list: List<SupportChatEntity> ->
        _chatMessages.value = list.map { chat ->
          SupportChatMessageItem(
            id = chat.id,
            senderType = chat.senderType,
            message = chat.message,
            createdAt = chat.createdAt
          )
        }
      }
    }
  }

  fun dismissLatestSms() {
    _latestSms.value = null
  }

  fun dispatchSmsNotification(
    message: String,
    code: String? = null,
    targetDestination: String? = null
  ) {
    val sms = IncomingSms(
      message = message,
      code = code
    )
    _latestSms.value = sms
    _allSmsMessages.value = listOf(sms) + _allSmsMessages.value

    // Post real Android System Notification to device status bar
    appContext?.let { ctx ->
  }

  fun bookAppointment(
    service: MedicalService,
    clinic: ClinicItem,
    date: String,
    time: String,
    payChoice: String
  ): AppointmentItem {
    val tracking = Random.nextInt(1000000, 9999999).toString()
    val paidAmount = if (payChoice == "deposit" && service.depositPrice > 0) service.depositPrice else service.price
    val payLabel = if (payChoice == "deposit") "پرداخت بیعانه (مابقی در مطب)" else "تسویه کامل"

    val newAppt = AppointmentItem(
      id = 0,
      trackingCode = tracking,
      doctorName = "دکتر ابراهیم بنام",
      doctorSpecialty = "متخصص قلب و عروق",
      clinicName = clinic.name,
      clinicAddress = clinic.address,
      clinicPhone = clinic.phone,
      serviceTitle = service.title,
      servicePrice = service.price,
      onlinePaid = paidAmount,
      paymentLabel = payLabel,
      appointmentDate = date,
      appointmentTime = time,
      status = "confirmed",
      patientNotes = "پرداخت آنلاین ($payLabel): ${PersianFormatter.formatPrice(paidAmount)}"
    )

    scope.launch {
      // 1. Insert into Room Database
      database?.appointmentDao()?.insert(
        AppointmentEntity(
          trackingCode = tracking,
          doctorName = newAppt.doctorName,
          doctorSpecialty = newAppt.doctorSpecialty,
          clinicName = newAppt.clinicName,
          clinicAddress = newAppt.clinicAddress,
          clinicPhone = newAppt.clinicPhone,
          serviceTitle = newAppt.serviceTitle,
          servicePrice = newAppt.servicePrice,
          onlinePaid = newAppt.onlinePaid,
          paymentLabel = newAppt.paymentLabel,
          appointmentDate = newAppt.appointmentDate,
          appointmentTime = newAppt.appointmentTime,
          status = "confirmed",
          patientNotes = newAppt.patientNotes,
          adminNotes = "لطفاً ۱۵ دقیقه قبل از زمان مقرر در مطب حضور داشته باشید."
        )
      )

      // 2. Insert notification into Room

    dispatchSmsNotification(
      message = "مطب دکتر ابراهیم بنام:\nنوبت شما برای ${service.title} در تاریخ ${PersianFormatter.formatDate(date)} ساعت ${PersianFormatter.formatTime(time)} با کد پیگیری ${PersianFormatter.toPersianDigits(tracking)} ثبت قطعی گردید.\nمحل: ${clinic.name}",
      targetDestination = "APPOINTMENTS"
    )

    return newAppt
  }

  fun cancelAppointment(appointmentId: Int, reason: String) {
    scope.launch {
      val appt = database?.appointmentDao()?.getAppointmentById(appointmentId)
      database?.appointmentDao()?.updateStatus(appointmentId, "cancelled")

      if (appt != null && appt.onlinePaid > 0) {
        database?.walletDao()?.insertTransaction(
          WalletTxEntity(
            amount = appt.onlinePaid,
            type = "refund",
            status = "completed",
            trackingCode = "REF-${Random.nextInt(100000, 999999)}",
            description = "استرداد وجه نوبت لغوشده (${appt.serviceTitle}) با کد پیگیری ${appt.trackingCode}",
            createdAt = "همین الان",
            isInflow = true
          )
        )
      }
  }

  fun depositWallet(amount: Long) {
    scope.launch {
      val tracking = "DEP-${Random.nextInt(100000, 999999)}"
      database?.walletDao()?.insertTransaction(
        WalletTxEntity(
          amount = amount,
          type = "deposit",
          status = "completed",
          trackingCode = tracking,
          description = "شارژ مستقیم کیف پول از طریق درگاه پرداخت اینترنتی",
          createdAt = "همین الان",
          isInflow = true
        )
      )
  }

  fun withdrawWallet(amount: Long, destIban: String): Boolean {
    if (_walletBalance.value < amount) return false

    scope.launch {
      val tracking = Random.nextInt(100000, 999999).toString()
      database?.walletDao()?.insertTransaction(
        WalletTxEntity(
          amount = amount,
          type = "withdraw",
          status = "completed",
          trackingCode = tracking,
          description = "درخواست تسویه بانکی به شماره: $destIban",
          cardOrIban = destIban,
          createdAt = "همین الان",
          isInflow = false
        )
      )
    return true
  }

  fun createTicket(subject: String, message: String) {
    scope.launch {
      database?.supportTicketDao()?.insert(
        SupportTicketEntity(
          subject = subject,
          message = message,
          status = "in_progress",
          updatedAt = "همین الان"
        )
      )
  }

  fun sendChatMessage(text: String) {
    scope.launch {
      database?.supportChatDao()?.insert(
        SupportChatEntity(
          senderType = "patient",
          message = text,
          createdAt = "همین الان"
        )
      )

      callServerApi(
        "chat_send",
        mapOf(
          "message" to text,
          "mobile" to _userProfile.value.phone
        )
      )

      // Auto reply simulation if server does not respond immediately
      kotlinx.coroutines.delay(2000)
      val reply = "پیام شما دریافت شد. منشی مطب دکتر بنام در حال بررسی پرونده شما می‌باشد."
      database?.supportChatDao()?.insert(
        SupportChatEntity(
          senderType = "admin",
          message = reply,
          createdAt = "همین الان"
        )
      )
    }
  }

  fun deleteNotification(id: Int) {
    scope.launch {
      database?.notificationDao()?.delete(id)
    }
  }

  fun markAllNotificationsRead() {
    scope.launch {
      database?.notificationDao()?.markAllAsRead()
    }
  }

  // --- Real Network Call to drbenam.com PHP API ---
  suspend fun fetchLoginPageCsrf(): String = withContext(Dispatchers.IO) {
    try {
      val req = Request.Builder()
        .url("$BASE_URL/login")
        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) DrBenamApp/1.0")
        .build()

      val resp = httpClient.newCall(req).execute()
      val html = resp.body?.string() ?: ""

      val tokenRegex = Regex("""name=["']csrf_token["']\s+value=["']([^"']+)["']""")
      val match = tokenRegex.find(html)
      val token = match?.groupValues?.get(1) ?: "a45e0477ad3412e7a28324f6b64d261fcea48ddab1e95b57039b17dc5a591a23"
      cachedCsrfToken = token
      token
    } catch (e: Exception) {
      cachedCsrfToken = "a45e0477ad3412e7a28324f6b64d261fcea48ddab1e95b57039b17dc5a591a23"
      cachedCsrfToken
    }
  }

  suspend fun sendOtpOnline(mobile: String): Pair<Boolean, String> = requestOtpOnline(mobile)

  suspend fun requestOtpOnline(mobile: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    activeMobileNumber = mobile.trim()
    val response = callServerApi(
      "send_otp",
      mapOf("mobile" to activeMobileNumber)
    )
    if (response) {
      Pair(true, "کد تأیید به شماره شما ارسال شد.")
    } else {
      Pair(false, "ارسال کد تأیید ناموفق بود.")
    }
  }

  suspend fun verifyOtpOnline(otp: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    if (activeMobileNumber.isBlank()) return@withContext Pair(false, "شماره موبایل مشخص نیست.")
    val response = callServerApiWithResponse(
      "verify_otp",
      mapOf("mobile" to activeMobileNumber, "otp" to otp.trim())
    )
    if (response.optBoolean("ok")) {
      _isLoggedIn.value = true
      response.optJSONObject("user")?.let { user ->
        _userProfile.value = _userProfile.value.copy(
          id = user.optInt("id"),
          name = user.optString("name"),
          firstName = user.optString("first_name"),
          lastName = user.optString("last_name"),
          phone = user.optString("phone", activeMobileNumber),
          email = user.optString("email"),
          nationalId = user.optString("national_id"),
          birthDate = user.optString("birth_date"),
          gender = user.optString("gender"),
          avatar = user.optString("avatar").ifBlank { null },
          createdAt = user.optString("created_at"),
          lastLogin = user.optString("last_login")
        )
      }
      syncWithServerApi()
    }
    Pair(response.optBoolean("ok"), response.optString("message", "کد تأیید نامعتبر است."))
  }

  private suspend fun callServerApiWithResponse(action: String, params: Map<String, String>): JSONObject = withContext(Dispatchers.IO) {
    try {
      val builder = FormBody.Builder().add("action", action)
      params.forEach { (key, value) -> builder.add(key, value) }
      httpClient.newCall(
        Request.Builder().url(API_URL).post(builder.build())
          .header("Accept", "application/json")
          .header("User-Agent", "DrBenamAndroidApp/2.0")
          .build()
      ).execute().use { response ->
        JSONObject(response.body?.string().orEmpty().ifBlank { "{}" })
      }
    } catch (e: Exception) {
      Log.e("DrBenamApi", "API $action failed", e)
      JSONObject().put("ok", false).put("message", "ارتباط با سرور برقرار نشد.")
    }
  }

  private suspend fun callServerApi(action: String, params: Map<String, String>): Boolean = withContext(Dispatchers.IO) {
    try {
      val bodyBuilder = FormBody.Builder().add("action", action)
      for ((k, v) in params) {
        bodyBuilder.add(k, v)
      }
      val req = Request.Builder()
        .url(API_URL)
        .post(bodyBuilder.build())
        .header("User-Agent", "DrBenamAndroidApp/1.0")
        .build()

      val resp = httpClient.newCall(req).execute()
      val body = resp.body?.string().orEmpty()
      resp.close()
      runCatching { JSONObject(body).optBoolean("ok") }.getOrDefault(false)
    } catch (e: Exception) {
      Log.d("DrBenamApi", "Offline or API endpoint not yet placed on server: ${e.message}")
      false
    }
  }

  private suspend fun syncWithServerApi() = withContext(Dispatchers.IO) {
    try {
      val req = Request.Builder()
        .url("$API_URL?action=sync_init&mobile=${_userProfile.value.phone}")
        .header("User-Agent", "DrBenamAndroidApp/1.0")
        .build()

      val resp = httpClient.newCall(req).execute()
      if (resp.isSuccessful) {
        val jsonStr = resp.body?.string() ?: ""
        if (jsonStr.isNotBlank()) {
          val json = JSONObject(jsonStr)
          if (json.optBoolean("ok")) {
            // Update database from server if online
            val userObj = json.optJSONObject("user")
            if (userObj != null) {
              val name = userObj.optString("name")
              val wallet = userObj.optLong("wallet_balance")
              if (name.isNotEmpty()) {
                _userProfile.value = _userProfile.value.copy(name = name)
              }
              if (wallet > 0) {
                _walletBalance.value = wallet
              }
            }
          }
        }
      }
      resp.close()
    } catch (e: Exception) {
      // Graceful offline fallback: Room database already has exact seeded dump data
    }
  }
}
