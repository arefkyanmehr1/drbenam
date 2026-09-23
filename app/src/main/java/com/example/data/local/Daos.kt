package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM users WHERE id = 1")
  fun getUserFlow(): Flow<List<UserEntity>>

  @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
  suspend fun getUserOnce(): UserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(user: UserEntity)
}

@Dao
interface AppointmentDao {
  @Query("SELECT * FROM appointments ORDER BY createdAtTimestamp DESC")
  fun getAllAppointments(): Flow<List<AppointmentEntity>>

  @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
  suspend fun getAppointmentById(id: Int): AppointmentEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(appointment: AppointmentEntity): Long

  @Query("UPDATE appointments SET status = :status WHERE id = :id")
  suspend fun updateStatus(id: Int, status: String)

  @Query("DELETE FROM appointments WHERE id = :id")
  suspend fun delete(id: Int)
}

@Dao
interface MedicalServiceDao {
  @Query("SELECT * FROM medical_services ORDER BY id ASC")
  fun getAll(): Flow<List<MedicalServiceEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(services: List<MedicalServiceEntity>)

  @Query("SELECT COUNT(*) FROM medical_services")
  suspend fun getCount(): Int
}

@Dao
interface ClinicDao {
  @Query("SELECT * FROM clinics ORDER BY id ASC")
  fun getAll(): Flow<List<ClinicEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(clinics: List<ClinicEntity>)

  @Query("SELECT COUNT(*) FROM clinics")
  suspend fun getCount(): Int
}

@Dao
interface TreatmentRecordDao {
  @Query("SELECT * FROM treatment_records ORDER BY id DESC")
  fun getAll(): Flow<List<TreatmentRecordEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(record: TreatmentRecordEntity): Long

  @Query("SELECT COUNT(*) FROM treatment_records")
  suspend fun getCount(): Int
}

@Dao
interface WalletDao {
  @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
  fun getAllTransactions(): Flow<List<WalletTxEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(tx: WalletTxEntity): Long

  @Query("SELECT COUNT(*) FROM wallet_transactions")
  suspend fun getCount(): Int
}

@Dao
interface NotificationDao {
  @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
  fun getAll(): Flow<List<NotificationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(notif: NotificationEntity): Long

  @Query("UPDATE notifications SET isRead = 1")
  suspend fun markAllAsRead()

  @Query("DELETE FROM notifications WHERE id = :id")
  suspend fun delete(id: Int)

  @Query("SELECT COUNT(*) FROM notifications")
  suspend fun getCount(): Int
}

@Dao
interface SupportTicketDao {
  @Query("SELECT * FROM support_tickets ORDER BY timestamp DESC")
  fun getAll(): Flow<List<SupportTicketEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(ticket: SupportTicketEntity): Long

  @Query("SELECT COUNT(*) FROM support_tickets")
  suspend fun getCount(): Int
}

@Dao
interface SupportChatDao {
  @Query("SELECT * FROM support_chats ORDER BY timestamp ASC")
  fun getAll(): Flow<List<SupportChatEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(chat: SupportChatEntity): Long

  @Query("SELECT COUNT(*) FROM support_chats")
  suspend fun getCount(): Int
}
