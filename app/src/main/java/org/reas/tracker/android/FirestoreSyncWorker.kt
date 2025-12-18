package org.reas.tracker.android

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.reas.tracker.AppDataContainer
import org.reas.tracker.R
import org.reas.tracker.TrackerApplication
import java.util.concurrent.TimeUnit

class FirestoreSyncWorker(context: Context, params: WorkerParameters):
    CoroutineWorker(context, params) {
    private val container = TrackerApplication.instance!!.container
    override suspend fun doWork(): Result {
        val notif = NotificationWrapper.show(container.context, "Sync Indicator") {
            setContentTitle("Backup in progress...")
            setSmallIcon(R.drawable.ic_launcher_foreground)
        }
        container.cloudSave.submitBatchEvents()
        NotificationWrapper.hide(notif)
        return Result.success()
    }

    companion object {
        fun start(container: AppDataContainer) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<FirestoreSyncWorker>(1, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager
                .getInstance(container.context)
                .enqueueUniquePeriodicWork(
                    "FirestoreSyncWorker",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    request
                )
        }
    }
}