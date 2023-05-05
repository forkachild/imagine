package com.suhel.imagine.editor

import android.app.Application
import com.google.android.material.color.DynamicColors

class ImagineApp: Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

}