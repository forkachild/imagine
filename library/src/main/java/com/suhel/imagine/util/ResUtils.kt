package com.suhel.imagine.util

import android.content.Context
import androidx.annotation.RawRes

fun Context.readRawRes(@RawRes resId: Int): String {
    return resources.openRawResource(resId).bufferedReader(Charsets.UTF_8).use { it.readText() }
}