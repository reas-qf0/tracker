package org.reas.tracker

import android.app.Application
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.installations.installations
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.reas.tracker.android.DataStoreWrapper
import org.reas.tracker.database.Repository
import org.reas.tracker.database.AppDatabase
import org.reas.tracker.database.EventProcessor
import org.reas.tracker.database.RoomRepository
import org.reas.tracker.firebase.AuthManager
import org.reas.tracker.firebase.FirestoreCloudSave
import org.reas.tracker.network.NetworkRepository
import org.reas.tracker.network.RetrofitNetworkRepository
import org.reas.tracker.rustore.UpdateManager

class AppDataContainer(val context: Context) {
    val fid = runBlocking { Firebase.installations.id.await() }
    val preferences = DataStoreWrapper(context)
    val authManager = AuthManager(context)
    val repository: Repository by lazy {
        RoomRepository(AppDatabase.getDatabase(context))
    }
    val networkRepository: NetworkRepository by lazy {
        RetrofitNetworkRepository()
    }
    val cloudSave = FirestoreCloudSave(this)
    val eventProcessor = EventProcessor(this)
    val updateManager = UpdateManager(context)
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