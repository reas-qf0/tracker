package org.reas.tracker.ui.viewmodels

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.reas.tracker.TrackerApplication

object ViewModelProvider {
    private val repository = TrackerApplication.instance!!.container.repository
    val Factory = viewModelFactory {
        initializer {
            HistoryScreenViewModel(repository)
        }

        initializer {
            ChartsScreenViewModel(repository)
        }

        initializer {
            InfoBottomSheetsViewModel(repository)
        }

        initializer {
            TrackHistoryViewModel(repository)
        }

        initializer {
            ArtistInfoScreenViewModel(repository)
        }
    }
}