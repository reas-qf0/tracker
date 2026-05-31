package com.reas.tracker2

import com.reas.tracker2.database.AppDatabase
import com.reas.tracker2.database.Repository
import com.reas.tracker2.database.RoomRepository
import com.reas.tracker2.network.*
import com.reas.tracker2.settings.createDataStore
import com.reas.tracker2.shared.EventProcessor
import com.reas.tracker2.shared.EventProcessorAdapter
import com.reas.tracker2.shared.HolePlugger
import com.reas.tracker2.ui.viewmodels.*
import com.reas.tracker2.util.EventProcessorAdapterImpl
import com.reas.tracker2.util.InMemoryLog
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    singleOf(::createDataStore)
    single {
        RoomRepository(AppDatabase.getDatabase(get()))
    } bind Repository::class
    singleOf(::KtorNetworkRepository) bind NetworkRepository::class
    singleOf(::httpClient)
    singleOf(::landscapistInstance)

    singleOf(::EventProcessorAdapterImpl) bind EventProcessorAdapter::class
    singleOf(::EventProcessor)
    singleOf(::TrackerInstanceClient)
    single { HolePlugger() }
    singleOf(::InMemoryLog)

    viewModelOf(::HistoryScreenViewModel)
    viewModelOf(::ChartsScreenViewModel)
    viewModelOf(::InfoBottomSheetsViewModel)
    viewModelOf(::TrackHistoryViewModel)
    viewModelOf(::ArtistInfoScreenViewModel)
    viewModelOf(::AlbumInfoScreenViewModel)
    viewModelOf(::TrackInfoScreenViewModel)
    viewModelOf(::SettingsScreenViewModel)
    viewModelOf(::LoginDialogViewModel)
    viewModelOf(::DebugScreenViewModel)
}