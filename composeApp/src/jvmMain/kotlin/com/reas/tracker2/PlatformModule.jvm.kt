package com.reas.tracker2

import com.reas.tracker2.database.getDatabaseBuilder
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule = module {
    singleOf(::getDatabaseBuilder)
}