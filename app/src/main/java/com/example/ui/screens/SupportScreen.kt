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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.data.DrBenamRepository
import com.example.ui.components.DrBenamProgressDialog
import com.example.ui.components.DrBenamResultDialog
import com.example.ui.theme.DrBenamPrimary
import com.example.ui.theme.DrBenamPrimaryDark
import com.example.ui.theme.DrBenamPrimarySoft
import com.example.util.PersianFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SupportScreen(
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Ticket, 1: Live Chat

  val tickets by DrBenamRepository.supportTickets.collectAsState()
  val chatMessages by DrBenamRepository.chatMessages.collectAsState()

  var newTicketSubject by remember { mutableStateOf("") }
  var newTicketMessage by remember { mutableStateOf("") }
  var showNewTicketForm by remember { mutableStateOf(false) }

  var chatInputText by remember { mutableStateOf("") }

  // Multi-second animated loading states
  var showLoadingDialog by remember { mutableStateOf(false) }
  var loadingTitle by remember { mutableStateOf("") }
  var loadingStatus by remember { mutableStateOf("") }
  var loadingProgress by remember { mutableFloatStateOf(0.2f) }

  // Result dialog
  var resultDialogState by remember { mutableStateOf<Triple<Boolean, String, String>?>(null) }

  Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      Text(
        text = "پشتیبانی و ارتباط با مطب",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A)
      )
      Text(
        text = "پاسخگویی به سوالات پزشکی، امور نوبت‌دهی و پشتیبانی فنی",
        fontSize = 11.sp,
        color = Color(0xFF64748B)
      )

      Spacer(modifier = Modifier.height(14.dp))

      TabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("تیکت‌های من (${PersianFormatter.toPersianDigits(tickets.size.toString())})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("گفتگوی آنلاین با منشی", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (selectedTab == 0) {
        // Tickets Pane
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("لیست درخواست‌ها و تیکت‌ها", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
          Button(
            onClick = { showNewTicketForm = !showNewTicketForm },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary)
          ) {
            Text(if (showNewTicketForm) "بستن فرم" else "تیکت جدید +", fontSize = 11.sp)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (showNewTicketForm) {
          Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text("ثبت درخواست و تیکت جدید", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
              OutlinedTextField(
                value = newTicketSubject,
                onValueChange = { newTicketSubject = it },
                label = { Text("موضوع تیکت") },
                placeholder = { Text("مثال: سوال درباره نحوه مصرف داروی تجویز شده") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
              )
              OutlinedTextField(
                value = newTicketMessage,
                onValueChange = { newTicketMessage = it },
                label = { Text("متن درخواست") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(10.dp)
              )
              Button(
                onClick = {
                  if (newTicketSubject.isNotBlank() && newTicketMessage.isNotBlank()) {
                    scope.launch {
                      showLoadingDialog = true
                      loadingTitle = "ثبت تیکت پشتیبانی"
                      loadingStatus = "در حال ارسال درخواست به منشی مطب..."
                      loadingProgress = 0.5f
                      delay(800)

                      loadingStatus = "صدور شماره پیگیری تیکت..."
                      loadingProgress = 0.9f
                      DrBenamRepository.createTicket(newTicketSubject, newTicketMessage)
                      delay(600)
                      showLoadingDialog = false

                      newTicketSubject = ""
                      newTicketMessage = ""
                      showNewTicketForm = false

                      resultDialogState = Triple(
                        true,
                        "تیکت ثبت شد",
                        "درخواست شما با موفقیت برای منشی مطب دکتر بنام ارسال گردید و در اسرع وقت پاسخ داده خواهد شد."
                      )
                    }
                  }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary)
              ) {
                Text("ارسال تیکت به مطب", fontWeight = FontWeight.Bold)
              }
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
        }

        LazyColumn(
          modifier = Modifier.fillMaxWidth().weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(tickets, key = { it.id }) { ticket ->
            Card(
              modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(ticket.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))

                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(
                        when (ticket.status) {
                          "open" -> Color(0xFFE8FAF3)
                          "in_progress" -> Color(0xFFFFF7E8)
                          else -> Color(0xFFF1F5F9)
                        }
                      )
                      .padding(horizontal = 8.dp, vertical = 3.dp)
                  ) {
                    Text(
                      text = when (ticket.status) {
                        "open" -> "پاسخ داده شد"
                        "in_progress" -> "در حال بررسی"
                        else -> "بسته شده"
                      },
                      fontSize = 10.sp,
                      color = when (ticket.status) {
                        "open" -> Color(0xFF059669)
                        "in_progress" -> Color(0xFFB45309)
                        else -> Color(0xFF475569)
                      },
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                Text(ticket.message, fontSize = 11.sp, color = Color(0xFF475569), lineHeight = 16.sp)
                Text("آخرین بروزرسانی: ${PersianFormatter.toPersianDigits(ticket.updatedAt)}", fontSize = 9.sp, color = Color(0xFF94A3B8))
              }
            }
          }
        }
      } else {
        // Live Chat Pane
        Column(
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
          // Chat messages
          LazyColumn(
            modifier = Modifier
              .weight(1f)
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(chatMessages, key = { it.id }) { msg ->
              val isPatient = msg.senderType == "patient"
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isPatient) Arrangement.End else Arrangement.Start
              ) {
                Box(
                  modifier = Modifier
                    .clip(
                      RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isPatient) 14.dp else 2.dp,
                        bottomEnd = if (isPatient) 2.dp else 14.dp
                      )
                    )
                    .background(if (isPatient) DrBenamPrimary else Color(0xFFF1F5F9))
                    .padding(12.dp)
                ) {
                  Column {
                    Text(
                      text = msg.message,
                      color = if (isPatient) Color.White else Color(0xFF0F172A),
                      fontSize = 12.sp,
                      lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = PersianFormatter.toPersianDigits(msg.createdAt),
                      fontSize = 9.sp,
                      color = if (isPatient) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B),
                      modifier = Modifier.align(Alignment.End)
                    )
                  }
                }
              }
            }
          }

          // Input bar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFFF8FAFC))
              .border(1.dp, Color(0xFFE2E8F0))
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = chatInputText,
              onValueChange = { chatInputText = it },
              placeholder = { Text("پیام خود را بنویسید...", fontSize = 12.sp) },
              modifier = Modifier.weight(1f),
              singleLine = true,
              shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
              onClick = {
                if (chatInputText.isNotBlank()) {
                  DrBenamRepository.sendChatMessage(chatInputText)
                  chatInputText = ""
                }
              },
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DrBenamPrimary)
            ) {
              Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "ارسال", tint = Color.White, modifier = Modifier.size(20.dp))
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
