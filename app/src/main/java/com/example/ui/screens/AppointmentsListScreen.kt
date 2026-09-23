package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppointmentItem
import com.example.data.DrBenamRepository
import com.example.ui.components.DrBenamProgressDialog
import com.example.ui.components.DrBenamResultDialog
import com.example.ui.theme.DrBenamDanger
import com.example.ui.theme.DrBenamDangerSoft
import com.example.ui.theme.DrBenamPrimary
import com.example.ui.theme.DrBenamPrimaryDark
import com.example.ui.theme.DrBenamPrimarySoft
import com.example.util.PersianFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppointmentsListScreen(
  onBookNewAppointment: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  val appointments by DrBenamRepository.appointments.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabTitles = listOf("همه نوبت‌ها", "پیش‌رو (فعال)", "انجام شده", "لغو شده")

  var appointmentToCancel by remember { mutableStateOf<AppointmentItem?>(null) }
  var cancelReason by remember { mutableStateOf("تغییر برنامه کاری و عدم امکان حضور") }
  var customCancelReason by remember { mutableStateOf("") }

  // Multi-second progressive loading dialog
  var showLoadingDialog by remember { mutableStateOf(false) }
  var loadingTitle by remember { mutableStateOf("") }
  var loadingStatus by remember { mutableStateOf("") }
  var loadingProgress by remember { mutableFloatStateOf(0.2f) }

  // Result dialog
  var resultDialogState by remember { mutableStateOf<Triple<Boolean, String, String>?>(null) }

  val filteredAppointments = remember(appointments, selectedTab) {
    when (selectedTab) {
      1 -> appointments.filter { it.status == "confirmed" || it.status == "pending" }
      2 -> appointments.filter { it.status == "completed" }
      3 -> appointments.filter { it.status == "cancelled" }
      else -> appointments
    }
  }

  Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "نوبت‌های من",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
          Text(
            text = "پیگیری، مدیریت و استرداد آنلاین نوبت‌ها",
            fontSize = 11.sp,
            color = Color(0xFF64748B)
          )
        }

        Button(
          onClick = onBookNewAppointment,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary)
        ) {
          Text("رزرو نوبت جدید +", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Tabs
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 0.dp,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
      ) {
        tabTitles.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (filteredAppointments.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF1F5F9)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(32.dp)
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("نوبتی در این دسته یافت نشد", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(4.dp))
            Text("جهت رزرو نوبت جدید، روی دکمه رزرو نوبت کلیک کنید.", fontSize = 11.sp, color = Color(0xFF64748B))
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxWidth().weight(1f),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(filteredAppointments, key = { it.id }) { item ->
            AppointmentTicketCard(
              appointment = item,
              onCancelClick = { appointmentToCancel = item }
            )
          }
        }
      }
    }

    // Cancellation Dialog
    if (appointmentToCancel != null) {
      val item = appointmentToCancel!!
      AlertDialog(
        onDismissRequest = { appointmentToCancel = null },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Cancel, contentDescription = null, tint = DrBenamDanger)
            Spacer(modifier = Modifier.width(8.dp))
            Text("لغو نوبت و استرداد وجه", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "آیا از لغو نوبت ${item.serviceTitle} برای تاریخ ${PersianFormatter.toPersianDigits(item.appointmentDate)} اطمینان دارید؟",
              fontSize = 12.sp,
              lineHeight = 18.sp,
              color = Color(0xFF0F172A)
            )

            Card(
              colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFECACA), RoundedCornerShape(10.dp))
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  "قوانین استرداد مطب دکتر ابراهیم بنام:",
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = Color(0xFF991B1B)
                )
                Text(
                  "در صورت لغو، مبلغ بیعانه به کیف پول حساب شما واریز و پیامک تأیید لغو ارسال می‌شود.",
                  fontSize = 10.sp,
                  color = Color(0xFF991B1B),
                  lineHeight = 15.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("علت انصراف:", fontWeight = FontWeight.Bold, fontSize = 11.sp)

            val reasons = listOf(
              "تغییر برنامه کاری و عدم امکان حضور",
              "بهبود وضعیت سلامت و رفع علائم",
              "اشتباه در انتخاب زمان نوبت",
              "سایر دلایل"
            )

            reasons.forEach { r ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(selected = cancelReason == r, onClick = { cancelReason = r })
                Text(r, fontSize = 11.sp)
              }
            }

            if (cancelReason == "سایر دلایل") {
              OutlinedTextField(
                value = customCancelReason,
                onValueChange = { customCancelReason = it },
                label = { Text("توضیحات تکمیلی") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
              )
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val finalReason = if (cancelReason == "سایر دلایل") customCancelReason else cancelReason
              val apptId = item.id
              val tracking = item.trackingCode
              appointmentToCancel = null

              scope.launch {
                showLoadingDialog = true
                loadingTitle = "لغو نوبت و استرداد وجه"
                loadingStatus = "در حال محاسبه درصد استرداد در دیتابیس مطب..."
                loadingProgress = 0.35f
                delay(800)

                loadingStatus = "واریز وجه بیعانه به کیف پول کاربری شما..."
                loadingProgress = 0.70f
                delay(800)

                loadingStatus = "ارسال پیامک لغو به شماره بیمار..."
                loadingProgress = 0.95f
                DrBenamRepository.cancelAppointment(apptId, finalReason)
                delay(600)
                showLoadingDialog = false

                resultDialogState = Triple(
                  true,
                  "نوبت با موفقیت لغو شد",
                  "نوبت با کد پیگیری ${PersianFormatter.toPersianDigits(tracking)} لغو گردید. مبلغ بیعانه به کیف پول حساب شما منظور شد و پیامک تأیید برایتان ارسال گردید."
                )
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DrBenamDanger),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("تأیید لغو و ارسال پیامک", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { appointmentToCancel = null }, shape = RoundedCornerShape(8.dp)) {
            Text("انصراف", fontSize = 11.sp)
          }
        }
      )
    }

    if (showLoadingDialog) {
      DrBenamProgressDialog(
        title = loadingTitle,
        currentStatus = loadingStatus,
        progress = loadingProgress
      )
    }

    if (resultDialogState != null) {
      val (isSuccess, title, msg) = resultDialogState!!
      DrBenamResultDialog(
        isSuccess = isSuccess,
        title = title,
        message = msg,
        onConfirm = { resultDialogState = null }
      )
    }
  }
}

