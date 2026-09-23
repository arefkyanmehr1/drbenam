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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
  val allSms by DrBenamRepository.allSmsMessages.collectAsState()

  var selectedMainTab by remember { mutableIntStateOf(0) } // 0: Notifications, 1: SMS Inbox
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
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "مرکز پیام‌ها و اعلان‌ها",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
        Text(
          text = "اطلاع‌رسانی‌های نوبت، پرونده و پیامک‌های دریافتی",
          fontSize = 11.sp,
          color = Color(0xFF64748B)
        )
      }

      if (selectedMainTab == 0 && unreadCount > 0) {
        TextButton(onClick = { DrBenamRepository.markAllNotificationsRead() }) {
          Text("خواندن همه", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DrBenamPrimary)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Main Tab Row
    TabRow(
      selectedTabIndex = selectedMainTab,
      modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
      Tab(
        selected = selectedMainTab == 0,
        onClick = { selectedMainTab = 0 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("اعلان‌ها (${PersianFormatter.toPersianDigits(notifications.size.toString())})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      )
      Tab(
        selected = selectedMainTab == 1,
        onClick = { selectedMainTab = 1 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("صندوق پیامک‌ها (${PersianFormatter.toPersianDigits(allSms.size.toString())})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (selectedMainTab == 0) {
      // Filters
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
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (filteredNotifications.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
          Text("اعلانی وجود ندارد", color = Color(0xFF64748B), fontSize = 12.sp)
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
                  color = if (!notif.isRead) DrBenamPrimary.copy(alpha = 0.5f) else Color(0xFFE2E8F0),
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
                    .background(if (!notif.isRead) DrBenamPrimarySoft else Color(0xFFF1F5F9)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.EventNote,
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
                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                    Text(PersianFormatter.toPersianDigits(notif.relativeTime), fontSize = 10.sp, color = Color(0xFF94A3B8))
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
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }
      }
    } else {
      // SMS Inbox Tab
      if (allSms.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Sms, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("پیامکی ثبت نشده است.", color = Color(0xFF64748B), fontSize = 12.sp)
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxWidth().weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(allSms, key = { it.id }) { sms ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DrBenamPrimarySoft),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.Sms, contentDescription = null, tint = DrBenamPrimaryDark, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(sms.sender, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                  }

                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(Color(0xFFE8FAF3))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text("ارسال موفق", color = Color(0xFF059669), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = PersianFormatter.toPersianDigits(sms.message),
                  fontSize = 11.sp,
                  color = Color(0xFF334155),
                  lineHeight = 18.sp
                )
              }
            }
          }
        }
      }
    }
  }
}
