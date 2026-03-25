package com.reas.tracker2

import com.reas.tracker2.database.DatabaseRepository
import com.reas.tracker2.database.EventProcessorAdapterImpl
import com.reas.tracker2.database.Repository
import com.reas.tracker2.database.createSQLiteDatabase
import com.reas.tracker2.shared.EventProcessor
import com.reas.tracker2.shared.EventProcessorAdapter
import com.reas.tracker2.shared.HolePlugger
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val module = module {
    singleOf(::createSQLiteDatabase)
    singleOf(::DatabaseRepository) bind Repository::class

    singleOf(::EventProcessorAdapterImpl) bind EventProcessorAdapter::class
    singleOf(::EventProcessor)
    singleOf(::HolePlugger)
}