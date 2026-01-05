package org.reas.tracker.ui.viewmodels

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.reas.tracker.TrackerApplication

object ViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HistoryScreenViewModel(TrackerApplication.instance!!.container.repository)
        }

        initializer {
            ChartsScreenViewModel(TrackerApplication.instance!!.container.repository)
        }

        initializer {
            InfoBottomSheetsViewModel(TrackerApplication.instance!!.container.repository)
        }
    }
}