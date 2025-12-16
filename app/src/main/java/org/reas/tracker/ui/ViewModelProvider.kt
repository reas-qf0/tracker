package org.reas.tracker.ui

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.reas.tracker.TrackerApplication
import org.reas.tracker.ui.screens.HistoryScreenViewModel

object ViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HistoryScreenViewModel(TrackerApplication.instance!!.container.repository)
        }
    }
}