package org.reas.tracker.ui.viewmodels

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.reas.tracker.database.CustomImage
import org.reas.tracker.database.Repository
import org.reas.tracker.supabase.CustomImageStorage
import org.reas.tracker.util.DateTimeFormatter.timeMsToString

class InfoBottomSheetsViewModel(private val repository: Repository, private val contentResolver: ContentResolver) : ViewModel() {
    fun artistPlays(artist: String) = repository.getArtistPlays(artist, 0L, Long.MAX_VALUE)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun artistTimePlayed(artist: String) = repository.getArtistTimePlayed(artist, 0L, Long.MAX_VALUE)
        .map { timeMsToString(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun trackPlays(artist: String, track: String, album: String?) = repository.getTrackPlays(artist, track, album, 0L, Long.MAX_VALUE)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun trackTimePlayed(artist: String, track: String, album: String?) = repository.getTrackTimePlayed(artist, track, album, 0L, Long.MAX_VALUE)
        .map { timeMsToString(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun albumPlays(artist: String, album: String) = repository.getAlbumPlays(artist, album, 0L, Long.MAX_VALUE)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun albumTimePlayed(artist: String, album: String) = repository.getAlbumTimePlayed(artist, album, 0L, Long.MAX_VALUE)
        .map { timeMsToString(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun uploadImage(arguments: List<String>, filename: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val remoteFilename = CustomImageStorage.save(filename, contentResolver)
            repository.insertCustomImage(CustomImage(arguments, remoteFilename))
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}