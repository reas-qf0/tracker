package com.reas.tracker2

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import com.reas.tracker2.android.NotificationWrapper
import com.reas.tracker2.database.getDatabaseBuilder
import com.reas.tracker2.settings.createDataStore
import com.reas.tracker2.settings.producePath
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule = module {
    singleOf(::getDatabaseBuilder)
    single {
        NotificationWrapper(
            get(),
            get<Context>().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        )
    }

    single { createDataStore(producePath(get())) }
}