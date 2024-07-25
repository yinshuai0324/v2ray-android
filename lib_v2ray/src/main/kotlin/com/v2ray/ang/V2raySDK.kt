package com.v2ray.ang

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import com.tencent.mmkv.MMKV
import com.v2ray.ang.bean.Socks5Config
import com.v2ray.ang.dto.EConfigType
import com.v2ray.ang.dto.ServerConfig
import com.v2ray.ang.dto.V2rayConfig
import com.v2ray.ang.extension.safeResume
import com.v2ray.ang.extension.toast
import com.v2ray.ang.service.V2RayServiceManager
import com.v2ray.ang.ui.V2RayMainActivity
import com.v2ray.ang.util.MmkvManager
import com.v2ray.ang.util.Utils
import kotlinx.coroutines.suspendCancellableCoroutine

@Keep
object V2raySDK {

    private val mainStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_MAIN,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val settingsStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_SETTING,
            MMKV.MULTI_PROCESS_MODE
        )
    }

    val isRunning by lazy { MutableLiveData<Boolean>() }

    private var callback: V2rayCallback? = null


    fun init(application: Application) {
        MMKV.initialize(application)
        AngApplication.application = application
        AppConfig.ANG_PACKAGE = application.packageName
        //启动监听
        startListener(application)
    }


    fun startSocks5Proxy(context: Context, configData: Socks5Config, callback: V2rayCallback) {
        this.callback = callback
        val guid = "socks5"
        val config =
            MmkvManager.decodeServerConfig(guid) ?: ServerConfig.create(EConfigType.SOCKS)
        config.remarks = "socks代理"
        config.outboundBean?.settings?.servers?.get(0)?.let { servers ->
            servers.address = configData.host
            servers.port = configData.port
            val socksUsersBean =
                V2rayConfig.OutboundBean.OutSettingsBean.ServersBean.SocksUsersBean()
            socksUsersBean.user = configData.userName
            socksUsersBean.pass = configData.password
            servers.users = listOf(socksUsersBean)
        }
        //保存当前的配置文件
        MmkvManager.encodeServerConfig(guid, config)
        //选中当前的配置文件
        mainStorage?.encode(MmkvManager.KEY_SELECTED_SERVER, guid)
        //启用本地DNS服务
        settingsStorage.encode(AppConfig.PREF_LOCAL_DNS_ENABLED, true)
        //启用虚拟DNS服务
        settingsStorage.encode(AppConfig.PREF_FAKE_DNS_ENABLED, true)
        if (configData.proxyApp == null || configData.proxyApp.isEmpty()) {
            //分应用代理
            settingsStorage.encode(AppConfig.PREF_PER_APP_PROXY, false)
            //全局代理
            settingsStorage.encode(AppConfig.PREF_ROUTING_MODE, 0)
        } else {
            //分应用代理
            settingsStorage.encode(AppConfig.PREF_PER_APP_PROXY, true)
            val app = HashSet<String>()
            configData.proxyApp.forEach { app.add(it) }
            settingsStorage.encode(AppConfig.PREF_PER_APP_PROXY_SET, app)
        }
        //启动服务
        V2RayServiceManager.startV2Ray(context)
    }

    suspend fun startSocks5ProxyAwait(context: Context, configData: Socks5Config) =
        suspendCancellableCoroutine<Boolean> { scope ->
            startSocks5Proxy(context, configData, object : V2rayCallback {
                override fun start(isSucceed: Boolean, isRun: Boolean) {
                    scope.safeResume(isSucceed)
                }

                override fun stop() {

                }
            })
        }


    fun stopSocks5Proxy(context: Context) {
        Utils.stopVService(context)
    }


    private fun startListener(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                mMsgReceiver,
                IntentFilter(AppConfig.BROADCAST_ACTION_ACTIVITY),
                Context.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(
                mMsgReceiver,
                IntentFilter(AppConfig.BROADCAST_ACTION_ACTIVITY)
            )
        }
    }

    private fun stopListener(context: Context) {
        context.unregisterReceiver(mMsgReceiver)
    }

    private val mMsgReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.getIntExtra("key", 0)) {
                AppConfig.MSG_STATE_RUNNING -> {
                    isRunning.value = true
                }

                AppConfig.MSG_STATE_NOT_RUNNING -> {
                    isRunning.value = false
                }

                AppConfig.MSG_STATE_START_SUCCESS -> {
                    ctx?.toast(R.string.toast_services_success)
                    isRunning.value = true
                    callback?.start(true, isRunning.value ?: false)
                }

                AppConfig.MSG_STATE_START_FAILURE -> {
                    ctx?.toast(R.string.toast_services_failure)
                    isRunning.value = false
                    callback?.start(false, isRunning.value ?: false)
                }

                AppConfig.MSG_STATE_STOP_SUCCESS -> {
                    isRunning.value = false
                    callback?.stop()
                }

                AppConfig.MSG_MEASURE_DELAY_SUCCESS -> {
                }

                AppConfig.MSG_MEASURE_CONFIG_SUCCESS -> {

                }
            }
        }
    }
}