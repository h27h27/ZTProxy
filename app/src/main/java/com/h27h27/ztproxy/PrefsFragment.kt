package com.h27h27.ztproxy

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import java.util.Arrays

class PrefsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "PrefsFragment"
        private const val PLANET_DOWNLOAD_CONN_TIMEOUT = 5 * 1000
        private const val PLANET_DOWNLOAD_TIMEOUT = 10 * 1000
        private val PLANET_FILE_HEADER = byteArrayOf(0x01)  // Planet file header
    }

    private var prefPlanetUseCustom: SwitchPreference? = null
    private var prefSetPlanetFile: Preference? = null
    private var prefSetMoonFile: Preference? = null

    private var planetDialog: Dialog? = null
    private var moonDialog: Dialog? = null
    private var loadingDialog: Dialog? = null

    private lateinit var planetFileSelectLauncher: ActivityResultLauncher<Intent>
    private lateinit var moonFileSelectLauncher: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        planetFileSelectLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == -1 && result.data != null) {
                val uriData = result.data?.data
                if (uriData == null) {
                    Log.e(TAG, "Invalid planet URI")
                    return@registerForActivityResult
                }
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uriData)
                    inputStream?.use {
                        val tempFile = File(requireContext().cacheDir, "planet_temp")
                        it.copyTo(tempFile.outputStream())
                        val success = dealTempPlanetFile(tempFile)
                        if (!success) {
                            Toast.makeText(context, R.string.planet_wrong_format, Toast.LENGTH_LONG).show()
                            closePlanetDialog()
                            return@registerForActivityResult
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Cannot copy planet file", e)
                    Toast.makeText(context, R.string.cannot_open_planet, Toast.LENGTH_LONG).show()
                    closePlanetDialog()
                    return@registerForActivityResult
                }
                Log.i(TAG, "Copy planet file successfully")
                Snackbar.make(requireView(), R.string.set_planet_succ, Snackbar.LENGTH_LONG).show()
                closePlanetDialog()
            } else {
                Toast.makeText(context, R.string.cannot_open_planet, Toast.LENGTH_LONG).show()
            }
        }

        moonFileSelectLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == -1 && result.data != null) {
                val uriData = result.data?.data
                if (uriData == null) {
                    Log.e(TAG, "Invalid moon URI")
                    return@registerForActivityResult
                }
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uriData)
                    inputStream?.use {
                        PreferencesManager.setMoonFileUri(context, uriData.toString())
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Cannot copy moon file", e)
                    Toast.makeText(context, R.string.cannot_open_moon, Toast.LENGTH_LONG).show()
                    return@registerForActivityResult
                }
                Log.i(TAG, "Copy moon file successfully")
                Snackbar.make(requireView(), R.string.set_moon_succ, Snackbar.LENGTH_LONG).show()
                closeMoonDialog()
            } else {
                Toast.makeText(context, R.string.cannot_open_moon, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        prefPlanetUseCustom = findPreference("planet_use_custom")
        prefSetPlanetFile = findPreference("set_planet_file")
        prefSetMoonFile = findPreference("set_moon_file")

        prefPlanetUseCustom?.setOnPreferenceClickListener {
            updatePlanetSetting()
            val prefs = it.sharedPreferences
            if (prefs != null && !prefs.getBoolean("planet_use_custom", false)) {
                return@setOnPreferenceClickListener true
            }
            if (customPlanetFileNotExist()) {
                showPlanetDialog()
            }
            true
        }

        prefSetPlanetFile?.setOnPreferenceClickListener {
            showPlanetDialog()
            true
        }

        prefSetMoonFile?.setOnPreferenceClickListener {
            showMoonDialog()
            true
        }

        updatePlanetSetting()
    }

    private fun showPlanetDialog() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_planet_select, null)
        val viewFile = view.findViewById<View>(R.id.from_file)
        val viewUrl = view.findViewById<View>(R.id.from_url)

        viewFile.setOnClickListener { showPlanetFromFileDialog() }
        viewUrl.setOnClickListener { showPlanetFromUrlDialog() }

        val builder = AlertDialog.Builder(context)
            .setView(view)
            .setTitle(R.string.load_planet_file)
            .setOnCancelListener { closePlanetDialog() }

        planetDialog = builder.create()
        planetDialog?.show()
    }

    private fun showPlanetFromFileDialog() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        planetFileSelectLauncher.launch(intent)
    }

    private fun showPlanetFromUrlDialog() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_planet_url, null)
        val editText = view.findViewById<EditText>(R.id.planet_url)

        AlertDialog.Builder(context)
            .setView(view)
            .setTitle(R.string.load_planet_file)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val url = editText.text.toString()
                val fileUrl = try {
                    URL(url)
                } catch (e: MalformedURLException) {
                    Toast.makeText(context, "Invalid URL format", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                if (planetDialog != null) {
                    planetDialog?.dismiss()
                    planetDialog = null
                }

                showLoadingDialog(R.string.downloading)

                Thread {
                    try {
                        val tempFile = File(requireContext().cacheDir, "planet_temp")
                        fileUrl.openStream().use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        val success = dealTempPlanetFile(tempFile)
                        if (!success) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(context, R.string.planet_wrong_format, Toast.LENGTH_LONG).show()
                                closePlanetDialog()
                                closeLoadingDialog()
                            }
                            return@Thread
                        }
                    } catch (e: SocketTimeoutException) {
                        Log.e(TAG, "Cannot download planet file", e)
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, R.string.planet_download_timeout, Toast.LENGTH_LONG).show()
                            closePlanetDialog()
                            closeLoadingDialog()
                        }
                        return@Thread
                    } catch (e: IOException) {
                        Log.e(TAG, "Cannot download planet file", e)
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, R.string.cannot_download_planet_file, Toast.LENGTH_LONG).show()
                            closePlanetDialog()
                            closeLoadingDialog()
                        }
                        return@Thread
                    }

                    requireActivity().runOnUiThread {
                        Snackbar.make(requireView(), R.string.set_planet_succ, Snackbar.LENGTH_LONG).show()
                        closePlanetDialog()
                        closeLoadingDialog()
                    }
                }.start()
            }
            .create()
            .show()
    }

    private fun showMoonDialog() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        moonFileSelectLauncher.launch(intent)
    }

    private fun customPlanetFileNotExist(): Boolean {
        val file = File(requireActivity().filesDir, "planet_custom")
        return !file.exists()
    }

    private fun closePlanetDialog() {
        if (planetDialog != null) {
            planetDialog?.dismiss()
            planetDialog = null
        }
        if (customPlanetFileNotExist()) {
            prefPlanetUseCustom?.isChecked = false
        }
    }

    private fun closeMoonDialog() {
        if (moonDialog != null) {
            moonDialog?.dismiss()
            moonDialog = null
        }
    }

    private fun updatePlanetSetting() {
        val prefs = preferenceManager.sharedPreferences
        val useCustom = prefs?.getBoolean("planet_use_custom", false) ?: false
        prefSetPlanetFile?.isEnabled = useCustom
    }

    private fun dealTempPlanetFile(tempFile: File): Boolean {
        return try {
            val buf = ByteArray(PLANET_FILE_HEADER.size)
            FileInputStream(tempFile).use { `in` ->
                if (`in`.read(buf) != PLANET_FILE_HEADER.size) {
                    return@use
                }
                if (!Arrays.equals(buf, PLANET_FILE_HEADER)) {
                    Log.i(TAG, "Planet file has a wrong header")
                    return@use
                }
            }
            val dest = File(requireActivity().filesDir, "planet_custom")
            tempFile.renameTo(dest)
            PreferencesManager.setPlanetFileUri(requireContext(), dest.absolutePath)
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun showLoadingDialog(prompt: Int) {
        closeLoadingDialog()
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        val textPrompt = view.findViewById<TextView>(R.id.prompt)
        textPrompt.setText(prompt)

        loadingDialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()
        loadingDialog?.show()
    }

    private fun closeLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog?.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Handle preference changes if needed
    }
}
