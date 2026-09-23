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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.WalletTx
import com.example.ui.components.DrBenamProgressDialog
import com.example.ui.components.DrBenamResultDialog
import com.example.ui.theme.DrBenamDanger
import com.example.ui.theme.DrBenamPrimary
import com.example.ui.theme.DrBenamPrimaryDark
import com.example.ui.theme.DrBenamPrimarySoft
import com.example.util.PersianFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WalletScreen(
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  val balance by DrBenamRepository.walletBalance.collectAsState()
  val transactions by DrBenamRepository.walletTransactions.collectAsState()

  var showDepositDialog by remember { mutableStateOf(false) }
  var showWithdrawDialog by remember { mutableStateOf(false) }

  var depositAmountText by remember { mutableStateOf("") }
  var withdrawAmountText by remember { mutableStateOf("") }
  var ibanInput by remember { mutableStateOf("") }

  // Multi-second progressive loading dialog
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
        text = "کیف پول و امور مالی",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A)
      )
      Text(
        text = "مدیریت موجودی، شارژ آنلاین و تسویه نوبت‌های استردادی",
        fontSize = 11.sp,
        color = Color(0xFF64748B)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Balance Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color(0xFF134E48), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3E38)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(DrBenamPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text("موجودی قابل استفاده", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0D9488).copy(alpha = 0.35f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text("حساب بیمار", color = Color(0xFF5EEAD4), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = PersianFormatter.formatPrice(balance),
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black
          )

          Spacer(modifier = Modifier.height(18.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { resultDialogState = Triple(false, "درگاه متصل نیست", "تا زمانی که درگاه واقعی سایت و callback آن به API متصل نشود، شارژ کیف پول انجام نمی‌شود و هیچ مبلغ ساختگی ثبت نخواهد شد.") },
              modifier = Modifier.weight(1f).height(44.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("شارژ آنلاین کیف پول", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Button(
              onClick = { showWithdrawDialog = true },
              modifier = Modifier.weight(1f).height(44.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.15f),
                contentColor = Color.White
              )
            ) {
              Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("درخواست تسویه", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "تاریخچه تراکنش‌های کیف پول",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A)
      )

      Spacer(modifier = Modifier.height(8.dp))

      if (transactions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("هیچ تراکنشی ثبت نشده است.", color = Color(0xFF64748B), fontSize = 12.sp)
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxWidth().weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(transactions, key = { it.id }) { tx ->
            Card(
              modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (tx.isInflow) Color(0xFFE8FAF3) else Color(0xFFFFF0F2)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = if (tx.isInflow) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (tx.isInflow) Color(0xFF059669) else Color(0xFFDC2626),
                    modifier = Modifier.size(20.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(tx.description, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                  Text(
                    text = "پیگیری: ${PersianFormatter.toPersianDigits(tx.trackingCode)} • ${PersianFormatter.toPersianDigits(tx.createdAt)}",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                  )
                  if (!tx.cardOrIban.isNullOrEmpty()) {
                    Text("شبا: ${PersianFormatter.toPersianDigits(tx.cardOrIban)}", fontSize = 10.sp, color = DrBenamPrimaryDark)
                  }
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = (if (tx.isInflow) "+ " else "- ") + PersianFormatter.formatPrice(tx.amount),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = if (tx.isInflow) Color(0xFF059669) else Color(0xFFDC2626)
                  )

                  Text(
                    text = when (tx.status) {
                      "completed" -> "تکمیل شده"
                      "pending" -> "در انتظار بررسی"
                      else -> "ناموفق"
                    },
                    fontSize = 9.sp,
                    color = if (tx.status == "completed") Color(0xFF059669) else Color(0xFFD97706),
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }
    }

    // Deposit Dialog
    if (showDepositDialog) {
      AlertDialog(
        onDismissRequest = { showDepositDialog = false },
        title = { Text("شارژ آنلاین کیف پول", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("مبلغ مورد نظر برای افزایش اعتبار را انتخاب یا وارد فرمایید:", fontSize = 11.sp, color = Color(0xFF64748B))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf("200000", "500000", "1000000").forEach { quickAmount ->
                FilterChip(
                  selected = depositAmountText == quickAmount,
                  onClick = { depositAmountText = quickAmount },
                  label = { Text(PersianFormatter.formatPrice(quickAmount.toLong()), fontSize = 10.sp) }
                )
              }
            }

            OutlinedTextField(
              value = depositAmountText,
              onValueChange = { depositAmountText = it },
              label = { Text("مبلغ به تومان") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val amt = depositAmountText.toLongOrNull() ?: 0
              if (amt >= 50000) {
                showDepositDialog = false
                scope.launch {
                  showLoadingDialog = true
                  loadingTitle = "شارژ کیف پول"
                  loadingStatus = "بررسی وضعیت درگاه پرداخت..."
                  loadingProgress = 0.30f
                  delay(900)

                  loadingStatus = "درخواست به سرور سایت..."
                  loadingProgress = 0.70f
                  delay(800)

                  loadingStatus = "دریافت پاسخ واقعی از API..."
                  loadingProgress = 0.95f
                  val result = DrBenamRepository.depositWallet(amt)
                  showLoadingDialog = false
                  resultDialogState = Triple(result.first, if (result.first) "کیف پول شارژ شد" else "شارژ انجام نشد", result.second)
                }
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("اتصال به درگاه و شارژ", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { showDepositDialog = false }, shape = RoundedCornerShape(10.dp)) {
            Text("انصراف", fontSize = 11.sp)
          }
        }
      )
    }

    // Withdraw Dialog
    if (showWithdrawDialog) {
      AlertDialog(
        onDismissRequest = { showWithdrawDialog = false },
        title = { Text("درخواست تسویه وجه به حساب", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("مبلغ درخواستی به شماره شبای ثبت‌شده بیمار واریز خواهد شد.", fontSize = 11.sp, color = Color(0xFF64748B))

            OutlinedTextField(
              value = withdrawAmountText,
              onValueChange = { withdrawAmountText = it },
              label = { Text("مبلغ تسویه (تومان)") },
              placeholder = { Text("مثال: 200000") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = ibanInput,
              onValueChange = { ibanInput = it },
              label = { Text("شماره شبا (IBAN)") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )

            Text(
              "حداکثر موجودی قابل تسویه: ${PersianFormatter.formatPrice(balance)}",
              fontSize = 11.sp,
              color = DrBenamPrimaryDark,
              fontWeight = FontWeight.Bold
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val amt = withdrawAmountText.toLongOrNull() ?: 0
              if (amt > 0 && amt <= balance) {
                showWithdrawDialog = false
                scope.launch {
                  showLoadingDialog = true
                  loadingTitle = "درخواست تسویه وجه"
                  loadingStatus = "ارسال درخواست تسویه به سرور سایت..."
                  loadingProgress = 0.35f
                  delay(900)

                  loadingStatus = "ثبت تراکنش مالی در دیتابیس..."
                  loadingProgress = 0.70f
                  delay(800)

                  loadingStatus = "دریافت نتیجه واقعی API..."
                  loadingProgress = 0.95f
                  val result = DrBenamRepository.withdrawWallet(amt, ibanInput)
                  showLoadingDialog = false
                  resultDialogState = Triple(result.first, if (result.first) "درخواست تسویه ثبت شد" else "تسویه انجام نشد", result.second)
                }
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DrBenamPrimary),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("ثبت تسویه و ارسال پیامک", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { showWithdrawDialog = false }, shape = RoundedCornerShape(10.dp)) {
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
