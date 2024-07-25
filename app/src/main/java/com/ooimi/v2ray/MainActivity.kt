package com.ooimi.v2ray

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.v2ray.ang.V2rayCallback
import com.v2ray.ang.V2raySDK
import com.v2ray.ang.bean.Socks5Config
import com.v2ray.ang.ui.V2RayMainActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                V2raySDK.startSocks5Proxy(this, socks5Config, callback)
            }
        }

    private val socks5Config = Socks5Config().apply {
        host = "59.63.213.95"
        port = 21080
        userName = "wanglin.sun"
        password = "b0j88fba"
        proxyApp = arrayListOf("com.tencent.mobileqq")
    }


    private val callback = object : V2rayCallback {
        override fun start(isSucceed: Boolean, isRun: Boolean) {
            Log.i("===>>>", "isSucceed:${isSucceed},isRun:${isRun}")
        }

        override fun stop() {
            Log.i("===>>>", "stop....")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.startBtn).setOnClickListener(this)
        findViewById<Button>(R.id.stopBtn).setOnClickListener(this)
        findViewById<Button>(R.id.uiBtn).setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.startBtn -> {
                val intent = VpnService.prepare(this)
                if (intent == null) {
                    V2raySDK.startSocks5Proxy(this, socks5Config, callback)
                } else {
                    requestVpnPermission.launch(intent)
                }
            }

            R.id.stopBtn -> {
                V2raySDK.stopSocks5Proxy(this)
            }

            R.id.uiBtn -> {
                startActivity(Intent(this, V2RayMainActivity::class.java))
            }
        }
    }
}