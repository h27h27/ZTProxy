package com.h27h27.ztproxy

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        try {
            // Set up toolbar/back button
            val backButton = findViewById<Button>(R.id.backButton)
            backButton?.setOnClickListener {
                finish()
            }

            // Load PrefsFragment if this is the first time
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.settings_container, PrefsFragment())
                    .commit()
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsActivity", "Error in onCreate", e)
        }
    }
}

// OLD CODE BELOW - DEPRECATED
/*
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class SettingsActivityOld : AppCompatActivity() {
    private val openPlanetFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                PreferencesManager.setPlanetFileUri(this, it.toString())
            } catch (ignored: Exception) {
            }
            updatePlanetFileStatus()
        }
    }

    private val openMoonFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                PreferencesManager.setMoonFileUri(this, it.toString())
            } catch (ignored: Exception) {
            }
            updateMoonFileStatus()
        }
    }

    private lateinit var planetUrlInput: EditText
    private lateinit var planetFileStatusText: TextView
    private lateinit var moonFileStatusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        try {
            val backButton = findViewById<Button>(R.id.backButton)
            val proxyPortInput = findViewById<EditText>(R.id.proxyPortInput)
            val bindAddressInput = findViewById<EditText>(R.id.bindAddressInput)
            val applySocksButton = findViewById<Button>(R.id.applySocksButton)
            val autoStartCheckbox = findViewById<CheckBox>(R.id.autoStartCheckbox)
            val debugLoggingCheckbox = findViewById<CheckBox>(R.id.debugLoggingCheckbox)
            val timeoutInput = findViewById<EditText>(R.id.timeoutInput)
            val useVirtualInterfaceCheckbox = findViewById<CheckBox>(R.id.useVirtualInterfaceCheckbox)
            val networkIdInput = findViewById<EditText>(R.id.networkIdInput)
            val selectPlanetFileButton = findViewById<Button>(R.id.selectPlanetFileButton)
            val selectMoonFileButton = findViewById<Button>(R.id.selectMoonFileButton)
            planetUrlInput = findViewById(R.id.planetUrlInput)
            planetFileStatusText = findViewById(R.id.planetFileStatusText)
            moonFileStatusText = findViewById(R.id.moonFileStatusText)

            // Load current settings
            proxyPortInput.setText(PreferencesManager.getProxyPort(this).toString())
            bindAddressInput.setText(PreferencesManager.getBindAddress(this))
            autoStartCheckbox.isChecked = PreferencesManager.getAutoStart(this)
            debugLoggingCheckbox.isChecked = PreferencesManager.getDebugLogging(this)
            timeoutInput.setText((PreferencesManager.getSocketTimeout(this) / 1000).toString())
            useVirtualInterfaceCheckbox.isChecked = PreferencesManager.getUseVirtualInterface(this)
            networkIdInput.setText(PreferencesManager.getNetworkId(this))
            planetUrlInput.setText(PreferencesManager.getPlanetUrl(this))
            updatePlanetFileStatus()
            updateMoonFileStatus()

            backButton.setOnClickListener {
                finish()
            }

            selectPlanetFileButton.setOnClickListener {
                openPlanetFile.launch(arrayOf("*/*"))
            }

            selectMoonFileButton.setOnClickListener {
                openMoonFile.launch(arrayOf("*/*"))
            }

            applySocksButton.setOnClickListener {
                try {
                    val port = proxyPortInput.text.toString().toIntOrNull() ?: 1080
                    if (port < 1024 || port > 65535) {
                        Toast.makeText(this, "Port must be between 1024 and 65535", Toast.LENGTH_SHORT)
                            .show()
                        return@setOnClickListener
                    }

                    val address = bindAddressInput.text.toString()
                    val timeout = timeoutInput.text.toString().toIntOrNull() ?: 30
                    val autoStart = autoStartCheckbox.isChecked
                    val debug = debugLoggingCheckbox.isChecked
                    val useVirtual = useVirtualInterfaceCheckbox.isChecked
                    val netId = networkIdInput.text.toString().trim()
                    val planetUrl = planetUrlInput.text.toString().trim()

                    PreferencesManager.setProxyPort(this, port)
                    PreferencesManager.setBindAddress(this, address)
                    PreferencesManager.setSocketTimeout(this, timeout)
                    PreferencesManager.setAutoStart(this, autoStart)
                    PreferencesManager.setDebugLogging(this, debug)
                    PreferencesManager.setUseVirtualInterface(this, useVirtual)
                    PreferencesManager.setNetworkId(this, netId)
                    PreferencesManager.setPlanetUrl(this, planetUrl)

                    Toast.makeText(this, "Settings saved. Restart the service to apply changes.", Toast.LENGTH_LONG)
                        .show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error saving settings: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing settings: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updatePlanetFileStatus() {
        val uri = PreferencesManager.getPlanetFileUri(this)
        planetFileStatusText.text = if (uri.isNotBlank()) {
            "Planet file selected"
        } else {
            "No planet file selected"
        }
    }

    private fun updateMoonFileStatus() {
        val uri = PreferencesManager.getMoonFileUri(this)
        moonFileStatusText.text = if (uri.isNotBlank()) {
            "Moon file selected"
        } else {
            "No moon file selected"
        }
    }
}
