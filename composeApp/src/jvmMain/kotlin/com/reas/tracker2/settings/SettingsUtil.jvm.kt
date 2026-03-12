package com.reas.tracker2.settings

import java.io.File

fun producePath(): String = File(/*System.getProperty("java.io.tmpdir"), */dataStoreFileName).absolutePath