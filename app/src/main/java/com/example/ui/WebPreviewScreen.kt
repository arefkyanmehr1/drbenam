package com.example.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPreviewScreen(modifier: Modifier = Modifier) {
  var urlInput by remember { mutableStateOf("https://google.com") }
  var activeUrl by remember { mutableStateOf("https://google.com") }
  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var canGoBack by remember { mutableStateOf(false) }
  var canGoForward by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var progressValue by remember { mutableIntStateOf(0) }
  val focusManager = LocalFocusManager.current

  BackHandler(enabled = canGoBack) {
    webViewInstance?.goBack()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Quick URL chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "نمونه‌ها: ",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.width(4.dp))
      SuggestionChip(
        onClick = {
          urlInput = "https://wikipedia.org"
          activeUrl = urlInput
        },
        label = { Text("ویکی‌پدیا") }
      )
      Spacer(modifier = Modifier.width(6.dp))
      SuggestionChip(
        onClick = {
          urlInput = "https://github.com"
          activeUrl = urlInput
        },
        label = { Text("گیت‌هاب") }
      )
    }

    // URL Address Bar & Controls
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Column(modifier = Modifier.padding(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            modifier = Modifier
              .weight(1f)
              .testTag("url_input_field"),
            placeholder = { Text("https://yoursite.com") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "آیکون وب‌سایت",
                tint = MaterialTheme.colorScheme.primary
              )
            },
            trailingIcon = {
              if (urlInput.isNotEmpty()) {
                IconButton(onClick = { urlInput = "" }) {
                  Icon(imageVector = Icons.Default.Clear, contentDescription = "پاک کردن متن")
                }
              }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Uri,
              imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
              onGo = {
                focusManager.clearFocus()
                var formatted = urlInput.trim()
                if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
                  formatted = "https://$formatted"
                }
                urlInput = formatted
                activeUrl = formatted
              }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = {
              focusManager.clearFocus()
              var formatted = urlInput.trim()
              if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
                formatted = "https://$formatted"
              }
              urlInput = formatted
              activeUrl = formatted
            },
            modifier = Modifier
              .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
              .testTag("load_url_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "بارگذاری مجدد",
              tint = MaterialTheme.colorScheme.onPrimary
            )
          }
        }

        // Navigation Bar (Back, Forward, Refresh, Status)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { webViewInstance?.goBack() },
            enabled = canGoBack
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "صفحه قبل"
            )
          }

          IconButton(
            onClick = { webViewInstance?.goForward() },
            enabled = canGoForward
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "صفحه بعد"
            )
          }

          IconButton(
            onClick = { webViewInstance?.reload() }
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "رفرش وب‌ویو"
            )
          }

          Spacer(modifier = Modifier.weight(1f))

          Text(
            text = if (isLoading) "در حال بارگذاری ($progressValue%)..." else "آماده",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    if (isLoading) {
      LinearProgressIndicator(
        progress = { progressValue / 100f },
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp)
      )
    } else {
      Spacer(modifier = Modifier.height(4.dp))
    }

    Spacer(modifier = Modifier.height(8.dp))

    // WebView Container
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.surface)
    ) {
      AndroidView(
        modifier = Modifier
          .fillMaxSize()
          .testTag("webview_container"),
        factory = { context ->
          WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.databaseEnabled = true

            webViewClient = object : WebViewClient() {
              override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isLoading = true
                canGoBack = view?.canGoBack() ?: false
                canGoForward = view?.canGoForward() ?: false
              }

              override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isLoading = false
                canGoBack = view?.canGoBack() ?: false
                canGoForward = view?.canGoForward() ?: false
                url?.let {
                  urlInput = it
                }
              }

              override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
              ): Boolean {
                return false
              }
            }

            webChromeClient = object : WebChromeClient() {
              override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressValue = newProgress
                if (newProgress == 100) {
                  isLoading = false
                }
              }
            }

            loadUrl(activeUrl)
            webViewInstance = this
          }
        },
        update = { webView ->
          if (webView.url != activeUrl) {
            webView.loadUrl(activeUrl)
          }
        }
      )
    }
  }
}
