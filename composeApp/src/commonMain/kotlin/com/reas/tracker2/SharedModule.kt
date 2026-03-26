package com.reas.tracker2

import com.reas.tracker2.database.AppDatabase
import com.reas.tracker2.database.EventProcessorAdapterImpl
import com.reas.tracker2.database.Repository
import com.reas.tracker2.database.RoomRepository
import com.reas.tracker2.network.*
import com.reas.tracker2.settings.createDataStore
import com.reas.tracker2.shared.EventProcessor
import com.reas.tracker2.shared.EventProcessorAdapter
import com.reas.tracker2.shared.HolePlugger
import com.reas.tracker2.ui.viewmodels.*
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.includes
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

expect val platformModule: Module

fun startKoinMp(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        includes(config)
        modules(sharedModule, platformModule)
    }
}