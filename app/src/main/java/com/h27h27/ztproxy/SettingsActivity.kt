package com.h27h27.ztproxy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        try {
            val backButton = findViewById<Button>(R.id.backButton)
            val proxyPortInput = findViewById<EditText>(R.id.proxyPortInput)
            val bindAddressInput = findViewById<EditText>(R.id.bindAddressInput)
            val applySocksButton = findViewById<Button>(R.id.applySocksButton)
            val autoStartCheckbox = findViewById<android.widget.CheckBox>(R.id.autoStartCheckbox)
            val debugLoggingCheckbox = findViewById<android.widget.CheckBox>(R.id.debugLoggingCheckbox)
            val timeoutInput = findViewById<EditText>(R.id.timeoutInput)

            // Load current settings
            proxyPortInput.setText(PreferencesManager.getProxyPort(this).toString())
            bindAddressInput.setText(PreferencesManager.getBindAddress(this))
            autoStartCheckbox.isChecked = PreferencesManager.getAutoStart(this)
            debugLoggingCheckbox.isChecked = PreferencesManager.getDebugLogging(this)
            timeoutInput.setText((PreferencesManager.getSocketTimeout(this) / 1000).toString())

            backButton.setOnClickListener {
                finish()
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

                    PreferencesManager.setProxyPort(this, port)
                    PreferencesManager.setBindAddress(this, address)
                    PreferencesManager.setSocketTimeout(this, timeout)
                    PreferencesManager.setAutoStart(this, autoStart)
                    PreferencesManager.setDebugLogging(this, debug)

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
}
