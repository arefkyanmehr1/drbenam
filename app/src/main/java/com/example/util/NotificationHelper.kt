package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
  const val CHANNEL_ID = "drbenam_official_channel"
  const val CHANNEL_NAME = "اعلان‌ها و یادآوری‌های مطب دکتر بنام"
  const val CHANNEL_DESC = "اطلاع‌رسانی‌های نوبت، پرونده، تراکنش‌های مالی و پیام‌های پزشکی"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = CHANNEL_DESC
        enableLights(true)
        lightColor = Color.parseColor("#0F766E")
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 300, 150, 300)
        setShowBadge(true)
      }

      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      manager?.createNotificationChannel(channel)
    }
  }

  fun showSystemNotification(
    context: Context,
    title: String,
    message: String,
    targetDestination: String? = null,
    notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
  ) {
    createNotificationChannel(context)

    // Check permission for Android 13+ (TIRAMISU)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        // Permission not yet granted by user, cannot post to notification manager
        return
      }
    }

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      if (targetDestination != null) {
        putExtra("NAV_DESTINATION", targetDestination)
      }
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
    )

    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(PersianFormatter.toPersianDigits(title))
      .setContentText(PersianFormatter.toPersianDigits(message))
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText(PersianFormatter.toPersianDigits(message))
          .setSummaryText("مطب دکتر بنام")
      )
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setAutoCancel(true)
      .setSound(defaultSoundUri)
      .setVibrate(longArrayOf(0, 300, 150, 300))
      .setColor(0xFF0F766E.toInt())
      .setContentIntent(pendingIntent)

    try {
      val notificationManager = NotificationManagerCompat.from(context)
      notificationManager.notify(notificationId, notificationBuilder.build())
    } catch (_: SecurityException) {
      // Handled gracefully if permission denied
    } catch (_: Exception) {
      // Ignored
    }
  }
}
