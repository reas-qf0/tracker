package org.reas.tracker.ui.viewmodels

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.reas.tracker.TrackerApplication

object ViewModelProvider {
    private val repository = TrackerApplication.instance!!.container.repository
    private val networkRepository = TrackerApplication.instance!!.container.networkRepository
    val Factory = viewModelFactory {
        initializer {
            HistoryScreenViewModel(repository, networkRepository)
        }

        initializer {
            ChartsScreenViewModel(repository, networkRepository)
        }

        initializer {
            InfoBottomSheetsViewModel(repository, TrackerApplication.instance!!.contentResolver)
        }

        initializer {
            TrackHistoryViewModel(repository, networkRepository)
        }

        initializer {
            ArtistInfoScreenViewModel(repository)
        }

        initializer {
            AlbumInfoScreenViewModel(repository, networkRepository)
        }

        initializer {
            TrackInfoScreenViewModel(repository)
        }
    }
}