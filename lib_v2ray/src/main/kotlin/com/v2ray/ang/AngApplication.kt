package com.v2ray.ang

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.tencent.mmkv.MMKV
import com.v2ray.ang.util.Utils

class AngApplication  {
    companion object {
        //const val PREF_LAST_VERSION = "pref_last_version"
        lateinit var application: Application
    }

}
