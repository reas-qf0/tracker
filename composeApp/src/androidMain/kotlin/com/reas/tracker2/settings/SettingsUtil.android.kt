package com.reas.tracker2.settings

import android.content.Context

fun producePath(context: Context): String = context.filesDir.resolve(dataStoreFileName).absolutePath