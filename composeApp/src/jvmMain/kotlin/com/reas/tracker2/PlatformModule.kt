package com.reas.tracker2

import com.reas.tracker2.database.getDatabaseBuilder
import com.reas.tracker2.util.PlatformDependentPaths
import com.reas.tracker2.util.PlatformDependentPathsDesktop
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val platformModule = module {
    singleOf(::PlatformDependentPathsDesktop) bind PlatformDependentPaths::class
    singleOf(::getDatabaseBuilder)
}