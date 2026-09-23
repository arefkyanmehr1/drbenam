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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
fun ProfileScreen(
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  val user by DrBenamRepository.userProfile.collectAsState()
  val appointments by DrBenamRepository.appointments.collectAsState()
  val treatments by DrBenamRepository.treatments.collectAsState()

  var firstName by remember(user) { mutableStateOf(user.firstName) }
  var lastName by remember(user) { mutableStateOf(user.lastName) }
  var nationalCode by remember(user) { mutableStateOf(user.nationalId) }
  var birthDate by remember(user) { mutableStateOf(user.birthDate) }
  var gender by remember(user) { mutableStateOf(user.gender) }
  var email by remember(user) { mutableStateOf(user.email) }

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
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text(
        text = "مشخصات و پرونده کاربری",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A)
      )
      Text(
        text = "اطلاعات هویتی جهت صدور نسخه الکترونیک و پرونده سلامت",
        fontSize = 11.sp,
        color = Color(0xFF64748B)
      )

      // Avatar Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Row(
          modifier = Modifier.padding(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(DrBenamPrimary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = user.firstName.take(1).ifEmpty { "ک" },
              color = Color.White,
              fontWeight = FontWeight.Black,
              fontSize = 22.sp
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
            Text(
              "شماره موبایل: ${PersianFormatter.toPersianDigits(user.phone)}",
              fontSize = 11.sp,
              color = Color(0xFF64748B)
            )
            Text(
              "تعداد مراجعات: ${PersianFormatter.toPersianDigits(appointments.size.toString())} نوبت • ${PersianFormatter.toPersianDigits(treatments.size.toString())} پرونده درمانی",
              fontSize = 10.sp,
              color = DrBenamPrimaryDark,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Edit Form Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text("اطلاعات فردی", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = firstName,
              onValueChange = { firstName = it },
              label = { Text("نام") },
              modifier = Modifier.weight(1f),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )
            OutlinedTextField(
              value = lastName,
              onValueChange = { lastName = it },
              label = { Text("نام خانوادگی") },
              modifier = Modifier.weight(1f),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )
          }

          OutlinedTextField(
            value = nationalCode,
            onValueChange = { nationalCode = it },
            label = { Text("کد ملی") },
            placeholder = { Text("0012345678") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
          )

          OutlinedTextField(
            value = birthDate,
            onValueChange = { birthDate = it },
            label = { Text("تاریخ تولد") },
            placeholder = { Text("1370/01/01") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
          )

          Text("جنسیت:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
          Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              RadioButton(selected = gender == "male", onClick = { gender = "male" })
              Text("مرد", fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              RadioButton(selected = gender == "female", onClick = { gender = "female" })
              Text("زن", fontSize = 12.sp)
            }
          }

          HorizontalDivider(color = Color(0xFFE2E8F0))

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("آدرس ایمیل") },
            placeholder = { Text("example@domain.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(4.dp))

          Button(
            onClick = {
              scope.launch {
                showLoadingDialog = true
                loadingTitle = "ذخیره مشخصات"
                loadingStatus = "در حال اتصال به دیتابیس مطب دکتر بنام..."
                loadingProgress = 0.35f
                delay(800)

                loadingStatus = "بروزرسانی اطلاعات هویتی و ثبت در پرونده الکترونیک..."
                loadingProgress = 0.85f
                delay(700)

                DrBenamRepository.updateProfile(
                  firstName = firstName,
                  lastName = lastName,
                  nationalCode = nationalCode,
                  birthDate = birthDate,
                  gender = gender,
                  email = email
                )
                showLoadingDialog = false

                resultDialogState = Triple(
                  true,
                  "تغییرات ذخیره شد",
                  "مشخصات هویتی و پرونده کاربری شما با موفقیت در سیستم مرکزی مطب ذخیره گردید."
                )
              }
            },
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary)
          ) {
            Text("ذخیره تغییرات پرونده کاربری", fontWeight = FontWeight.Bold)
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
