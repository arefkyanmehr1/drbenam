package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val id: Int = 1,
  val name: String = "کاربر گرامی",
  val firstName: String = "",
  val lastName: String = "",
  val phone: String = "09121234567",
  val email: String = "patient@drbenam.com",
  val nationalId: String = "0012345678",
  val birthDate: String = "1368/04/15",
  val gender: String = "male",
  val avatar: String? = null,
  val createdAt: String = "1403/01/10",
  val lastLogin: String = "امروز ساعت ۱۰:۱۵"
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val trackingCode: String,
  val doctorName: String = "دکتر ابراهیم بنام",
  val doctorSpecialty: String = "متخصص قلب و عروق",
  val clinicName: String = "مطب مرکزی دکتر ابراهیم بنام",
  val clinicAddress: String = "تهران، خیابان ولیعصر، نرسیده به توانیر، ساختمان پزشکان، طبقه ۳",
  val clinicPhone: String = "021-88776655",
  val serviceTitle: String,
  val servicePrice: Long,
  val onlinePaid: Long,
  val paymentLabel: String = "پرداخت آنلاین بیعانه",
  val appointmentDate: String, // e.g. 1403/07/28
  val appointmentTime: String, // e.g. 17:30
  val status: String = "confirmed", // "confirmed", "pending", "completed", "cancelled"
  val patientNotes: String = "",
  val adminNotes: String? = null,
  val createdAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "medical_services")
data class MedicalServiceEntity(
  @PrimaryKey val id: Int,
  val title: String,
  val durationMinutes: Int,
  val price: Long,
  val depositPrice: Long,
  val shortDescription: String,
  val visualTone: String = "teal"
)

@Entity(tableName = "clinics")
data class ClinicEntity(
  @PrimaryKey val id: Int,
  val name: String,
  val address: String,
  val phone: String
)

@Entity(tableName = "treatment_records")
data class TreatmentRecordEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val title: String,
  val treatmentDate: String,
  val dateShamsi: String,
  val serviceTitle: String,
  val clinicName: String,
  val doctorName: String = "دکتر ابراهیم بنام",
  val sessions: Int = 1,
  val body: String,
  val attachmentsJson: String = "" // "name|url|size;;name2|url2|size2"
)

@Entity(tableName = "wallet_transactions")
data class WalletTxEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val amount: Long,
  val type: String, // "deposit", "withdraw", "refund", "payment"
  val status: String = "completed",
  val trackingCode: String,
  val description: String,
  val cardOrIban: String? = null,
  val createdAt: String,
  val isInflow: Boolean,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val type: String,
  val title: String,
  val message: String,
  val isRead: Boolean = false,
  val createdAt: String,
  val relativeTime: String,
  val category: String = "system",
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val subject: String,
  val message: String,
  val status: String = "in_progress", // "open", "in_progress", "closed"
  val updatedAt: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_chats")
data class SupportChatEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val senderType: String, // "patient" or "admin"
  val message: String,
  val createdAt: String,
  val timestamp: Long = System.currentTimeMillis()
)
