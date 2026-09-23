package com.example.util

import java.text.NumberFormat
import java.util.Locale

object PersianFormatter {
  private val englishToPersianMap = mapOf(
    '0' to '۰',
    '1' to '۱',
    '2' to '۲',
    '3' to '۳',
    '4' to '۴',
    '5' to '۵',
    '6' to '۶',
    '7' to '۷',
    '8' to '۸',
    '9' to '۹'
  )

  fun toPersianDigits(text: String): String {
    val builder = StringBuilder(text.length)
    for (char in text) {
      builder.append(englishToPersianMap[char] ?: char)
    }
    return builder.toString()
  }

  fun formatNumber(number: Long): String {
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(number)
    return toPersianDigits(formatted)
  }

  fun formatPrice(amount: Long): String {
    return "${formatNumber(amount)} تومان"
  }

  fun formatTime(time: String): String {
    return toPersianDigits(time)
  }

  fun formatDate(date: String): String {
    return toPersianDigits(date)
  }

  fun formatTimer(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    val raw = "%02d:%02d".format(mins, secs)
    return toPersianDigits(raw)
  }
}
