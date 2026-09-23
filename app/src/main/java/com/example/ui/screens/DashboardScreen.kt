package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DrBenamRepository
import com.example.ui.theme.DrBenamPrimary
import com.example.ui.theme.DrBenamPrimaryDark
import com.example.ui.theme.DrBenamPrimarySoft
import com.example.util.PersianFormatter

@Composable
fun DashboardScreen(
  onNavigateToBooking: () -> Unit,
  onNavigateToAppointments: () -> Unit,
  onNavigateToTreatments: () -> Unit,
  onNavigateToWallet: () -> Unit,
  onNavigateToProfile: () -> Unit,
  onNavigateToNotifications: () -> Unit,
  modifier: Modifier = Modifier
) {
  val user by DrBenamRepository.userProfile.collectAsState()
  val appointments by DrBenamRepository.appointments.collectAsState()
  val treatments by DrBenamRepository.treatments.collectAsState()
  val notifications by DrBenamRepository.notifications.collectAsState()

  val activeAppointment = appointments.firstOrNull { it.status == "confirmed" || it.status == "pending" }
  val upcomingCount = appointments.count { it.status == "confirmed" || it.status == "pending" }
  val completedCount = appointments.count { it.status == "completed" }
  val unreadNotifs = notifications.count { !it.isRead }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFC))
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Welcome Hero Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Row(
        modifier = Modifier.padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(DrBenamPrimary),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = user.firstName.take(1).ifEmpty { "ک" },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "سلام، ${user.name} عزیز 👋",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "مطب دکتر ابراهیم بنام؛ سامانه مدیریت نوبت و پیگیری مراجعات",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B)
          )
        }
      }
    }

    // 4 Stats Cards
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      StatCard(
        title = "نوبت پیش‌رو",
        count = PersianFormatter.toPersianDigits(upcomingCount.toString()),
        icon = Icons.Default.EventNote,
        iconTint = DrBenamPrimary,
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "نوبت انجام‌شده",
        count = PersianFormatter.toPersianDigits(completedCount.toString()),
        icon = Icons.Default.CheckCircle,
        iconTint = Color(0xFF10B981),
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "سوابق مراجعه",
        count = PersianFormatter.toPersianDigits(treatments.size.toString()),
        icon = Icons.Default.Description,
        iconTint = Color(0xFF8B5CF6),
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "اعلان جدید",
        count = PersianFormatter.toPersianDigits(unreadNotifs.toString()),
        icon = Icons.Default.Notifications,
        iconTint = Color(0xFFF59E0B),
        modifier = Modifier.weight(1f)
      )
    }

    // Active Appointment Ticket Banner
    if (activeAppointment != null) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color(0xFF134E48), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3E38)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text("نوبت فعال بعدی", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Text(
              text = "کد پیگیری: ${PersianFormatter.toPersianDigits(activeAppointment.trackingCode)}",
              color = Color(0xFF5EEAD4),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = activeAppointment.serviceTitle,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "پزشک: ${activeAppointment.doctorName} • ${activeAppointment.clinicName}",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF5EEAD4), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${PersianFormatter.toPersianDigits(activeAppointment.appointmentDate)} • ساعت ${PersianFormatter.formatTime(activeAppointment.appointmentTime)}",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = onNavigateToAppointments,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary),
            modifier = Modifier.fillMaxWidth().height(42.dp)
          ) {
            Text("مشاهده جزئیات و لغو نوبت", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        }
      }
    }

    // Quick Actions
    Text(
      text = "دسترسی‌های سریع",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF0F172A)
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      QuickActionButton(
        title = "رزرو نوبت جدید",
        subtitle = "انتخاب خدمت و ساعت",
        icon = Icons.Default.CalendarMonth,
        backgroundColor = Color(0xFFE8FAF3),
        iconColor = Color(0xFF059669),
        onClick = onNavigateToBooking,
        modifier = Modifier.weight(1f)
      )

      QuickActionButton(
        title = "کیف پول من",
        subtitle = "شارژ و تسویه آنلاین",
        icon = Icons.Default.AccountBalanceWallet,
        backgroundColor = Color(0xFFEFF6FF),
        iconColor = Color(0xFF2563EB),
        onClick = onNavigateToWallet,
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      QuickActionButton(
        title = "سوابق پرونده",
        subtitle = "دستورات و مدارک پزشک",
        icon = Icons.Default.Description,
        backgroundColor = Color(0xFFF5F3FF),
        iconColor = Color(0xFF7C3AED),
        onClick = onNavigateToTreatments,
        modifier = Modifier.weight(1f)
      )

      QuickActionButton(
        title = "اعلان‌ها و پیامک‌ها",
        subtitle = "تاریخچه پیامک‌های دریافتی",
        icon = Icons.Default.Sms,
        backgroundColor = Color(0xFFFEF3C7),
        iconColor = Color(0xFFD97706),
        onClick = onNavigateToNotifications,
        modifier = Modifier.weight(1f)
      )
    }

    // Profile Completion Meter
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("تکمیل پرونده الکترونیک سلامت", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
          Text(PersianFormatter.toPersianDigits("85%"), color = DrBenamPrimaryDark, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
          progress = { 0.85f },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape),
          color = DrBenamPrimary,
          trackColor = DrBenamPrimarySoft
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("اطلاعات شناسنامه‌ای و تلفن تأیید شده است.", fontSize = 11.sp, color = Color(0xFF64748B))
          Text(
            text = "ویرایش >",
            fontSize = 11.sp,
            color = DrBenamPrimaryDark,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onNavigateToProfile() }
          )
        }
      }
    }
  }
}

@Composable
private fun StatCard(
  title: String,
  count: String,
  icon: ImageVector,
  iconTint: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(iconTint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(count, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A))
      Spacer(modifier = Modifier.height(2.dp))
      Text(title, fontSize = 9.sp, color = Color(0xFF64748B), maxLines = 1)
    }
  }
}

@Composable
private fun QuickActionButton(
  title: String,
  subtitle: String,
  icon: ImageVector,
  backgroundColor: Color,
  iconColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .clickable(onClick = onClick)
      .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(backgroundColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
      Spacer(modifier = Modifier.height(2.dp))
      Text(subtitle, fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1)
    }
  }
}
