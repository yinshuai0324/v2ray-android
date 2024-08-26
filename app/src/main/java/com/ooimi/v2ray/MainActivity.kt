package com.ooimi.v2ray

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.v2ray.ang.V2rayCallback
import com.v2ray.ang.V2raySDK
import com.v2ray.ang.bean.Socks5Config
import com.v2ray.ang.ui.V2RayMainActivity
import org.w3c.dom.Text

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                V2raySDK.startSocks5Proxy(this, socks5Config, callback)
            }
        }

    private val socks5Config = Socks5Config()


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


    private fun getInputText(id: Int): String {
        return try {
            findViewById<EditText>(id).text.toString() ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.startBtn -> {
                if (TextUtils.isEmpty(getInputText(R.id.host))) {
                    Toast.makeText(this, "host不能为空", Toast.LENGTH_SHORT).show()
                    return
                }
                if (TextUtils.isEmpty(getInputText(R.id.port))) {
                    Toast.makeText(this, "port不能为空", Toast.LENGTH_SHORT).show()
                    return
                }
                val proxyApp = getInputText(R.id.proxyApp).split(",") ?: arrayListOf()
                socks5Config.host = getInputText(R.id.host)
                socks5Config.port = getInputText(R.id.port)?.toInt() ?: 0
                socks5Config.userName = getInputText(R.id.userName)
                socks5Config.password = getInputText(R.id.password)
                socks5Config.proxyApp = proxyApp
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