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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DrBenamRepository
import com.example.data.LoginAuthStep
import com.example.ui.components.DrBenamProgressDialog
import com.example.ui.components.DrBenamResultDialog
import com.example.ui.theme.DrBenamPrimary
import com.example.ui.theme.DrBenamPrimaryDark
import com.example.ui.theme.DrBenamPrimarySoft
import com.example.util.PersianFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
  onLoginSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()

  var authStep by remember { mutableStateOf(LoginAuthStep.MOBILE_INPUT) }
  var selectedTab by remember { mutableIntStateOf(0) } // 0: SMS OTP, 1: Password

  var mobileNumber by remember { mutableStateOf("") }
  var otpCode by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var rememberMe by remember { mutableStateOf(true) }

  // Multi-second animated loading states
  var showLoadingDialog by remember { mutableStateOf(false) }
  var loadingTitle by remember { mutableStateOf("") }
  var loadingStatus by remember { mutableStateOf("") }
  var loadingProgress by remember { mutableFloatStateOf(0.2f) }

  // Result dialog
  var resultDialogState by remember { mutableStateOf<Triple<Boolean, String, String>?>(null) }
  var countdownSeconds by remember { mutableIntStateOf(120) }

  // Countdown timer for OTP
  LaunchedEffect(authStep) {
    if (authStep == LoginAuthStep.OTP_VERIFY) {
      countdownSeconds = 120
      while (countdownSeconds > 0) {
        delay(1000)
        countdownSeconds--
      }
    }
  }

  Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(18.dp))

      // Doctor Brand Header Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color(0xFF1F4839), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12382B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(
          modifier = Modifier.padding(22.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DrBenamPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = "Dr. Benam",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
              )
              Text(
                text = "مطب دکتر ابراهیم بنام • متخصص قلب و عروق",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = "سلام، خوش آمدید.",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "برای مدیریت نوبت‌ها، سوابق درمان و پیگیری ویزیت، وارد حساب کاربری خود شوید.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            lineHeight = 20.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("مدیریت آسان نوبت‌ها و استرداد آنلاین وجه", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("ورود امن با پیامک یکبار مصرف (سامانه ملی‌پیامک)", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Form Container Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp)
        ) {
          // Toggle tabs if in root steps
          if (authStep == LoginAuthStep.MOBILE_INPUT || authStep == LoginAuthStep.PASSWORD_LOGIN) {
            TabRow(
              selectedTabIndex = selectedTab,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
            ) {
              Tab(
                selected = selectedTab == 0,
                onClick = {
                  selectedTab = 0
                  authStep = LoginAuthStep.MOBILE_INPUT
                },
                text = { Text("ورود با پیامک (OTP)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
              )
              Tab(
                selected = selectedTab == 1,
                onClick = {
                  selectedTab = 1
                  authStep = LoginAuthStep.PASSWORD_LOGIN
                },
                text = { Text("ورود با رمز عبور", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
              )
            }
            Spacer(modifier = Modifier.height(18.dp))
          }

          when (authStep) {
            LoginAuthStep.MOBILE_INPUT -> {
              Text(
                text = "ورود به حساب کاربری",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "شماره موبایل خود را وارد کنید تا کد تأیید ۶ رقمی پیامک شود.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )

              Spacer(modifier = Modifier.height(16.dp))

              OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("mobile_input_field"),
                label = { Text("شماره موبایل") },
                placeholder = { Text("09123456789") },
                leadingIcon = {
                  Icon(Icons.Default.Phone, contentDescription = null, tint = DrBenamPrimary)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )

              Spacer(modifier = Modifier.height(12.dp))

              Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                  checked = rememberMe,
                  onCheckedChange = { rememberMe = it }
                )
                Text("مرا به خاطر بسپار", fontSize = 12.sp)
              }

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = {
                  if (mobileNumber.length < 10) {
                    resultDialogState = Triple(false, "خطا در شماره موبایل", "لطفاً شماره موبایل را به‌صورت صحیح ۱۱ رقمی وارد نمایید.")
                  } else {
                    scope.launch {
                      showLoadingDialog = true
                      loadingTitle = "ارسال کد تأیید پیامکی"
                      loadingStatus = "در حال اتصال به وب‌سایت drbenam.com..."
                      loadingProgress = 0.25f
                      delay(800)

                      loadingStatus = "درخواست صدور کد یکبار مصرف از سرور..."
                      loadingProgress = 0.60f
                      delay(900)

                      loadingStatus = "ارسال پیامک از طریق سامانه ملی‌پیامک..."
                      loadingProgress = 0.90f
                      val result = DrBenamRepository.sendOtpOnline(mobileNumber)
                      delay(600)
                      showLoadingDialog = false

                      authStep = LoginAuthStep.OTP_VERIFY
                      resultDialogState = Triple(true, "پیامک ارسال گردید", result.second)
                    }
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("send_otp_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary)
              ) {
                Text("دریافت کد تأیید پیامکی", fontWeight = FontWeight.Bold)
              }
            }

            LoginAuthStep.OTP_VERIFY -> {
              Text(
                text = "کد تأیید را وارد کنید",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "کد ۶ رقمی پیامک‌شده برای شماره ${PersianFormatter.toPersianDigits(mobileNumber)} را وارد فرمایید.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )

              Spacer(modifier = Modifier.height(16.dp))

              OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6) otpCode = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("otp_code_field"),
                label = { Text("کد ۶ رقمی تأیید") },
                placeholder = { Text("۱۲۳۴۵۶") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (countdownSeconds > 0) {
                  Text(
                    text = "ارسال مجدد کد تا ${PersianFormatter.formatTimer(countdownSeconds)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                } else {
                  TextButton(onClick = {
                    scope.launch {
                      showLoadingDialog = true
                      loadingTitle = "ارسال مجدد پیامک"
                      loadingStatus = "در حال ارسال مجدد کد تأیید..."
                      loadingProgress = 0.5f
                      DrBenamRepository.sendOtpOnline(mobileNumber)
                      delay(1200)
                      showLoadingDialog = false
                      countdownSeconds = 120
                    }
                  }) {
                    Text("ارسال مجدد کد", color = DrBenamPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                }

                TextButton(onClick = {
                  authStep = LoginAuthStep.MOBILE_INPUT
                }) {
                  Text("ویرایش شماره موبایل", fontSize = 12.sp)
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = {
                  if (otpCode.length < 5) {
                    resultDialogState = Triple(false, "کد تأیید ناقص است", "لطفاً کد ۶ رقمی ارسالی را به طور کامل وارد نمایید.")
                  } else {
                    scope.launch {
                      showLoadingDialog = true
                      loadingTitle = "اعتبارسنجی و ورود"
                      loadingStatus = "در حال بررسی صحت کد تأیید در سرور..."
                      loadingProgress = 0.4f
                      delay(900)

                      loadingStatus = "بارگذاری پرونده پزشکی بیمار..."
                      loadingProgress = 0.85f
                      val verifyRes = DrBenamRepository.verifyOtpOnline(otpCode)
                      delay(700)
                      showLoadingDialog = false

                      if (verifyRes.first) {
                        onLoginSuccess()
                      } else {
                        resultDialogState = Triple(false, "خطا در احراز هویت", verifyRes.second)
                      }
                    }
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("verify_otp_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary)
              ) {
                Text("تأیید و ورود به پنل کاربری", fontWeight = FontWeight.Bold)
              }
            }

            LoginAuthStep.PASSWORD_LOGIN -> {
              Text(
                text = "ورود با رمز عبور",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "شماره موبایل و رمز عبور تعیین‌شده خود را وارد کنید.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )

              Spacer(modifier = Modifier.height(14.dp))

              OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("شماره موبایل") },
                leadingIcon = {
                  Icon(Icons.Default.Phone, contentDescription = null, tint = DrBenamPrimary)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )

              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("رمز عبور") },
                leadingIcon = {
                  Icon(Icons.Default.Lock, contentDescription = null, tint = DrBenamPrimary)
                },
                trailingIcon = {
                  IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                      imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                      contentDescription = null
                    )
                  }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )

              Spacer(modifier = Modifier.height(14.dp))

              Button(
                onClick = {
                  if (mobileNumber.isEmpty() || passwordInput.isEmpty()) {
                    resultDialogState = Triple(false, "اطلاعات ناقص", "لطفاً شماره موبایل و رمز عبور را وارد فرمایید.")
                  } else {
                    scope.launch {
                      showLoadingDialog = true
                      loadingTitle = "ورود با رمز عبور"
                      loadingStatus = "در حال احراز هویت با دیتابیس..."
                      loadingProgress = 0.5f
                      val result = DrBenamRepository.loginPassword(mobileNumber, passwordInput)
                      showLoadingDialog = false
                      if (result.first) {
                        onLoginSuccess()
                      } else {
                        resultDialogState = Triple(false, "خطا در ورود", result.second)
                      }
                    }
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary)
              ) {
                Text("ورود به حساب", fontWeight = FontWeight.Bold)
              }
            }

            else -> {}
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "محیط اختصاصی بیماران مطب دکتر ابراهیم بنام • امن و محرمانه",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        textAlign = TextAlign.Center
      )
    }

    // Loading Dialog
    if (showLoadingDialog) {
      DrBenamProgressDialog(
        title = loadingTitle,
        currentStatus = loadingStatus,
        progress = loadingProgress
      )
    }

    // Result/Feedback Dialog
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
