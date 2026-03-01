package com.reas.tracker2

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoinMp {
            androidLogger()
            androidContext(this@TrackerApplication)
        }
    }
}