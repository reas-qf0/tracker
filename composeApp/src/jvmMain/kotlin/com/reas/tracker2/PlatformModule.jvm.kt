package com.reas.tracker2

import com.reas.tracker2.database.getDatabaseBuilder
import com.reas.tracker2.settings.createDataStore
import com.reas.tracker2.settings.producePath
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule = module {
    singleOf(::getDatabaseBuilder)
    single { createDataStore(producePath()) }
}