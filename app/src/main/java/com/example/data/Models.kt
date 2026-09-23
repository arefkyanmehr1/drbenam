package com.example.data

data class UserProfile(
  val id: Int = 1,
  val name: String = "کاربر گرامی",
  val firstName: String = "",
  val lastName: String = "",
  val phone: String = "09123456789",
  val email: String = "",
  val nationalId: String = "",
  val birthDate: String = "",
  val gender: String = "unknown",
  val avatar: String? = null,
  val createdAt: String = "1403/01/15",
  val lastLogin: String = "همین الان"
)

data class AppointmentItem(
  val id: Int,
  val trackingCode: String,
  val doctorName: String = "دکتر ابراهیم بنام",
  val doctorSpecialty: String = "متخصص قلب و عروق",
  val clinicName: String = "مطب مرکزی دکتر بنام",
  val clinicAddress: String = "خیابان اصلی، ساختمان پزشکان، طبقه ۳",
  val clinicPhone: String = "021-88888888",
  val serviceTitle: String,
  val servicePrice: Long,
  val onlinePaid: Long,
  val paymentLabel: String = "تسویه کامل",
  val appointmentDate: String, // e.g. 1403/07/12
  val appointmentTime: String, // e.g. 17:30
  val status: String, // "confirmed", "pending", "completed", "cancelled"
  val patientNotes: String = "",
  val adminNotes: String? = null
)

data class MedicalService(
  val id: Int,
  val title: String,
  val durationMinutes: Int,
  val price: Long,
  val depositPrice: Long,
  val shortDescription: String,
  val visualTone: String = "teal"
)

data class ClinicItem(
  val id: Int,
  val name: String,
  val address: String,
  val phone: String
)

data class TreatmentRecord(
  val id: Int,
  val title: String,
  val treatmentDate: String,
  val dateShamsi: String,
  val serviceTitle: String,
  val clinicName: String,
  val doctorName: String,
  val sessions: Int,
  val body: String,
  val attachments: List<AttachmentFile> = emptyList()
)

data class AttachmentFile(
  val name: String,
  val url: String,
  val size: Long
)

data class NotificationMessage(
  val id: Int,
  val type: String,
  val title: String,
  val message: String,
  val isRead: Boolean,
  val createdAt: String,
  val relativeTime: String,
  val category: String = "system"
)

data class WalletTx(
  val id: Int,
  val amount: Long,
  val type: String, // "deposit", "withdraw", "refund", "payment"
  val status: String, // "completed", "pending", "rejected", "refunded"
  val trackingCode: String,
  val description: String,
  val cardOrIban: String? = null,
  val createdAt: String,
  val isInflow: Boolean
)

data class SupportTicketItem(
  val id: Int,
  val subject: String,
  val message: String,
  val status: String, // "open", "in_progress", "closed"
  val updatedAt: String
)

data class SupportChatMessageItem(
  val id: Int,
  val senderType: String, // "patient" or "admin"
  val message: String,
  val createdAt: String
)

data class IncomingSms(
  val id: Long = System.currentTimeMillis(),
  val sender: String = "مطب دکتر ابراهیم بنام",
  val message: String,
  val code: String? = null,
  val timestamp: Long = System.currentTimeMillis()
)

enum class LoginAuthStep {
  MOBILE_INPUT,
  OTP_VERIFY,
  PASSWORD_LOGIN,
  REGISTER_NAME,
  SET_PASSWORD,
  FORGOT_PASSWORD_MOBILE,
  FORGOT_PASSWORD_OTP,
  RESET_PASSWORD
}