@Composable
private fun AppointmentTicketCard(
  appointment: AppointmentItem,
  onCancelClick: () -> Unit
) {
  val isConfirmed = appointment.status == "confirmed"
  val isCompleted = appointment.status == "completed"
  val isCancelled = appointment.status == "cancelled"

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column {
      // Ticket Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFFF0FDF9))
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AccessTime, contentDescription = null, tint = DrBenamPrimaryDark, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${PersianFormatter.toPersianDigits(appointment.appointmentDate)} • ساعت ${PersianFormatter.formatTime(appointment.appointmentTime)}",
            fontWeight = FontWeight.Bold,
            color = DrBenamPrimaryDark,
            fontSize = 12.sp
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
              when {
                isConfirmed -> Color(0xFFE8FAF3)
                isCompleted -> Color(0xFFF1F5F9)
                isCancelled -> Color(0xFFFFF0F2)
                else -> Color(0xFFFFF7E8)
              }
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = when {
              isConfirmed -> "تأیید شده (قطعی)"
              isCompleted -> "انجام شده"
              isCancelled -> "لغو شده"
              else -> "در انتظار بررسی"
            },
            color = when {
              isConfirmed -> Color(0xFF065F46)
              isCompleted -> Color(0xFF475569)
              isCancelled -> Color(0xFF991B1B)
              else -> Color(0xFFB45309)
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Ticket Body
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(appointment.serviceTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
          Text("کد پیگیری: ${PersianFormatter.toPersianDigits(appointment.trackingCode)}", fontSize = 11.sp, color = Color(0xFF64748B))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Person, contentDescription = null, tint = DrBenamPrimary, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("پزشک معالج: ${appointment.doctorName}", fontSize = 11.sp, color = Color(0xFF334155))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Business, contentDescription = null, tint = DrBenamPrimary, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("محل مراجعه: ${appointment.clinicName}", fontSize = 11.sp, color = Color(0xFF334155))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CreditCard, contentDescription = null, tint = DrBenamPrimary, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("پرداخت آنلاین: ${PersianFormatter.formatPrice(appointment.onlinePaid)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
        }

        // Cancellation action
        if (isConfirmed || appointment.status == "pending") {
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            Button(
              onClick = onCancelClick,
              colors = ButtonDefaults.buttonColors(containerColor = DrBenamDangerSoft, contentColor = DrBenamDanger),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(34.dp)
            ) {
              Text("لغو نوبت و استرداد وجه", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
