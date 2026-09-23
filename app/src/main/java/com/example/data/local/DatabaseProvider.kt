package com.example.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
  @Volatile
  private var INSTANCE: AppDatabase? = null

  fun getDatabase(context: Context): AppDatabase {
    return INSTANCE ?: synchronized(this) {
      val instance = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "drbenam_server_cache_v2.db"
      )
        .fallbackToDestructiveMigration()
        .build()
      INSTANCE = instance
      instance
    }
  }

  /**
   * Deliberately empty.
   *
   * The Android app must never seed patient data, appointments, wallet balances,
   * treatments, notifications or time slots. MySQL on drbenam.com is the source
   * of truth; Room is only a cache for future offline-safe features.
   */
  suspend fun populateInitialDataIfEmpty(database: AppDatabase) = Unit
}
