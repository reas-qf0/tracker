package com.reas.tracker2.android

import android.content.Context
import androidx.work.*
import com.reas.tracker2.R
import com.reas.tracker2.network.SyncManager
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.backup_in_progress
import java.util.concurrent.TimeUnit

class SyncWorker(context: Context, params: WorkerParameters):
    CoroutineWorker(context, params), KoinComponent {
    private val notif: NotificationWrapper by inject()
    private val sync: SyncManager by inject()

    override suspend fun doWork(): Result {
        val s = getString(Res.string.backup_in_progress)
        val id = notif.show("Sync Indicator") {
            setContentTitle(s)
            setSmallIcon(R.drawable.ic_stat_name)
        }
        sync.submitFromQueue()
        notif.hide(id)
        return Result.success()
    }

    companion object {
        fun start(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                    "SyncWorker",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    request
                )
        }
    }
}