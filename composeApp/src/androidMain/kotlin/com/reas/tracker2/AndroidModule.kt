package com.reas.tracker2

import com.reas.tracker2.android.DataStoreWrapper
import com.reas.tracker2.database.AppDatabase
import com.reas.tracker2.database.EventProcessor
import com.reas.tracker2.database.Repository
import com.reas.tracker2.database.RoomRepository
import com.reas.tracker2.network.NetworkRepository
import com.reas.tracker2.network.RetrofitNetworkRepository
import com.reas.tracker2.ui.viewmodels.AlbumInfoScreenViewModel
import com.reas.tracker2.ui.viewmodels.ArtistInfoScreenViewModel
import com.reas.tracker2.ui.viewmodels.ChartsScreenViewModel
import com.reas.tracker2.ui.viewmodels.HistoryScreenViewModel
import com.reas.tracker2.ui.viewmodels.InfoBottomSheetsViewModel
import com.reas.tracker2.ui.viewmodels.SettingsScreenViewModel
import com.reas.tracker2.ui.viewmodels.TrackHistoryViewModel
import com.reas.tracker2.ui.viewmodels.TrackInfoScreenViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val module = module {
    factoryOf(::DataStoreWrapper)
    factory {
        RoomRepository(AppDatabase.getDatabase(get()))
    } bind Repository::class
    factory {
        RetrofitNetworkRepository()
    } bind NetworkRepository::class
    factoryOf(::EventProcessor)

    viewModelOf(::HistoryScreenViewModel)
    viewModelOf(::ChartsScreenViewModel)
    viewModelOf(::InfoBottomSheetsViewModel)
    viewModelOf(::TrackHistoryViewModel)
    viewModelOf(::ArtistInfoScreenViewModel)
    viewModelOf(::AlbumInfoScreenViewModel)
    viewModelOf(::TrackInfoScreenViewModel)
    viewModelOf(::SettingsScreenViewModel)
}