package org.reas.tracker

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import org.reas.tracker.firebase.AuthManager

class AppDataContainer(private val context: Context) {
    val authManager = AuthManager(context)
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