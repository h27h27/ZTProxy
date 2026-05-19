package com.h27h27.ztproxy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

public class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Start the SOCKS proxy service
        val i = Intent(this, SocksProxyService::class.java)
        startForegroundService(i)
        // Start libzt manager (initialization and join network)
        CoroutineScope(Dispatchers.IO).launch {
            LibZtManager.initialize(applicationContext)
            // TODO: join network and configure virtual interface
        }
        finish()
    }
}
