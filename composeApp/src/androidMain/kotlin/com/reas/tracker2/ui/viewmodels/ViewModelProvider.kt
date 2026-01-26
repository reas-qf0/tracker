package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.reas.tracker2.TrackerApplication

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
            InfoBottomSheetsViewModel(repository)
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