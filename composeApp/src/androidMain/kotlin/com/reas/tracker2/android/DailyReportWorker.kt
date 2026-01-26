package com.reas.tracker2.android

import android.app.Notification
import android.content.Context
import android.text.Html
import android.util.Log
import androidx.paging.PagingSource
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.reas.tracker2.AppDataContainer
import com.reas.tracker2.R
import com.reas.tracker2.TrackerApplication
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
        val artists = (repository.getMostPlayedArtists(startTime, endTime).load(
            PagingSource.LoadParams.Refresh(0, COUNT, false)
        ) as PagingSource.LoadResult.Page).data
        val albums = (repository.getMostPlayedAlbums(startTime, endTime).load(
            PagingSource.LoadParams.Refresh(0, COUNT, false)
        ) as PagingSource.LoadResult.Page).data
        val tracks = (repository.getMostPlayedTracks(startTime, endTime).load(
            PagingSource.LoadParams.Refresh(0, COUNT, false)
        ) as PagingSource.LoadResult.Page).data

        if (artists.isNotEmpty()) {
            NotificationWrapper.show(container.context, "Daily Report") {
                setSmallIcon(R.drawable.ic_stat_name)
                setContentTitle(container.context.getString(R.string.daily_report_header))
                setStyle(
                    Notification.BigTextStyle().bigText(
                        Html.fromHtml("""
                            <b>${
                                container.context.getString(R.string.daily_report_artists)
                            }</b><br>${
                                artists.joinToString("<br>") { it.artist }
                            }<br>
                            <b>${
                                container.context.getString(R.string.daily_report_albums)
                            }</b><br>${
                                albums.joinToString("<br>") { "${it.artist} - ${it.album}" }
                            }<br>
                            <b>${
                                container.context.getString(R.string.daily_report_tracks)
                            }</b><br>${
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