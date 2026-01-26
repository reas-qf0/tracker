package com.reas.tracker2

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform