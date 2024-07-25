package com.v2ray.ang

import androidx.annotation.Keep

@Keep
interface V2rayCallback {
    fun start(isSucceed: Boolean, isRun: Boolean)
    fun stop()
}