package com.h27h27.ztproxy

import android.content.Context
import android.content.Intent
import android.os.Build

object ServiceManager {
    fun startSocksService(context: Context) {
        val intent = Intent(context, SocksProxyService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopSocksService(context: Context) {
        val intent = Intent(context, SocksProxyService::class.java)
        context.stopService(intent)
    }

    fun isSocksServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.className == SocksProxyService::class.java.name) {
                return true
            }
        }
        return false
    }
}
