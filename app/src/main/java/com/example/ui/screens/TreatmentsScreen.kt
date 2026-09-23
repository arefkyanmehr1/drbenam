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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.TreatmentRecord
import com.example.ui.theme.DrBenamPrimary
import com.example.ui.theme.DrBenamPrimaryDark
import com.example.ui.theme.DrBenamPrimarySoft
import com.example.util.PersianFormatter

@Composable
fun TreatmentsScreen(
  modifier: Modifier = Modifier
) {
  val treatments by DrBenamRepository.treatments.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedRecordForDetail by remember { mutableStateOf<TreatmentRecord?>(null) }

  val filteredTreatments = treatments.filter {
    searchQuery.isEmpty() ||
      it.title.contains(searchQuery, ignoreCase = true) ||
      it.body.contains(searchQuery, ignoreCase = true) ||
      it.serviceTitle.contains(searchQuery, ignoreCase = true)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFC))
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("پرونده سلامت و سوابق درمانی", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        Text("سوابق معاینات، اکوکاردیوگرافی، نوار قلب و دستورات پزشک", fontSize = 11.sp, color = Color(0xFF64748B))
      }
    }

    // Search Box
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("جستجو در سوابق درمانی و پرونده...", fontSize = 12.sp) },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = DrBenamPrimary)
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp)
    )

    // Summary Cards Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      SummaryMiniCard(
        title = "سوابق ثبت‌شده",
        count = "${PersianFormatter.toPersianDigits(treatments.size.toString())} مورد",
        modifier = Modifier.weight(1f)
      )
      SummaryMiniCard(
        title = "مدارک پزشکی",
        count = "${PersianFormatter.toPersianDigits(treatments.sumOf { it.attachments.size }.toString())} فایل",
        modifier = Modifier.weight(1f)
      )
      SummaryMiniCard(
        title = "آخرین مراجعه",
        count = PersianFormatter.toPersianDigits(treatments.firstOrNull()?.dateShamsi ?: "---"),
        modifier = Modifier.weight(1f)
      )
    }

    // Treatments List
    if (filteredTreatments.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(44.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text("سابقه‌ای با این مشخصات پیدا نشد.", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredTreatments, key = { it.id }) { record ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(DrBenamPrimarySoft),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = DrBenamPrimaryDark, modifier = Modifier.size(18.dp))
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(record.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                    Text("${record.serviceTitle} • ${record.doctorName}", fontSize = 10.sp, color = Color(0xFF64748B))
                  }
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(PersianFormatter.toPersianDigits(record.dateShamsi), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DrBenamPrimaryDark)
                }
              }

              // Body notes
              Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("شرح ثبت‌شده توسط پزشک:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DrBenamPrimaryDark)
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(PersianFormatter.toPersianDigits(record.body), fontSize = 11.sp, lineHeight = 18.sp, color = Color(0xFF334155))
                }
              }

              // Attachments & Action
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "ضمائم: ${PersianFormatter.toPersianDigits(record.attachments.size.toString())} مدارک ضمیمه",
                  fontSize = 11.sp,
                  color = Color(0xFF64748B)
                )

                Button(
                  onClick = { selectedRecordForDetail = record },
                  colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.height(34.dp)
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("مشاهده نسخه و فایل‌ها", fontSize = 10.sp)
                }
              }
            }
          }
        }
      }
    }

    // Detail Dialog
    if (selectedRecordForDetail != null) {
      val item = selectedRecordForDetail!!
      AlertDialog(
        onDismissRequest = { selectedRecordForDetail = null },
        title = {
          Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("تاریخ مراجعه: ${PersianFormatter.toPersianDigits(item.dateShamsi)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("پزشک معالج: ${item.doctorName}", fontSize = 11.sp)
            Text("خدمت: ${item.serviceTitle}", fontSize = 11.sp)

            Text("شرح کامل دستورات پزشکی:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Card(
              colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = PersianFormatter.toPersianDigits(item.body),
                fontSize = 11.sp,
                modifier = Modifier.padding(10.dp),
                lineHeight = 18.sp
              )
            }

            if (item.attachments.isNotEmpty()) {
              Text("فایل‌های پیوست:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
              item.attachments.forEach { file ->
                Text("• ${file.name} (${PersianFormatter.toPersianDigits(file.size.toString())} کیلوبایت)", fontSize = 11.sp, color = DrBenamPrimaryDark)
              }
            }
          }
        },
        confirmButton = {
          Button(onClick = { selectedRecordForDetail = null }, shape = RoundedCornerShape(8.dp)) {
            Text("بستن", fontSize = 11.sp)
          }
        }
      )
    }
  }
}

@Composable
private fun SummaryMiniCard(
  title: String,
  count: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(count, fontWeight = FontWeight.Black, fontSize = 13.sp, color = DrBenamPrimaryDark)
      Spacer(modifier = Modifier.height(2.dp))
      Text(title, fontSize = 9.sp, color = Color(0xFF64748B), maxLines = 1)
    }
  }
}
