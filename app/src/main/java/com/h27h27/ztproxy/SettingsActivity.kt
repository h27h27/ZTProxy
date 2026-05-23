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
...
*/
