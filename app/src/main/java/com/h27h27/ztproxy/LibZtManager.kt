package com.h27h27.ztproxy

import android.content.Context

/**
 * Small stub wrapper for libzt Java bindings from the AAR.
 * Replace the TODOs with real calls to the libzt API provided by the AAR.
 */
object LibZtManager {
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return
        // TODO: call into libzt Java API to initialize the node
        // Example (pseudocode):
        // ZT.init(context, options)
        initialized = true
    }

    fun joinNetwork(networkId: String) {
        // TODO: instruct libzt to join a network
    }

    fun getVirtualAddress(): String? {
        // TODO: return assigned ZeroTier IP address on success
        return null
    }
}
