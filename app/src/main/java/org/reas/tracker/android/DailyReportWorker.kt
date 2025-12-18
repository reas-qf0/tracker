package org.reas.tracker.android

import android.app.Notification
import android.content.Context
import android.text.Html
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import org.reas.tracker.AppDataContainer
import org.reas.tracker.R
import org.reas.tracker.TrackerApplication
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DailyReportWorker(context: Context, params: WorkerParameters):
    CoroutineWorker(context, params) {
    private val container = TrackerApplication.instance!!.container
    override suspend fun doWork(): Result {
        val repository = container.repository
        val startTime = clock.now().minus(1.days).toEpochMilliseconds()
        val endTime = clock.now().toEpochMilliseconds()
        val artists = repository.getMostPlayedArtists(startTime, endTime, COUNT).first()
        val albums = repository.getMostPlayedAlbums(startTime, endTime, COUNT).first()
        val tracks = repository.getMostPlayedTracks(startTime, endTime, COUNT).first()

        if (artists.isNotEmpty()) {
            NotificationWrapper.show(container.context, "Daily Report") {
                setSmallIcon(R.drawable.ic_launcher_foreground)
                setContentTitle("Here's what you listened to today!")
                setStyle(
                    Notification.BigTextStyle().bigText(
                        Html.fromHtml("""
                            <b>Most played artists:</b><br>${
                                artists.joinToString("<br>")
                            }<br>
                            <b>Most played albums:</b><br>${
                                albums.joinToString("<br>") { "${it.artist} - ${it.album}" }
                            }<br>
                            <b>Most played tracks:</b><br>${
                                tracks.joinToString("<br>") { "${it.artist} - ${it.track}" }
                            }
                        """.trimIndent())
                    )
                )
            }
        }
        return Result.success()
    }

    companion object {
        private const val COUNT = 3
        private val clock = Clock.System

        fun start(container: AppDataContainer) {
            val calendar: Calendar = Calendar.getInstance()
            val nowMillis: Long = calendar.getTimeInMillis()

            calendar.set(Calendar.HOUR_OF_DAY, 2)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1)
            }
            val diff = calendar.getTimeInMillis() - nowMillis
            Log.d("DailyReportWorker", "Work will run in $diff milliseconds")

            val request = PeriodicWorkRequestBuilder<DailyReportWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(diff,TimeUnit.MILLISECONDS)
                .build()
            WorkManager
                .getInstance(container.context)
                .enqueueUniquePeriodicWork(
                    "DailyReportWorker",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    request
                )
        }
    }
}