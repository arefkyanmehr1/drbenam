package com.example.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object MelipayamakSmsService {
  private const val API_URL = "https://console.melipayamak.com/api/send/shared/338e273d263744b88d3f26919beebe4b"
  const val BODY_ID_APPOINTMENT = 536371
  const val BODY_ID_CANCELLATION = 542683
  const val BODY_ID_WITHDRAWAL = 536715

  private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  /**
   * ارسال پیامک ثبت قطعی نوبت بر اساس پترن ۵۳۶۳۷۱ ملی‌پیامک
   * {0} = نام بیمار
   * {1} = عنوان پزشک (متخصص قلب و عروق)
   * {2} = تاریخ و ساعت نوبت
   * {3} = کد رهگیری
   */
  suspend fun sendAppointmentSms(
    mobile: String,
    patientName: String,
    doctorTitle: String = "متخصص قلب و عروق",
    appointmentDateTime: String,
    trackingCode: String
  ): Boolean = withContext(Dispatchers.IO) {
    val cleanMobile = normalizeMobile(mobile)
    if (cleanMobile.isEmpty()) return@withContext false

    try {
      val args = JSONArray().apply {
        put(patientName.ifBlank { "کاربر گرامی" })
        put(doctorTitle)
        put(appointmentDateTime)
        put(trackingCode)
      }

      val json = JSONObject().apply {
        put("bodyId", BODY_ID_APPOINTMENT)
        put("to", cleanMobile)
        put("args", args)
      }

      val req = Request.Builder()
        .url(API_URL)
        .post(json.toString().toRequestBody(jsonMediaType))
        .build()

      val resp = client.newCall(req).execute()
      val bodyStr = resp.body?.string() ?: ""
      Log.d("Melipayamak", "Appointment SMS Response: code=${resp.code} body=$bodyStr")
      resp.isSuccessful
    } catch (e: Exception) {
      Log.e("Melipayamak", "Error sending appointment SMS", e)
      false
    }
  }

  /**
   * ارسال پیامک لغو نوبت بر اساس پترن ۵۴۲۶۸۳ ملی‌پیامک
   * {0} = نام بیمار
   */
  suspend fun sendCancellationSms(
    mobile: String,
    patientName: String
  ): Boolean = withContext(Dispatchers.IO) {
    val cleanMobile = normalizeMobile(mobile)
    if (cleanMobile.isEmpty()) return@withContext false

    try {
      val args = JSONArray().apply {
        put(patientName.ifBlank { "کاربر گرامی" })
      }

      val json = JSONObject().apply {
        put("bodyId", BODY_ID_CANCELLATION)
        put("to", cleanMobile)
        put("args", args)
      }

      val req = Request.Builder()
        .url(API_URL)
        .post(json.toString().toRequestBody(jsonMediaType))
        .build()

      val resp = client.newCall(req).execute()
      val bodyStr = resp.body?.string() ?: ""
      Log.d("Melipayamak", "Cancellation SMS Response: code=${resp.code} body=$bodyStr")
      resp.isSuccessful
    } catch (e: Exception) {
      Log.e("Melipayamak", "Error sending cancellation SMS", e)
      false
    }
  }

  /**
   * ارسال پیامک ثبت درخواست تسویه بر اساس پترن ۵۳۶۷۱۵ ملی‌پیامک
   * {0} = نام کاربر
   * {1} = مبلغ تسویه
   */
  suspend fun sendWithdrawalSms(
    mobile: String,
    patientName: String,
    amountFormatted: String
  ): Boolean = withContext(Dispatchers.IO) {
    val cleanMobile = normalizeMobile(mobile)
    if (cleanMobile.isEmpty()) return@withContext false

    try {
      val args = JSONArray().apply {
        put(patientName.ifBlank { "کاربر گرامی" })
        put(amountFormatted)
      }

      val json = JSONObject().apply {
        put("bodyId", BODY_ID_WITHDRAWAL)
        put("to", cleanMobile)
        put("args", args)
      }

      val req = Request.Builder()
        .url(API_URL)
        .post(json.toString().toRequestBody(jsonMediaType))
        .build()

      val resp = client.newCall(req).execute()
      val bodyStr = resp.body?.string() ?: ""
      Log.d("Melipayamak", "Withdrawal SMS Response: code=${resp.code} body=$bodyStr")
      resp.isSuccessful
    } catch (e: Exception) {
      Log.e("Melipayamak", "Error sending withdrawal SMS", e)
      false
    }
  }

  private fun normalizeMobile(mobile: String): String {
    var m = mobile.trim().replace(Regex("[^0-9+]"), "")
    if (m.startsWith("+98")) {
      m = "0" + m.substring(3)
    } else if (m.startsWith("98") && m.length == 12) {
      m = "0" + m.substring(2)
    }
    return if (m.matches(Regex("^09\\d{9}$"))) m else ""
  }
}
