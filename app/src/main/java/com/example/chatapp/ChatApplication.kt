package com.example.chatapp

import android.app.Application
import android.graphics.Bitmap
import timber.log.Timber

class ChatApplication :Application(){
    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
    }
}