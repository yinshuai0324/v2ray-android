package com.ooimi.v2ray

import android.app.Application
import com.v2ray.ang.V2raySDK

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        V2raySDK.init(this)
    }
}