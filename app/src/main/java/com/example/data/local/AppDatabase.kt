package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
  entities = [
    UserEntity::class,
    AppointmentEntity::class,
    MedicalServiceEntity::class,
    ClinicEntity::class,
    TreatmentRecordEntity::class,
    WalletTxEntity::class,
    NotificationEntity::class,
    SupportTicketEntity::class,
    SupportChatEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun appointmentDao(): AppointmentDao
  abstract fun medicalServiceDao(): MedicalServiceDao
  abstract fun clinicDao(): ClinicDao
  abstract fun treatmentRecordDao(): TreatmentRecordDao
  abstract fun walletDao(): WalletDao
  abstract fun notificationDao(): NotificationDao
  abstract fun supportTicketDao(): SupportTicketDao
  abstract fun supportChatDao(): SupportChatDao
}
