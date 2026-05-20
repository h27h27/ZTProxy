package com.h27h27.ztproxy

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var isServiceRunning = false
    private lateinit var statusText: TextView
    private lateinit var proxyPortText: TextView
    private lateinit var addressText: TextView
    private lateinit var toggleButton: Button
    private lateinit var statusIndicator: View
    private lateinit var ztStatusText: TextView
    private lateinit var ztAddressText: TextView
    private lateinit var networkIdInput: EditText
    private lateinit var joinNetworkButton: Button
    private lateinit var settingsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        statusText = findViewById(R.id.statusText)
        proxyPortText = findViewById(R.id.proxyPortText)
        addressText = findViewById(R.id.addressText)
        toggleButton = findViewById(R.id.toggleButton)
        statusIndicator = findViewById(R.id.statusIndicator)
        ztStatusText = findViewById(R.id.ztStatusText)
        ztAddressText = findViewById(R.id.ztAddressText)
        networkIdInput = findViewById(R.id.networkIdInput)
        joinNetworkButton = findViewById(R.id.joinNetworkButton)
        settingsButton = findViewById(R.id.settingsButton)
        val infoButton = findViewById<Button>(R.id.infoButton)

        // Set up button listeners
        toggleButton.setOnClickListener { toggleProxy() }
        joinNetworkButton.setOnClickListener { joinNetwork() }
        settingsButton.setOnClickListener { openSettings() }
        infoButton.setOnClickListener { showInfo() }

        // Initialize service state
        updateServiceStatus()
        updateUI()

        // Start libzt manager initialization in background
        CoroutineScope(Dispatchers.IO).launch {
            LibZtManager.initialize(applicationContext)
            // TODO: join network and configure virtual interface
        }

        // Auto-start service if enabled in preferences
        if (PreferencesManager.getAutoStart(this)) {
            if (!isServiceRunning) {
                ServiceManager.startSocksService(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
        updateUI()
    }

    private fun updateServiceStatus() {
        isServiceRunning = ServiceManager.isSocksServiceRunning(this)
    }

    private fun updateUI() {
        val port = PreferencesManager.getProxyPort(this)
        val address = PreferencesManager.getBindAddress(this)

        if (isServiceRunning) {
            statusText.text = "Status: Running"
            statusIndicator.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
            toggleButton.text = "Stop"
        } else {
            statusText.text = "Status: Stopped"
            statusIndicator.setBackgroundColor(Color.parseColor("#FF5252")) // Red
            toggleButton.text = "Start"
        }

        proxyPortText.text = "Port: $port"
        addressText.text = "Address: $address"

        val ztAddress = LibZtManager.getVirtualAddress()
        if (ztAddress != null) {
            ztStatusText.text = "Status: Connected"
            ztAddressText.text = "Virtual Address: $ztAddress"
        } else {
            ztStatusText.text = "Status: Not initialized"
            ztAddressText.text = "Virtual Address: -"
        }
    }

    private fun toggleProxy() {
        if (isServiceRunning) {
            ServiceManager.stopSocksService(this)
            Toast.makeText(this, "Stopping proxy...", Toast.LENGTH_SHORT).show()
        } else {
            ServiceManager.startSocksService(this)
            Toast.makeText(this, "Starting proxy...", Toast.LENGTH_SHORT).show()
        }
        updateServiceStatus()
        updateUI()
    }

    private fun joinNetwork() {
        val networkId = networkIdInput.text.toString().trim()
        if (networkId.isEmpty()) {
            Toast.makeText(this, "Please enter a network ID", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                LibZtManager.joinNetwork(networkId)
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Joined network: $networkId",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error joining network: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun showInfo() {
        val port = PreferencesManager.getProxyPort(this)
        val address = PreferencesManager.getBindAddress(this)
        val status = if (isServiceRunning) "Running" else "Stopped"
        
        val message = """
            ZTProxy v1.0
            
            SOCKS Proxy Status: $status
            Bind Address: $address:$port
            
            ZeroTier is a platform that simplifies the concepts of virtual networking and manages it as a global software defined network that you can manage and control in one place.
        """.trimIndent()

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("About ZTProxy")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }
}
