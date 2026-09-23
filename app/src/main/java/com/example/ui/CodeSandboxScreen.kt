package com.example.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private const val SAMPLE_LOGIN_HTML = """
<!DOCTYPE html>
<html dir="rtl" lang="fa">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    body { font-family: system-ui, sans-serif; background: #0f172a; color: #f8fafc; padding: 20px; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 80vh; margin: 0; }
    .card { background: #1e293b; padding: 24px; border-radius: 16px; width: 100%; max-width: 320px; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }
    h2 { margin-top: 0; color: #38bdf8; text-align: center; }
    input { width: 100%; padding: 12px; margin: 8px 0 16px; border-radius: 8px; border: 1px solid #334155; background: #0f172a; color: #fff; box-sizing: border-box; }
    button { width: 100%; padding: 12px; background: #6366f1; border: none; border-radius: 8px; color: #fff; font-weight: bold; cursor: pointer; }
    button:active { background: #4f46e5; }
  </style>
</head>
<body>
  <div class="card">
    <h2>ورود به حساب کاربری</h2>
    <label>نام کاربری یا ایمیل:</label>
    <input type="text" placeholder="example@mail.com" />
    <label>رمز عبور:</label>
    <input type="password" placeholder="••••••••" />
    <button onclick="alert('ورود با موفقیت انجام شد!')">ورود به سیستم</button>
  </div>
</body>
</html>
"""

private const val SAMPLE_STORE_HTML = """
<!DOCTYPE html>
<html dir="rtl" lang="fa">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    body { font-family: system-ui, sans-serif; background: #f1f5f9; padding: 16px; margin: 0; }
    .product-grid { display: flex; flex-direction: column; gap: 14px; }
    .product-card { background: white; border-radius: 16px; padding: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); display: flex; justify-content: space-between; align-items: center; }
    .title { font-weight: bold; font-size: 16px; color: #1e293b; }
    .price { color: #059669; font-weight: 700; margin-top: 4px; }
    .badge { background: #e0e7ff; color: #4338ca; padding: 4px 8px; border-radius: 6px; font-size: 12px; }
    .buy-btn { background: #2563eb; color: white; border: none; padding: 8px 14px; border-radius: 8px; font-weight: bold; }
  </style>
</head>
<body>
  <div class="product-grid">
    <div class="product-card">
      <div>
        <span class="badge">تخفیف ویژه</span>
        <div class="title">هدفون بی‌سیم پرو</div>
        <div class="price">۱,۴۵۰,۰۰۰ تومان</div>
      </div>
      <button class="buy-btn" onclick="alert('به سبد خرید اضافه شد')">خرید</button>
    </div>
    <div class="product-card">
      <div>
        <span class="badge">جدید</span>
        <div class="title">ساعت هوشمند اولترا</div>
        <div class="price">۲,۹۰۰,۰۰۰ تومان</div>
      </div>
      <button class="buy-btn" onclick="alert('به سبد خرید اضافه شد')">خرید</button>
    </div>
  </div>
</body>
</html>
"""

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CodeSandboxScreen(modifier: Modifier = Modifier) {
  var codeText by remember { mutableStateOf(SAMPLE_LOGIN_HTML.trim()) }
  var renderedHtml by remember { mutableStateOf(SAMPLE_LOGIN_HTML.trim()) }
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Editor, 1: Live Render
  var selectedPreset by remember { mutableStateOf("login") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Preset Selector
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = selectedPreset == "login",
        onClick = {
          selectedPreset = "login"
          codeText = SAMPLE_LOGIN_HTML.trim()
          renderedHtml = codeText
        },
        label = { Text("فرم ورود و ثبت‌نام") }
      )
      FilterChip(
        selected = selectedPreset == "store",
        onClick = {
          selectedPreset = "store"
          codeText = SAMPLE_STORE_HTML.trim()
          renderedHtml = codeText
        },
        label = { Text("فروشگاه و محصولات") }
      )
      FilterChip(
        selected = selectedPreset == "custom",
        onClick = {
          selectedPreset = "custom"
        },
        label = { Text("کد اختصاصی شما") }
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // View Mode Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Code, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("ویرایش کد وب")
          }
        }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = {
          renderedHtml = codeText
          selectedTab = 1
        },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("پیش‌نمایش زنده")
          }
        }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (selectedTab == 0) {
      // Code Editor
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "کدهای HTML / CSS / JS سایت خود را اینجا جای‌گذاری کنید:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = codeText,
            onValueChange = {
              codeText = it
              selectedPreset = "custom"
            },
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .testTag("code_editor_field"),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                renderedHtml = codeText
                selectedTab = 1
              },
              modifier = Modifier
                .weight(1f)
                .testTag("render_code_button"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("اجرا و مشاهده پیش‌نمایش")
            }

            Button(
              onClick = {
                codeText = ""
                renderedHtml = ""
                selectedPreset = "custom"
              },
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.Refresh, contentDescription = "پاک کردن")
            }
          }
        }
      }
    } else {
      // Live Web Rendering Screen
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          AndroidView(
            modifier = Modifier
              .fillMaxSize()
              .testTag("sandbox_webview"),
            factory = { context ->
              WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadDataWithBaseURL(null, renderedHtml, "text/html", "UTF-8", null)
              }
            },
            update = { webView ->
              webView.loadDataWithBaseURL(null, renderedHtml, "text/html", "UTF-8", null)
            }
          )
        }
      }
    }
  }
}
