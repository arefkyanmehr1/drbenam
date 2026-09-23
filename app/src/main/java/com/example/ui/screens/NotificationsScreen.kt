package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DrBenamRepository
import com.example.ui.theme.DrBenamPrimary
import com.example.ui.theme.DrBenamPrimaryDark
import com.example.ui.theme.DrBenamPrimarySoft
import com.example.util.PersianFormatter

@Composable
fun NotificationsScreen(
  onNavigateToAppointments: () -> Unit,
  modifier: Modifier = Modifier
) {
  val notifications by DrBenamRepository.notifications.collectAsState()
  var selectedFilter by remember { mutableIntStateOf(0) }

  val unreadCount = notifications.count { !it.isRead }

  val filteredNotifications = remember(notifications, selectedFilter) {
    when (selectedFilter) {
      1 -> notifications.filter { !it.isRead }
      2 -> notifications.filter { it.category == "appointment" }
      3 -> notifications.filter { it.category == "system" }
      else -> notifications
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFC))
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(DrBenamPrimarySoft),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            tint = DrBenamPrimaryDark,
            modifier = Modifier.size(21.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Text(
            text = "اعلان‌ها",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
          Text(
            text = "اطلاع‌رسانی‌های واقعی حساب شما",
            fontSize = 11.sp,
            color = Color(0xFF64748B)
          )
        }
      }

      if (unreadCount > 0) {
        TextButton(onClick = { DrBenamRepository.markAllNotificationsRead() }) {
          Text(
            "خواندن همه",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = DrBenamPrimary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      FilterChip(
        selected = selectedFilter == 0,
        onClick = { selectedFilter = 0 },
        label = { Text("همه", fontSize = 11.sp) }
      )
      FilterChip(
        selected = selectedFilter == 1,
        onClick = { selectedFilter = 1 },
        label = { Text("خوانده‌نشده", fontSize = 11.sp) }
      )
      FilterChip(
        selected = selectedFilter == 2,
        onClick = { selectedFilter = 2 },
        label = { Text("نوبت‌ها", fontSize = 11.sp) }
      )
      FilterChip(
        selected = selectedFilter == 3,
        onClick = { selectedFilter = 3 },
        label = { Text("سیستم", fontSize = 11.sp) }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (filteredNotifications.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxWidth().weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(42.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "اعلانی وجود ندارد",
            color = Color(0xFF64748B),
            fontSize = 12.sp
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredNotifications, key = { it.id }) { notif ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .border(
                width = 1.dp,
                color = if (!notif.isRead) {
                  DrBenamPrimary.copy(alpha = 0.5f)
                } else {
                  Color(0xFFE2E8F0)
                },
                shape = RoundedCornerShape(14.dp)
              ),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (!notif.isRead) Color(0xFFF0FDF9) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(
                    if (!notif.isRead) DrBenamPrimarySoft
                    else Color(0xFFF1F5F9)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.EventNote,
                  contentDescription = null,
                  tint = if (!notif.isRead) DrBenamPrimaryDark else Color(0xFF64748B),
                  modifier = Modifier.size(18.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    notif.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                  )
                  Text(
                    PersianFormatter.toPersianDigits(notif.relativeTime),
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = PersianFormatter.toPersianDigits(notif.message),
                  fontSize = 11.sp,
                  color = Color(0xFF475569),
                  lineHeight = 16.sp
                )
              }

              IconButton(
                onClick = { DrBenamRepository.deleteNotification(notif.id) },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  Icons.Default.Delete,
                  contentDescription = "حذف",
                  tint = Color(0xFF94A3B8),
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
