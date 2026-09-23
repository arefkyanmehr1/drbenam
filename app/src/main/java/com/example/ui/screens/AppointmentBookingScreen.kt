package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppointmentItem
import com.example.data.AvailableSlot
import com.example.data.ClinicItem
import com.example.data.DrBenamRepository
import com.example.data.MedicalService
import com.example.ui.components.DrBenamProgressDialog
import com.example.ui.theme.DrBenamPrimary
import com.example.ui.theme.DrBenamPrimaryDark
import com.example.ui.theme.DrBenamPrimarySoft
import com.example.util.PersianFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppointmentBookingScreen(
  onNavigateBack: () -> Unit,
  onBookingSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  val services by DrBenamRepository.services.collectAsState()
  val clinics by DrBenamRepository.clinics.collectAsState()
  val availableSlots by DrBenamRepository.availableSlots.collectAsState()

  var currentStep by remember { mutableIntStateOf(1) } // 1: Service, 2: Clinic, 3: DateTime, 4: Invoice, 5: Payment
  var selectedService by remember { mutableStateOf<MedicalService?>(services.firstOrNull()) }
  var selectedClinic by remember { mutableStateOf<ClinicItem?>(null) }
  var selectedSlot by remember { mutableStateOf<AvailableSlot?>(null) }
  var selectedDate by remember { mutableStateOf("") }

  LaunchedEffect(clinics) {
    if (selectedClinic == null) selectedClinic = clinics.firstOrNull()
  }
  LaunchedEffect(selectedClinic, availableSlots) {
    val first = availableSlots.firstOrNull { selectedClinic?.id == it.clinicId }
    if (selectedDate.isBlank() || availableSlots.none { it.clinicId == selectedClinic?.id && it.date == selectedDate }) {
      selectedDate = first?.date.orEmpty()
    }
    if (selectedSlot == null || selectedSlot?.let { s -> availableSlots.none { it.id == s.id } } == true ||
        selectedSlot?.clinicId != selectedClinic?.id || selectedSlot?.date != selectedDate) {
      selectedSlot = availableSlots.firstOrNull { it.clinicId == selectedClinic?.id && it.date == selectedDate }
    }
  }

  var paymentChoice by remember { mutableStateOf("full") } // "full" or "deposit"
  var confirmedAppointment by remember { mutableStateOf<AppointmentItem?>(null) }

  // Multi-second animated progressive loading
  var showLoadingDialog by remember { mutableStateOf(false) }
  var loadingTitle by remember { mutableStateOf("") }
  var loadingStatus by remember { mutableStateOf("") }
  var loadingProgress by remember { mutableFloatStateOf(0.2f) }

  Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
      // Top bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onNavigateBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = Color(0xFF0F172A))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "رزرو آنلاین نوبت ویزیت",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (confirmedAppointment != null) {
        SuccessReceiptCard(
          appointment = confirmedAppointment!!,
          onViewAppointments = onBookingSuccess,
          onBackHome = onNavigateBack
        )
      } else {
        // Stepper Header
        BookingStepper(currentStep = currentStep)

        Spacer(modifier = Modifier.height(20.dp))

        when (currentStep) {
          1 -> {
            Text("مرحله ۱: انتخاب خدمت پزشکی", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text("لطفاً نوع خدمت تخصصی مورد نیاز خود را انتخاب فرمایید.", fontSize = 12.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              services.forEach { service ->
                val isSelected = selectedService?.id == service.id
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedService = service }
                    .border(
                      width = if (isSelected) 2.dp else 1.dp,
                      color = if (isSelected) DrBenamPrimary else Color(0xFFE2E8F0),
                      shape = RoundedCornerShape(16.dp)
                    ),
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFF0FDF9) else Color.White)
                ) {
                  Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) DrBenamPrimary else Color(0xFFF1F5F9)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else DrBenamPrimaryDark,
                        modifier = Modifier.size(22.dp)
                      )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                      Text(service.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                      Text(service.shortDescription, fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 2)
                      Spacer(modifier = Modifier.height(4.dp))
                      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                          "هزینه: ${PersianFormatter.formatPrice(service.price)}",
                          fontSize = 11.sp,
                          color = DrBenamPrimaryDark,
                          fontWeight = FontWeight.Bold
                        )
                        if (service.depositPrice > 0) {
                          Text(
                            "(بیعانه: ${PersianFormatter.formatPrice(service.depositPrice)})",
                            fontSize = 11.sp,
                            color = Color(0xFFD97706)
                          )
                        }
                      }
                    }

                    RadioButton(
                      selected = isSelected,
                      onClick = { selectedService = service }
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = { currentStep = 2 },
              modifier = Modifier.fillMaxWidth().height(48.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary),
              enabled = selectedService != null
            ) {
              Text("ادامه به انتخاب مطب و کلینیک ←", fontWeight = FontWeight.Bold)
            }
          }

          2 -> {
            Text("مرحله ۲: انتخاب محل مراجعه", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text("مرکز درمانی مد نظر خود را برای ویزیت مشخص کنید.", fontSize = 12.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              clinics.forEach { clinic ->
                val isSelected = selectedClinic?.id == clinic.id
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedClinic = clinic }
                    .border(
                      width = if (isSelected) 2.dp else 1.dp,
                      color = if (isSelected) DrBenamPrimary else Color(0xFFE2E8F0),
                      shape = RoundedCornerShape(16.dp)
                    ),
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFF0FDF9) else Color.White)
                ) {
                  Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) DrBenamPrimary else Color(0xFFF1F5F9)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        Icons.Default.Business,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else DrBenamPrimaryDark,
                        modifier = Modifier.size(22.dp)
                      )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                      Text(clinic.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                      Text(clinic.address, fontSize = 11.sp, color = Color(0xFF64748B))
                      Spacer(modifier = Modifier.height(2.dp))
                      Text("تلفن: ${PersianFormatter.toPersianDigits(clinic.phone)}", fontSize = 11.sp, color = DrBenamPrimaryDark)
                    }

                    RadioButton(
                      selected = isSelected,
                      onClick = { selectedClinic = clinic }
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              OutlinedButton(
                onClick = { currentStep = 1 },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text("مرحله قبل")
              }
              Button(
                onClick = { currentStep = 3 },
                modifier = Modifier.weight(2f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary),
                enabled = selectedClinic != null
              ) {
                Text("ادامه به تقویم و ساعت ←", fontWeight = FontWeight.Bold)
              }
            }
          }

          3 -> {
            val clinicSlots = availableSlots.filter { it.clinicId == selectedClinic?.id }
            val dates = clinicSlots.map { it.date }.distinct()
            val times = clinicSlots.filter { it.date == selectedDate }

            Text("مرحله ۳: انتخاب تاریخ و ساعت ویزیت", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text("تاریخ و ساعت فقط از سانس‌های واقعی موجود در دیتابیس سایت نمایش داده می‌شوند.", fontSize = 12.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(14.dp))

            if (clinicSlots.isEmpty()) {
              Text("در حال حاضر هیچ سانس آزادی برای این مطب در دیتابیس سایت وجود ندارد.", color = Color(0xFFDC2626), fontSize = 12.sp)
            } else {
              Text("۱. تاریخ‌های دارای ظرفیت:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                dates.forEach { date ->
                  val isSelected = selectedDate == date
                  Card(
                    modifier = Modifier
                      .clickable {
                        selectedDate = date
                        selectedSlot = clinicSlots.firstOrNull { it.date == date }
                      }
                      .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) DrBenamPrimary else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(12.dp)
                      ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) DrBenamPrimarySoft else Color.White)
                  ) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = if (isSelected) DrBenamPrimaryDark else Color(0xFF64748B), modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(PersianFormatter.toPersianDigits(date), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) DrBenamPrimaryDark else Color(0xFF0F172A), fontSize = 12.sp)
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(18.dp))
              Text("۲. سانس‌های آزاد همان تاریخ:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                times.forEach { slot ->
                  val isSelected = selectedSlot?.id == slot.id
                  Card(
                    modifier = Modifier
                      .clickable { selectedSlot = slot }
                      .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) DrBenamPrimary else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(12.dp)
                      ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) DrBenamPrimarySoft else Color.White)
                  ) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.AccessTime, contentDescription = null, tint = if (isSelected) DrBenamPrimaryDark else Color(0xFF64748B), modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(PersianFormatter.formatTime(slot.startTime), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) DrBenamPrimaryDark else Color(0xFF0F172A), fontSize = 13.sp)
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              OutlinedButton(onClick = { currentStep = 2 }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) {
                Text("مرحله قبل")
              }
              Button(
                onClick = { currentStep = 4 },
                modifier = Modifier.weight(2f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary),
                enabled = selectedSlot != null
              ) {
                Text("مشاهده جزئیات ←", fontWeight = FontWeight.Bold)
              }
            }
          }

          4 -> {
            Text("مرحله ۴: پیش‌فاکتور و شیوه پرداخت", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text("جزئیات نوبت بر اساس اطلاعات واقعی دیتابیس سایت.", fontSize = 12.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(14.dp))

            Card(
              modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                InvoiceRow("پزشک معالج:", "دکتر ابراهیم بنام (متخصص قلب)")
                Spacer(modifier = Modifier.height(8.dp))
                InvoiceRow("خدمت درمانی:", selectedService?.title ?: "-")
                Spacer(modifier = Modifier.height(8.dp))
                InvoiceRow("محل مراجعه:", selectedClinic?.name ?: "-")
                Spacer(modifier = Modifier.height(8.dp))
                InvoiceRow("تاریخ و ساعت:", "${PersianFormatter.toPersianDigits(selectedSlot?.date.orEmpty())} ساعت ${PersianFormatter.formatTime(selectedSlot?.startTime.orEmpty())}")
                Spacer(modifier = Modifier.height(8.dp))
                InvoiceRow("مبلغ کل خدمت:", PersianFormatter.formatPrice(selectedService?.price ?: 0))

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text("انتخاب نوع پرداخت آنلاین:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                // Full
                Row(
                  modifier = Modifier.fillMaxWidth().clickable { paymentChoice = "full" },
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  RadioButton(selected = paymentChoice == "full", onClick = { paymentChoice = "full" })
                  Column {
                    Text("تسویه کامل آنلاین", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("مبلغ: ${PersianFormatter.formatPrice(selectedService?.price ?: 0)}", fontSize = 11.sp, color = Color(0xFF64748B))
                  }
                }

                // Deposit
                if ((selectedService?.depositPrice ?: 0) > 0) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth().clickable { paymentChoice = "deposit" },
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(selected = paymentChoice == "deposit", onClick = { paymentChoice = "deposit" })
                    Column {
                      Text("پرداخت بیعانه جهت تثبیت نوبت", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                      Text("مبلغ: ${PersianFormatter.formatPrice(selectedService?.depositPrice ?: 0)} (مابقی در مطب)", fontSize = 11.sp, color = Color(0xFFD97706))
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              OutlinedButton(
                onClick = { currentStep = 3 },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text("مرحله قبل")
              }
              Button(
                onClick = { currentStep = 5 },
                modifier = Modifier.weight(2f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary)
              ) {
                Text("انتقال به درگاه پرداخت ←", fontWeight = FontWeight.Bold)
              }
            }
          }

          5 -> {
            Text("مرحله ۵: ثبت درخواست نوبت", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

            val payableAmount = if (paymentChoice == "deposit") selectedService?.depositPrice ?: 0 else selectedService?.price ?: 0

            Spacer(modifier = Modifier.height(14.dp))

            Card(
              modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
              Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Box(
                  modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2FE)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("ثبت نوبت در سامانه مطب", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("پرداخت اینترنتی فقط پس از اتصال درگاه واقعی انجام می‌شود.", fontSize = 12.sp, color = Color(0xFF64748B))

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF9)),
                  modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFCCFBF1), RoundedCornerShape(12.dp))
                ) {
                  Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("مبلغ قابل پرداخت:", fontSize = 11.sp, color = Color(0xFF0F766E))
                    Text(PersianFormatter.formatPrice(payableAmount), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F766E))
                    Text(if (paymentChoice == "deposit") "نوع: پرداخت بیعانه جهت رزرو نوبت" else "نوع: تسویه حساب کامل نوبت", fontSize = 10.sp, color = Color(0xFF64748B))
                  }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                  onClick = {
                    scope.launch {
                      showLoadingDialog = true
                      loadingTitle = "تأیید تراکنش بانکی"
                      loadingStatus = "در حال اتصال به سرور درگاه شاپرک..."
                      loadingProgress = 0.25f
                      delay(800)

                      loadingStatus = "ثبت زمان انتخاب‌شده در دیتابیس مطب..."
                      loadingProgress = 0.80f
                      val result = DrBenamRepository.bookAppointment(
                        service = selectedService!!,
                        clinic = selectedClinic!!,
                        slot = selectedSlot!!,
                        payChoice = paymentChoice
                      )
                      showLoadingDialog = false
                      if (result.first != null) {
                        confirmedAppointment = result.first
                      }
                      if (result.second.isNotBlank() && result.first == null) {
                        currentStep = 3
                      }
                    }
                  },
                  modifier = Modifier.fillMaxWidth().height(48.dp).testTag("confirm_booking_button"),
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                  Text("ثبت درخواست نوبت", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                  onClick = { currentStep = 4 },
                  modifier = Modifier.fillMaxWidth().height(42.dp),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text("انصراف و بازگشت")
                }
              }
            }
          }
        }
      }
    }

    if (showLoadingDialog) {
      DrBenamProgressDialog(
        title = loadingTitle,
        currentStatus = loadingStatus,
        progress = loadingProgress
      )
    }
  }
}

