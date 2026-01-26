package com.reas.tracker2

import android.app.Application
import android.content.Context
import com.reas.tracker2.android.DataStoreWrapper
import com.reas.tracker2.database.Repository
import com.reas.tracker2.database.AppDatabase
import com.reas.tracker2.database.EventProcessor
import com.reas.tracker2.database.RoomRepository
import com.reas.tracker2.network.NetworkRepository
import com.reas.tracker2.network.RetrofitNetworkRepository

class AppDataContainer(val context: Context) {
    val preferences = DataStoreWrapper(context)
    val repository: Repository by lazy {
        RoomRepository(AppDatabase.getDatabase(context))
    }
    val networkRepository: NetworkRepository by lazy {
        RetrofitNetworkRepository()
    }
    val eventProcessor = EventProcessor(this)
}

class TrackerApplication : Application() {
    init {
        instance = this
    }

    companion object {
        var instance: TrackerApplication? = null
            private set
    }
    lateinit var container: AppDataContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}