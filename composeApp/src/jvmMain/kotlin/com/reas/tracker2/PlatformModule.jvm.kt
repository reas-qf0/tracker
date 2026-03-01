package com.reas.tracker2

import com.reas.tracker2.database.getDatabaseBuilder
import org.koin.dsl.module

actual val platformModule = module {
    single {
        getDatabaseBuilder()
    }
}