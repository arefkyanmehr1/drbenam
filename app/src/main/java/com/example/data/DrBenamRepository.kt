package com.example.data

import android.content.Context
import android.util.Log
import com.example.util.PersianFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object DrBenamRepository {
  const val BASE_URL = "https://drbenam.com"
  const val API_URL = "$BASE_URL/api.php"

  private const val PREFS = "drbenam_auth"
  private const val TOKEN = "session_token"
  private const val MOBILE = "mobile"

  private var context: Context? = null
  private var sessionToken = ""
  private var mobile = ""
  private val scope = CoroutineScope(Dispatchers.IO)

  val httpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .writeTimeout(25, TimeUnit.SECONDS)
    .build()

  private val _isLoggedIn = MutableStateFlow(false)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  private val _userProfile = MutableStateFlow(UserProfile())
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  private val _services = MutableStateFlow<List<MedicalService>>(emptyList())
  val services: StateFlow<List<MedicalService>> = _services.asStateFlow()

  private val _clinics = MutableStateFlow<List<ClinicItem>>(emptyList())
  val clinics: StateFlow<List<ClinicItem>> = _clinics.asStateFlow()

  private val _availableSlots = MutableStateFlow<List<AvailableSlot>>(emptyList())
  val availableSlots: StateFlow<List<AvailableSlot>> = _availableSlots.asStateFlow()

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

  // Compatibility only. OTP is never generated, stored or auto-filled by the app.
  private val _activeOtpCode = MutableStateFlow<String?>(null)
  val activeOtpCode: StateFlow<String?> = _activeOtpCode.asStateFlow()

  fun init(ctx: Context) {
    if (context != null) return
    context = ctx.applicationContext
    val p = context!!.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    sessionToken = p.getString(TOKEN, "") ?: ""
    mobile = p.getString(MOBILE, "") ?: ""

    if (sessionToken.isNotBlank()) {
      _isLoggedIn.value = true
      scope.launch { if (!syncNow()) _isLoggedIn.value = false }
    }
  }

  fun setLoggedIn(value: Boolean) {
    if (value) return
    val oldToken = sessionToken
    sessionToken = ""
    mobile = ""
    _isLoggedIn.value = false
    _activeOtpCode.value = null
    context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.clear()?.apply()
    clearState()
    if (oldToken.isNotBlank()) scope.launch { post("logout", emptyMap(), oldToken) }
  }

  fun dismissLatestSms() = Unit
  fun dispatchSmsNotification(message: String, code: String? = null, targetDestination: String? = null) = Unit

  suspend fun sendOtpOnline(number: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    val normalized = normalizeMobile(number)
    if (!Regex("^09\\d{9}$").matches(normalized)) return@withContext false to "شماره موبایل معتبر نیست."
    val r = post("send_otp", mapOf("mobile" to normalized))
    if (!r.optBoolean("ok")) return@withContext false to r.optString("message", "ارسال کد ناموفق بود.")
    mobile = normalized
    _activeOtpCode.value = null
    context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.putString(MOBILE, mobile)?.apply()
    true to r.optString("message", "کد تأیید ارسال شد.")
  }

  suspend fun requestOtpOnline(number: String) = sendOtpOnline(number)

  suspend fun verifyOtpOnline(otp: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    if (mobile.isBlank()) return@withContext false to "شماره موبایل مشخص نیست."
    if (!Regex("^\\d{6}$").matches(otp)) return@withContext false to "کد تأیید باید ۶ رقمی باشد."

    val r = post("verify_otp", mapOf("mobile" to mobile, "otp" to otp))
    if (!r.optBoolean("ok")) return@withContext false to r.optString("message", "کد تأیید صحیح نیست.")

    sessionToken = r.optString("token")
    if (sessionToken.isBlank()) return@withContext false to "سرور نشست کاربری ایجاد نکرد."

    context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()
      ?.putString(TOKEN, sessionToken)?.putString(MOBILE, mobile)?.apply()

    _isLoggedIn.value = true
    _activeOtpCode.value = null
    if (!syncNow()) {
      setLoggedIn(false)
      return@withContext false to "ورود انجام شد اما اطلاعات حساب از دیتابیس سایت دریافت نشد."
    }
    true to r.optString("message", "ورود با موفقیت انجام شد.")
  }

  suspend fun loginPassword(number: String, password: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    val normalized = normalizeMobile(number)
    val r = post("login_password", mapOf("mobile" to normalized, "password" to password))
    if (!r.optBoolean("ok")) return@withContext false to r.optString("message", "ورود ناموفق بود.")
    sessionToken = r.optString("token")
    if (sessionToken.isBlank()) return@withContext false to "سرور نشست کاربری ایجاد نکرد."
    mobile = normalized
    context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()
      ?.putString(TOKEN, sessionToken)?.putString(MOBILE, mobile)?.apply()
    _isLoggedIn.value = true
    if (!syncNow()) return@withContext false to "ورود انجام شد اما همگام‌سازی دیتابیس ناموفق بود."
    true to r.optString("message", "ورود با موفقیت انجام شد.")
  }

  suspend fun updateProfile(firstName:String,lastName:String,nationalCode:String,birthDate:String,gender:String,email:String):Pair<Boolean,String> {
    val r=post("update_profile",mapOf("first_name" to firstName,"last_name" to lastName,"national_code" to nationalCode,"birth_date" to birthDate,"gender" to gender,"email" to email))
    if(!r.optBoolean("ok")) return false to r.optString("message","ذخیره پروفایل ناموفق بود.")
    syncNow()
    return true to r.optString("message","پروفایل ذخیره شد.")
  }

  suspend fun bookAppointment(service:MedicalService,clinic:ClinicItem,slot:AvailableSlot,payChoice:String):Pair<AppointmentItem?,String> {
    val r=post("book_appointment",mapOf("service_id" to service.id.toString(),"clinic_id" to clinic.id.toString(),"slot_id" to slot.id.toString(),"payment_choice" to payChoice))
    if(!r.optBoolean("ok")) return null to r.optString("message","ثبت نوبت ناموفق بود.")
    syncNow()
    return _appointments.value.firstOrNull { it.id==r.optInt("appointment_id") } to r.optString("message","درخواست نوبت ثبت شد.")
  }

  suspend fun cancelAppointment(id:Int,reason:String):Pair<Boolean,String>{
    val r=post("cancel_appointment",mapOf("appointment_id" to id.toString(),"cancellation_reason" to reason))
    if(!r.optBoolean("ok")) return false to r.optString("message","لغو نوبت ناموفق بود.")
    syncNow()
    return true to r.optString("message","نوبت لغو شد.")
  }

  suspend fun depositWallet(amount:Long):Pair<Boolean,String>{
    val r=post("deposit_wallet",mapOf("amount" to amount.toString()))
    return false to r.optString("message","درگاه واقعی شارژ کیف پول متصل نیست.")
  }

  suspend fun withdrawWallet(amount:Long,iban:String):Pair<Boolean,String>{
    val r=post("withdraw_wallet",mapOf("amount" to amount.toString(),"iban_or_card" to iban))
    if(!r.optBoolean("ok")) return false to r.optString("message","درخواست تسویه ناموفق بود.")
    syncNow()
    return true to r.optString("message","درخواست تسویه ثبت شد.")
  }

  fun createTicket(subject:String,message:String){ scope.launch { if(post("ticket_create",mapOf("subject" to subject,"message" to message)).optBoolean("ok")) syncNow() } }
  fun sendChatMessage(message:String){ scope.launch { if(post("chat_send",mapOf("message" to message)).optBoolean("ok")) syncNow() } }
  fun deleteNotification(id:Int){ scope.launch { if(post("notification_delete",mapOf("notification_id" to id.toString())).optBoolean("ok")) syncNow() } }
  fun markAllNotificationsRead(){ scope.launch { if(post("notifications_read_all",emptyMap()).optBoolean("ok")) syncNow() } }
  suspend fun markNotificationRead(id:Int):Boolean{ val r=post("notification_read",mapOf("notification_id" to id.toString())); if(!r.optBoolean("ok")) return false; syncNow(); return true }

  suspend fun syncNow():Boolean=withContext(Dispatchers.IO){
    if(sessionToken.isBlank()) return@withContext false
    val r=post("sync_init",emptyMap())
    if(!r.optBoolean("ok")){
      if(r.optInt("code",0)==401 || r.optString("message").contains("نشست")) _isLoggedIn.value=false
      return@withContext false
    }
    r.optJSONObject("user")?.let { _userProfile.value=parseUser(it); _walletBalance.value=it.optLong("wallet_balance",0L) }
    _services.value=parseServices(r.optJSONArray("services"))
    _clinics.value=parseClinics(r.optJSONArray("clinics"))
    _availableSlots.value=parseSlots(r.optJSONArray("available_slots"))
    _appointments.value=parseAppointments(r.optJSONArray("appointments"))
    _treatments.value=parseTreatments(r.optJSONArray("treatments"))
    _walletTransactions.value=parseWallet(r.optJSONArray("wallet_transactions"))
    _notifications.value=parseNotifications(r.optJSONArray("notifications"))
    _supportTickets.value=parseTickets(r.optJSONArray("support_tickets"))
    _chatMessages.value=parseChat(r.optJSONArray("chat_messages"))
    _isLoggedIn.value=true
    true
  }

  private suspend fun post(action:String,params:Map<String,String>,tokenOverride:String?=null):JSONObject=withContext(Dispatchers.IO){
    try{
      val form=FormBody.Builder().add("action",action).apply{params.forEach{(k,v)->add(k,v)}}.build()
      val b=Request.Builder().url(API_URL).post(form).header("Accept","application/json")
      val token=tokenOverride?:sessionToken
      if(token.isNotBlank()) b.header("Authorization","Bearer $token")
      httpClient.newCall(b.build()).execute().use{response->
        val raw=response.body?.string().orEmpty()
        if(raw.isBlank()) return@withContext JSONObject().put("ok",false).put("message","پاسخ خالی از سرور.")
        try{JSONObject(raw)}catch(_:Exception){JSONObject().put("ok",false).put("message","پاسخ نامعتبر از API.")}
      }
    }catch(e:Exception){
      Log.e("DrBenamApi","$action failed",e)
      JSONObject().put("ok",false).put("message","ارتباط با سرور برقرار نشد.")
    }
  }

  private fun parseUser(o:JSONObject)=UserProfile(o.optInt("id"),o.optString("name"),o.optString("first_name"),o.optString("last_name"),o.optString("phone"),o.optString("email"),o.optString("national_id"),o.optString("birth_date"),o.optString("gender","unknown"),o.optString("avatar").ifBlank{null},o.optString("created_at"),o.optString("last_login_at"))

  private fun parseServices(a:JSONArray?)=buildList{if(a!=null)for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;add(MedicalService(o.optInt("id"),o.optString("title"),o.optInt("duration_minutes"),o.optLong("price"),o.optLong("deposit_price"),o.optString("short_description").ifBlank{o.optString("description")}))}}

  private fun parseClinics(a:JSONArray?)=buildList{if(a!=null)for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;add(ClinicItem(o.optInt("id"),o.optString("name"),o.optString("address"),o.optString("phone")))}}

  private fun parseSlots(a:JSONArray?)=buildList{if(a!=null)for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;add(AvailableSlot(o.optInt("id"),o.optInt("schedule_id"),o.optInt("clinic_id"),o.optInt("doctor_id"),o.optString("slot_date"),o.optString("start_time").take(5),o.optString("end_time").take(5)))}}

  private fun parseAppointments(a:JSONArray?)=buildList{if(a!=null)for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;add(AppointmentItem(o.optInt("id"),o.optString("tracking_code"),o.optString("doctor_name"),o.optString("doctor_specialty"),o.optString("clinic_name"),o.optString("clinic_address"),o.optString("clinic_phone"),o.optString("service_title"),o.optLong("service_price"),0L,if(o.optString("status")=="pending")"در انتظار تأیید" else "ثبت در سامانه مطب",o.optString("appointment_date"),o.optString("appointment_time").take(5),o.optString("status"),o.optString("patient_notes"),o.optString("admin_notes").ifBlank{null}))}}

  private fun parseTreatments(a:JSONArray?)=buildList{if(a!=null)for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;val files=buildList{val aa=o.optJSONArray("attachments");if(aa!=null)for(j in 0 until aa.length()){val f=aa.optJSONObject(j)?:continue;val u=f.optString("url");add(AttachmentFile(f.optString("name"),if(u.startsWith("http"))u else "$BASE_URL$u",f.optLong("size")))}};add(TreatmentRecord(o.optInt("id"),o.optString("title"),o.optString("treatment_date"),PersianFormatter.toPersianDigits(o.optString("treatment_date")),o.optString("service_title"),o.optString("clinic_name"),o.optString("doctor_name"),o.optInt("sessions"),o.optString("body"),files))}}

  private fun parseWallet(a:JSONArray?)=buildList{if(a!=null)for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;val t=o.optString("type");add(WalletTx(o.optInt("id"),o.optLong("amount"),t,o.optString("status"),o.optString("tracking_code"),o.optString("description"),o.optString("card_or_iban").ifBlank{null},o.optString("created_at"),t=="deposit"||t=="refund"))}}

  private fun parseNotifications(a:JSONArray?)=buildList{if(a!=null)for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;add(NotificationMessage(o.optInt("id"),o.optString("type"),o.optString("title"),o.optString("message"),o.optInt("is_read")==1||o.optBoolean("is_read"),o.optString("created_at"),o.optString("created_at"),when(o.optString("type")){"wallet"->"wallet";"appointment"->"appointment";else->"system"}))}}

  private fun parseTickets(a:JSONArray?)=buildList{if(a!=null)for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;add(SupportTicketItem(o.optInt("id"),o.optString("subject"),o.optString("message"),o.optString("status"),o.optString("updated_at")))}}

  private fun parseChat(a:JSONArray?)=buildList{if(a!=null)for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;add(SupportChatMessageItem(o.optInt("id"),o.optString("sender_type"),o.optString("message"),o.optString("created_at")))}}

  private fun normalizeMobile(value:String):String{val d=value.filter{it.isDigit()};return when{d.startsWith("98")&&d.length==12->"0"+d.drop(2);d.length==10&&d.startsWith("9")->"0$d";else->d}}

  private fun clearState(){
    _userProfile.value=UserProfile()
    _services.value=emptyList();_clinics.value=emptyList();_availableSlots.value=emptyList()
    _appointments.value=emptyList();_treatments.value=emptyList();_walletTransactions.value=emptyList()
    _walletBalance.value=0L;_notifications.value=emptyList();_supportTickets.value=emptyList();_chatMessages.value=emptyList()
  }
}
