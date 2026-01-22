package org.reas.tracker.rustore

import android.app.Activity
import android.content.Context
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import ru.rustore.sdk.appupdate.listener.InstallStateUpdateListener
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.AppUpdateInfo
import ru.rustore.sdk.appupdate.model.AppUpdateOptions
import ru.rustore.sdk.appupdate.model.AppUpdateType
import ru.rustore.sdk.appupdate.model.InstallState
import ru.rustore.sdk.appupdate.model.InstallStatus
import ru.rustore.sdk.appupdate.model.UpdateAvailability
import ru.rustore.sdk.core.exception.RuStoreException

sealed class CheckStatus {
    class Checking : CheckStatus()
    class Latest : CheckStatus()
    data class Available(val updateInfo: AppUpdateInfo) : CheckStatus()
    class Failed : CheckStatus()
}

sealed class UpdateStatus {
    data class Downloading(val progress: Float): UpdateStatus()
    class Failed(): UpdateStatus()
    class Ready(): UpdateStatus()
    class Canceled(): UpdateStatus()
}

class UpdateManager(context: Context) {
    private val rustoreUpdateManager = RuStoreAppUpdateManagerFactory.create(context)

    fun checkForUpdates(): Flow<CheckStatus> = flow {
        emit(CheckStatus.Checking())
        try {
            val appUpdateInfo = rustoreUpdateManager.getAppUpdateInfo().await()
            when (appUpdateInfo.updateAvailability) {
                UpdateAvailability.UPDATE_AVAILABLE ->
                    emit(CheckStatus.Available(appUpdateInfo))
                UpdateAvailability.UPDATE_NOT_AVAILABLE ->
                    emit(CheckStatus.Latest())
            }
        } catch (e: RuStoreException) {
            Log.e(TAG, "getAppUpdateInfo error", e)
            emit(CheckStatus.Failed())
        }
    }

    fun update(updateInfo: AppUpdateInfo): Flow<UpdateStatus> = callbackFlow {
        fun sendFromListener(x: UpdateStatus) {
            trySendBlocking(x).onFailure { e ->
                Log.e(TAG, "couldn't send value to ui flow", e)
            }
        }

        val listener = object : InstallStateUpdateListener {
            override fun onStateUpdated(state: InstallState) {
                when (state.installStatus) {
                    InstallStatus.DOWNLOADED -> {
                        sendFromListener(UpdateStatus.Ready())
                        rustoreUpdateManager.completeUpdate(
                            AppUpdateOptions.Builder()
                                .appUpdateType(AppUpdateType.FLEXIBLE)
                                .build()
                        ).addOnFailureListener { throwable ->
                            Log.e(TAG, "update error", throwable)
                            sendFromListener(UpdateStatus.Failed())
                            close()
                        }
                    }
                    InstallStatus.DOWNLOADING -> {
                        val totalBytes = state.totalBytesToDownload
                        val bytesDownloaded = state.bytesDownloaded
                        val progress = bytesDownloaded.toFloat() / totalBytes
                        sendFromListener(UpdateStatus.Downloading(progress))
                    }
                    InstallStatus.FAILED -> {
                        Log.e(TAG, "Downloading error")
                        sendFromListener(UpdateStatus.Failed())
                        close()
                    }
                }
            }
        }

        rustoreUpdateManager.registerListener(listener)
        rustoreUpdateManager.startUpdateFlow(
            updateInfo,
            AppUpdateOptions.Builder()
                .appUpdateType(AppUpdateType.FLEXIBLE)
                .build()
        ).addOnSuccessListener { resultCode ->
            if (resultCode == Activity.RESULT_CANCELED) {
                sendFromListener(UpdateStatus.Canceled())
                close()
            }
        }
        .addOnFailureListener { throwable ->
            Log.e(TAG, "startUpdateFlow error", throwable)
            close(throwable)
        }

        awaitClose { rustoreUpdateManager.unregisterListener(listener) }
    }

    companion object {
        private const val TAG = "UpdateManager"
    }
}