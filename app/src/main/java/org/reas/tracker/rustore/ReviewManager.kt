package org.reas.tracker.rustore

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.rustore.sdk.review.RuStoreReviewManagerFactory

sealed class ReviewStatus {
    class Requesting : ReviewStatus()
    class RequestError(val e: Throwable) : ReviewStatus()
    class Launching : ReviewStatus()
    class LaunchError(val e: Throwable) : ReviewStatus()
    class Complete : ReviewStatus()
}

class ReviewManager(context: Context) {
    private val rustoreUpdate = RuStoreReviewManagerFactory.create(context)

    fun review() : Flow<ReviewStatus> = flow {
        emit(ReviewStatus.Requesting())
        try {
            val reviewInfo = rustoreUpdate.requestReviewFlow().await()
            emit(ReviewStatus.Launching())
            try {
                rustoreUpdate.launchReviewFlow(reviewInfo).await()
                emit(ReviewStatus.Complete())
            } catch (e: Throwable) {
                Log.e(TAG, "launchReviewFlow failed", e)
                emit(ReviewStatus.LaunchError(e))
            }
        } catch (e: Throwable) {
            Log.e(TAG, "requestReviewFlow failed", e)
            emit(ReviewStatus.RequestError(e))
        }
    }

    companion object {
        private const val TAG = "ReviewManager"
    }
}