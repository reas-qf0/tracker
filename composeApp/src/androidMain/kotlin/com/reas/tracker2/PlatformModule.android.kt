package com.reas.tracker2

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import com.reas.tracker2.android.DataStoreWrapper
import com.reas.tracker2.android.NotificationWrapper
import com.reas.tracker2.database.AppDatabase
import com.reas.tracker2.database.Repository
import com.reas.tracker2.database.RoomRepository
import com.reas.tracker2.database.getDatabaseBuilder
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

actual val platformModule = module {
    single {
        getDatabaseBuilder(get())
    }
    single {
        NotificationWrapper(
            get(),
            get<Context>().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        )
    }

    singleOf(::DataStoreWrapper)
}