@Composable
private fun BookingStepper(currentStep: Int) {
  val stepNames = listOf("خدمت", "مطب", "زمان", "فاکتور", "پرداخت")
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    stepNames.forEachIndexed { index, name ->
      val stepNum = index + 1
      val isActive = stepNum == currentStep
      val isDone = stepNum < currentStep

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
              when {
                isActive -> DrBenamPrimary
                isDone -> Color(0xFF10B981)
                else -> Color(0xFFE2E8F0)
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isDone) "✓" else PersianFormatter.toPersianDigits(stepNum.toString()),
            color = if (isActive || isDone) Color.White else Color(0xFF64748B),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = name,
          fontSize = 10.sp,
          fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
          color = if (isActive) DrBenamPrimaryDark else Color(0xFF64748B)
        )
      }
    }
  }
}

@Composable
private fun InvoiceRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, fontSize = 11.sp, color = Color(0xFF64748B))
    Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
  }
}

@Composable
private fun SuccessReceiptCard(
  appointment: AppointmentItem,
  onViewAppointments: () -> Unit,
  onBackHome: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, Color(0xFFCCFBF1), RoundedCornerShape(20.dp)),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(Color(0xFFE8FAF3)),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(36.dp))
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text("نوبت شما با موفقیت ثبت قطعی شد", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF065F46))
      Text("پیامک تأیید حاوی کد پیگیری به شماره موبایل شما ارسال گردید.", fontSize = 11.sp, color = Color(0xFF64748B))

      Spacer(modifier = Modifier.height(14.dp))

      Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          InvoiceRow("کد پیگیری نوبت:", PersianFormatter.toPersianDigits(appointment.trackingCode))
          InvoiceRow("پزشک معالج:", appointment.doctorName)
          InvoiceRow("خدمت:", appointment.serviceTitle)
          InvoiceRow("محل مراجعه:", appointment.clinicName)
          InvoiceRow("تاریخ نوبت:", PersianFormatter.toPersianDigits(appointment.appointmentDate))
          InvoiceRow("ساعت ویزیت:", PersianFormatter.formatTime(appointment.appointmentTime))
          InvoiceRow("مبلغ پرداختی:", PersianFormatter.formatPrice(appointment.onlinePaid))
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Button(
        onClick = onViewAppointments,
        modifier = Modifier.fillMaxWidth().height(46.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary)
      ) {
        Text("مشاهده در لیست نوبت‌های من", fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedButton(
        onClick = onBackHome,
        modifier = Modifier.fillMaxWidth().height(42.dp),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("بازگشت به پیشخوان")
      }
    }
  }
}
