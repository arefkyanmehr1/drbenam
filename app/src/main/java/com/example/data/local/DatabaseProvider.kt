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
        "drbenam_official_db.db"
      )
        .fallbackToDestructiveMigration()
        .build()
      INSTANCE = instance
      instance
    }
  }

  suspend fun populateInitialDataIfEmpty(database: AppDatabase) {
    if (database.medicalServiceDao().getCount() > 0) return

    // ۱. کاربر واقعی سیستم بر اساس دامپ دیتابیس drbena_drbenam
    database.userDao().insertOrUpdate(
      UserEntity(
        id = 1,
        name = "عارف کیانمهر",
        firstName = "عارف",
        lastName = "کیانمهر",
        phone = "09935179549",
        email = "patient@drbenam.com",
        nationalId = "6000123269",
        birthDate = "1372/06/15",
        gender = "male",
        createdAt = "1405/06/10",
        lastLogin = "همین الان"
      )
    )

    // ۲. خدمات واقعی مطب بر اساس جدول services دیتابیس سایت drbenam.com
    database.medicalServiceDao().insertAll(
      listOf(
        MedicalServiceEntity(
          id = 1,
          title = "اکوکاردیوگرافی",
          durationMinutes = 15,
          price = 2300000L,
          depositPrice = 0L,
          shortDescription = "لباس راحت بپوشید - آقایان موهای قفسه سینه خود را بتراشید",
          visualTone = "teal"
        ),
        MedicalServiceEntity(
          id = 2,
          title = "ویزیت",
          durationMinutes = 15,
          price = 500000L,
          depositPrice = 0L,
          shortDescription = "ویزیت تخصصی پزشک متخصص قلب و عروق",
          visualTone = "sky"
        ),
        MedicalServiceEntity(
          id = 3,
          title = "نوارقلب",
          durationMinutes = 5,
          price = 415000L,
          depositPrice = 0L,
          shortDescription = "لباس راحت بپوشید - موهای قفسه سینه خود را بتراشید",
          visualTone = "coral"
        ),
        MedicalServiceEntity(
          id = 4,
          title = "هولتر فشارخون",
          durationMinutes = 10,
          price = 1700000L,
          depositPrice = 0L,
          shortDescription = "لباس راحت بپوشید - ۲۴ ساعت دستگاه هولتر فشارخون همراه شما خواهد بود",
          visualTone = "blue"
        ),
        MedicalServiceEntity(
          id = 5,
          title = "هولتر ریتم",
          durationMinutes = 20,
          price = 1700000L,
          depositPrice = 0L,
          shortDescription = "موهای قفسه سینه را بتراشید - قبل از مراجعه حمام کنید - هولتر ریتم به تشخیص پزشک از ۲۴ تا ۹۶ ساعت همراه شما خواهد بود",
          visualTone = "violet"
        ),
        MedicalServiceEntity(
          id = 6,
          title = "مشاوره پیش از عمل جراحی",
          durationMinutes = 20,
          price = 1200000L,
          depositPrice = 0L,
          shortDescription = "مدارک و داروهای مصرفی خود را به همراه بیاورید",
          visualTone = "teal"
        )
      )
    )

    // ۳. مطب واقعی بر اساس جدول clinics دیتابیس سایت
    database.clinicDao().insertAll(
      listOf(
        ClinicEntity(
          id = 1,
          name = "مطب مرکزی دکتر ابراهیم بنام",
          address = "تهران، بولوار کشاورز، تقاطع کارگر شمالی، برج سبز، طبقه ۶",
          phone = "09376246647"
        )
      )
    )

    // ۴. نوبت‌های واقعی بیمار بر اساس جدول appointments دیتابیس سایت
    database.appointmentDao().insert(
      AppointmentEntity(
        id = 42,
        trackingCode = "2009039",
        doctorName = "دکتر ابراهیم بنام",
        doctorSpecialty = "متخصص قلب و عروق",
        clinicName = "مطب مرکزی دکتر ابراهیم بنام",
        clinicAddress = "تهران، بولوار کشاورز، تقاطع کارگر شمالی، برج سبز، طبقه ۶",
        clinicPhone = "09376246647",
        serviceTitle = "اکوکاردیوگرافی",
        servicePrice = 2300000L,
        onlinePaid = 2300000L,
        paymentLabel = "تسویه کامل آنلاین",
        appointmentDate = "1405/07/02",
        appointmentTime = "09:30",
        status = "cancelled",
        patientNotes = "پرداخت آنلاین (تسویه کامل): ۲,۳۰۰,۰۰۰ تومان [علت لغو: تغییر برنامه کاری یا سفر]",
        adminNotes = null,
        createdAtTimestamp = System.currentTimeMillis() - 86400000L
      )
    )

    database.appointmentDao().insert(
      AppointmentEntity(
        id = 43,
        trackingCode = "2665722",
        doctorName = "دکتر ابراهیم بنام",
        doctorSpecialty = "متخصص قلب و عروق",
        clinicName = "مطب مرکزی دکتر ابراهیم بنام",
        clinicAddress = "تهران، بولوار کشاورز، تقاطع کارگر شمالی، برج سبز، طبقه ۶",
        clinicPhone = "09376246647",
        serviceTitle = "ویزیت",
        servicePrice = 500000L,
        onlinePaid = 500000L,
        paymentLabel = "تسویه کامل آنلاین",
        appointmentDate = "1405/07/02",
        appointmentTime = "11:00",
        status = "cancelled",
        patientNotes = "پرداخت آنلاین (تسویه کامل): ۵۰۰,۰۰۰ تومان [علت لغو: تغییر برنامه کاری یا سفر]",
        adminNotes = null,
        createdAtTimestamp = System.currentTimeMillis() - 84000000L
      )
    )

    database.appointmentDao().insert(
      AppointmentEntity(
        id = 44,
        trackingCode = "7069016",
        doctorName = "دکتر ابراهیم بنام",
        doctorSpecialty = "متخصص قلب و عروق",
        clinicName = "مطب مرکزی دکتر ابراهیم بنام",
        clinicAddress = "تهران، بولوار کشاورز، تقاطع کارگر شمالی، برج سبز، طبقه ۶",
        clinicPhone = "09376246647",
        serviceTitle = "نوارقلب",
        servicePrice = 415000L,
        onlinePaid = 415000L,
        paymentLabel = "تسویه کامل آنلاین",
        appointmentDate = "1405/07/02",
        appointmentTime = "10:00",
        status = "cancelled",
        patientNotes = "پرداخت آنلاین (تسویه کامل): ۴۱۵,۰۰۰ تومان [علت لغو: تغییر برنامه کاری یا سفر]",
        adminNotes = null,
        createdAtTimestamp = System.currentTimeMillis() - 80000000L
      )
    )

    database.appointmentDao().insert(
      AppointmentEntity(
        id = 46,
        trackingCode = "9074924",
        doctorName = "دکتر ابراهیم بنام",
        doctorSpecialty = "متخصص قلب و عروق",
        clinicName = "مطب مرکزی دکتر ابراهیم بنام",
        clinicAddress = "تهران، بولوار کشاورز، تقاطع کارگر شمالی، برج سبز، طبقه ۶",
        clinicPhone = "09376246647",
        serviceTitle = "هولتر فشارخون",
        servicePrice = 1700000L,
        onlinePaid = 1700000L,
        paymentLabel = "تسویه کامل آنلاین",
        appointmentDate = "1405/07/02",
        appointmentTime = "10:30",
        status = "cancelled",
        patientNotes = "پرداخت آنلاین (تسویه کامل): ۱,۷۰۰,۰۰۰ تومان [علت لغو: تغییر برنامه کاری یا سفر]",
        adminNotes = null,
        createdAtTimestamp = System.currentTimeMillis() - 76000000L
      )
    )

    database.appointmentDao().insert(
      AppointmentEntity(
        id = 47,
        trackingCode = "6202705",
        doctorName = "دکتر ابراهیم بنام",
        doctorSpecialty = "متخصص قلب و عروق",
        clinicName = "مطب مرکزی دکتر ابراهیم بنام",
        clinicAddress = "تهران، بولوار کشاورز، تقاطع کارگر شمالی، برج سبز، طبقه ۶",
        clinicPhone = "09376246647",
        serviceTitle = "ویزیت",
        servicePrice = 500000L,
        onlinePaid = 500000L,
        paymentLabel = "تسویه کامل آنلاین",
        appointmentDate = "1405/07/02",
        appointmentTime = "13:30",
        status = "cancelled",
        patientNotes = "پرداخت آنلاین (تسویه کامل): ۵۰۰,۰۰۰ تومان [علت لغو: عدم امکان حضور در ساعت مقرر]",
        adminNotes = null,
        createdAtTimestamp = System.currentTimeMillis() - 72000000L
      )
    )

    // ۵. پرونده‌های واقعی درمان بر اساس جدول treatments دیتابیس سایت
    database.treatmentRecordDao().insert(
      TreatmentRecordEntity(
        id = 1,
        title = "ویزیت قلب با دستگاه",
        treatmentDate = "2026-09-03",
        dateShamsi = "۱۳ شهریور ۱۴۰۵",
        serviceTitle = "ویزیت",
        clinicName = "مطب مرکزی دکتر ابراهیم بنام",
        doctorName = "دکتر ابراهیم بنام",
        sessions = 1,
        body = "بیمار دارای بیماری میباشد و روند درمان با تجویز دارو ادامه دارد.",
        attachmentsJson = "مدارک - پردیس کیانمهر - ویزیت قلب با دستگاه.pdf|https://drbenam.com/uploads/medical_docs/med_1788546690_پردیس_کیانمهر_ویزیت_قلب_با_دستگاه_1_4c0458.pdf|1290042"
      )
    )

    database.treatmentRecordDao().insert(
      TreatmentRecordEntity(
        id = 2,
        title = "ویزیت قلب",
        treatmentDate = "2026-09-03",
        dateShamsi = "۱۳ شهریور ۱۴۰۵",
        serviceTitle = "ویزیت",
        clinicName = "مطب مرکزی دکتر ابراهیم بنام",
        doctorName = "دکتر ابراهیم بنام",
        sessions = 1,
        body = "بیمار مشکل قلبی دارد و روند معاینه و آزمایش‌های بالینی جهت بررسی دقیق‌تر تجویز شد.",
        attachmentsJson = "پرونده_پزشکی.zip|https://drbenam.com/uploads/medical_docs/med_3_1788514341_2c8b2ae4a028.zip|6731"
      )
    )

    // ۶. تراکنش‌های واقعی کیف پول بر اساس جدول wallet_transactions دیتابیس سایت
    database.walletDao().insertTransaction(
      WalletTxEntity(
        id = 23,
        amount = 1794000L,
        type = "refund",
        status = "completed",
        trackingCode = "REF-715516",
        description = "درخواست استرداد وجه نوبت لغو شده (اکوکاردیوگرافی) با کد پیگیری 2009039 - در صف بررسی مدیریت و کسر درصد جریمه (جریمه کسر شده: 22٪)",
        createdAt = "1405/07/02 • ۱۱:۳۵",
        isInflow = true,
        timestamp = System.currentTimeMillis() - 86000000L
      )
    )

    database.walletDao().insertTransaction(
      WalletTxEntity(
        id = 24,
        amount = 365000L,
        type = "refund",
        status = "completed",
        trackingCode = "REF-609423",
        description = "درخواست استرداد وجه نوبت لغو شده (ویزیت) با کد پیگیری 2665722 - در صف بررسی مدیریت و کسر درصد جریمه (جریمه کسر شده: 27٪)",
        createdAt = "1405/07/02 • ۱۱:۴۲",
        isInflow = true,
        timestamp = System.currentTimeMillis() - 84000000L
      )
    )

    database.walletDao().insertTransaction(
      WalletTxEntity(
        id = 25,
        amount = 340300L,
        type = "refund",
        status = "completed",
        trackingCode = "REF-104525",
        description = "درخواست استرداد وجه نوبت لغو شده (نوارقلب) با کد پیگیری 7069016 - در صف بررسی مدیریت و کسر درصد جریمه (جریمه کسر شده: 18٪)",
        createdAt = "1405/07/02 • ۱۱:۵۳",
        isInflow = true,
        timestamp = System.currentTimeMillis() - 80000000L
      )
    )

    database.walletDao().insertTransaction(
      WalletTxEntity(
        id = 26,
        amount = 1360000L,
        type = "refund",
        status = "completed",
        trackingCode = "REF-667789",
        description = "درخواست استرداد وجه نوبت لغو شده (هولتر فشارخون) با کد پیگیری 9074924 - در صف بررسی مدیریت و کسر درصد جریمه (جریمه کسر شده: 20٪)",
        createdAt = "1405/07/02 • ۱۲:۰۴",
        isInflow = true,
        timestamp = System.currentTimeMillis() - 76000000L
      )
    )

    database.walletDao().insertTransaction(
      WalletTxEntity(
        id = 28,
        amount = 500000L,
        type = "withdraw",
        status = "completed",
        trackingCode = "WDR-20260923-D429423A",
        description = "درخواست تسویه بانکی (واریز شد)",
        cardOrIban = "6219861967954891",
        createdAt = "1405/07/02 • ۱۲:۴۲",
        isInflow = false,
        timestamp = System.currentTimeMillis() - 70000000L
      )
    )

    database.walletDao().insertTransaction(
      WalletTxEntity(
        id = 30,
        amount = 2500000L,
        type = "withdraw",
        status = "completed",
        trackingCode = "WDR-20260923-8704940A",
        description = "درخواست تسویه بانکی (واریز گردید)",
        cardOrIban = "6219861967954891",
        createdAt = "1405/07/02 • ۱۲:۵۶",
        isInflow = false,
        timestamp = System.currentTimeMillis() - 65000000L
      )
    )

    database.walletDao().insertTransaction(
      WalletTxEntity(
        id = 31,
        amount = 500000L,
        type = "withdraw",
        status = "completed",
        trackingCode = "839821",
        description = "درخواست تسویه بانکی (واریز گردید)",
        cardOrIban = "6219861967954891",
        createdAt = "1405/07/02 • ۱۳:۰۱",
        isInflow = false,
        timestamp = System.currentTimeMillis() - 60000000L
      )
    )

    // ۷. اعلان‌های واقعی کاربر بر اساس جدول notifications دیتابیس سایت
    database.notificationDao().insert(
      NotificationEntity(
        id = 122,
        type = "APPOINTMENT_CONFIRMED",
        title = "تأیید قطعی نوبت",
        message = "نوبت شما برای پنج‌شنبه، ۲ مهر ۱۴۰۵ ساعت 09:30 با کد رهگیری 2009039 با موفقیت ثبت شد.",
        isRead = true,
        createdAt = "1405/07/02",
        relativeTime = "امروز",
        category = "appointment"
      )
    )

    database.notificationDao().insert(
      NotificationEntity(
        id = 123,
        type = "APPOINTMENT_CANCELLED",
        title = "لغو نوبت و ثبت درخواست استرداد",
        message = "نوبت شما برای پنج‌شنبه، ۲ مهر ۱۴۰۵ ساعت ۰۹:۳۰ لغو گردید. درخواست استرداد مبلغ ۲,۳۰۰,۰۰۰ تومان در صف بررسی مدیریت قرار گرفت.",
        isRead = true,
        createdAt = "1405/07/02",
        relativeTime = "امروز",
        category = "appointment"
      )
    )

    database.notificationDao().insert(
      NotificationEntity(
        id = 124,
        type = "WALLET_REFUND",
        title = "واریز استرداد به کیف پول",
        message = "مبلغ ۱,۷۹۴,۰۰۰ تومان بابت استرداد وجه به کیف پول شما واریز گردید.",
        isRead = false,
        createdAt = "1405/07/02",
        relativeTime = "چند ساعت پیش",
        category = "wallet"
      )
    )

    database.notificationDao().insert(
      NotificationEntity(
        id = 143,
        type = "WALLET_PAYOUT",
        title = "واریز موفق وجه به حساب",
        message = "درخواست برداشت وجه به مبلغ ۵۰۰,۰۰۰ تومان به شماره شبا/کارت ثبت شده واریز گردید.",
        isRead = false,
        createdAt = "1405/07/02",
        relativeTime = "لحظاتی پیش",
        category = "wallet"
      )
    )

    // ۸. تیکت و پیام‌های پشتیبانی
    database.supportTicketDao().insert(
      SupportTicketEntity(
        id = 1,
        subject = "راهنمایی درباره جواب آزمایش و اکوکاردیوگرافی",
        message = "سلام آقای دکتر، آیا قبل از انجام تست ورزش نیاز به قطع مصرف دارو هست؟",
        status = "in_progress",
        updatedAt = "امروز"
      )
    )

    database.supportChatDao().insert(
      SupportChatEntity(
        id = 1,
        senderType = "admin",
        message = "سلام و احترام. به پشتیبانی آنلاین مطب دکتر ابراهیم بنام خوش آمدید. چگونه می‌توانیم کمکتان کنیم؟",
        createdAt = "۱۰:۰۰"
      )
    )
  }
}
