package com.h27h27.ztproxy

import android.content.Context

object PreferencesManager {
    private const val PREFS_NAME = "ztproxy_prefs"
    private const val KEY_PROXY_PORT = "proxy_port"
    private const val KEY_BIND_ADDRESS = "bind_address"
    private const val KEY_SOCKET_TIMEOUT = "socket_timeout"
    private const val KEY_AUTO_START = "auto_start"
    private const val KEY_DEBUG_LOGGING = "debug_logging"
    private const val KEY_NETWORK_ID = "network_id"
    private const val KEY_USE_VIRTUAL_INTERFACE = "use_virtual_interface"

    fun getProxyPort(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_PROXY_PORT, 1080)
    }

    fun setProxyPort(context: Context, port: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_PROXY_PORT, port).apply()
    }

    fun getBindAddress(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BIND_ADDRESS, "127.0.0.1") ?: "127.0.0.1"
    }

    fun setBindAddress(context: Context, address: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BIND_ADDRESS, address).apply()
    }

    fun getSocketTimeout(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_SOCKET_TIMEOUT, 30) * 1000 // convert to milliseconds
    }

    fun setSocketTimeout(context: Context, seconds: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_SOCKET_TIMEOUT, seconds).apply()
    }

    fun getAutoStart(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_START, false)
    }

    fun setAutoStart(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_START, enabled).apply()
    }

    fun getDebugLogging(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DEBUG_LOGGING, false)
    }

    fun setDebugLogging(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DEBUG_LOGGING, enabled).apply()
    }

    fun getNetworkId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NETWORK_ID, "") ?: ""
    }

    fun setNetworkId(context: Context, id: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NETWORK_ID, id).apply()
    }

    fun getUseVirtualInterface(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_USE_VIRTUAL_INTERFACE, true)
    }

    fun setUseVirtualInterface(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_USE_VIRTUAL_INTERFACE, enabled).apply()
    }
}
