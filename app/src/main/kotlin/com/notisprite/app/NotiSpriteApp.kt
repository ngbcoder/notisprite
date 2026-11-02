package com.notisprite.app

import android.app.Application
import timber.log.Timber

class NotiSpriteApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
