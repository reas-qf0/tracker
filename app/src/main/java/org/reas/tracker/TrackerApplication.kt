package org.reas.tracker

import android.app.Application
import android.content.Context
import org.reas.tracker.database.Repository
import org.reas.tracker.database.AppDatabase
import org.reas.tracker.database.RoomRepository
import org.reas.tracker.firebase.AuthManager

class AppDataContainer(private val context: Context) {
    val authManager = AuthManager(context)

    val repository: Repository by lazy {
        RoomRepository(AppDatabase.getDatabase(context))
    }
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