package com.getsquire.bookingapp

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.getsquire.bookingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            enableIconVariant1()
            showMessage("Replace with your own action")
        }
    }

    override fun onResume() {
        super.onResume()
        // We need this delay to call clipboard method only after the app is fully resumed
        Handler(Looper.getMainLooper()).post() {
            handleAliasData()
            handleIntentData()
            handleInstallReferrer()
            handleClipboardData()
        }
    }

    private fun handleAliasData() {
        val componentName = intent.component?.className
        showMessage("Launched via $componentName")
    }

    private fun handleIntentData() {
        intent?.data?.lastPathSegment?.let { alias ->
            showMessage("Current barber from intent is $alias")
        }
    }

    private fun handleClipboardData() {
        val clipboardText = getClipboardText()
        clipboardText?.let {
            binding.message.text = "Data from clipboard: $it"
        }
    }

    private fun handleInstallReferrer() {
        val referrerClient = InstallReferrerClient.newBuilder(this).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                    try {
                        val response = referrerClient.installReferrer
                        val referrerUrl = response.installReferrer
                        Log.d("InstallReferrer", "Referrer URL: $referrerUrl")
                    } catch (e: Exception) {
                        Log.e("InstallReferrer", "Error retrieving referrer: ${e.message}")
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Handle disconnection if necessary
            }
        })
    }

    private fun getClipboardText(): String? {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(
                ClipDescription.MIMETYPE_TEXT_PLAIN
            ) == true
        ) {
            val item = clipboard.primaryClip?.getItemAt(clipboard.primaryClip!!.itemCount - 1)
            return item?.text.toString()
        }
        return null
    }

    private fun enableIconVariant1() {
        val packageName = packageName
        packageManager.setComponentEnabledSetting(
            componentName(className = "$packageName.IconVariant1"),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            componentName(className = "$packageName.IconVariant2"),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun showMessage(message: String) {
        binding.message.text = message
    }

    private fun componentName(className: String) = ComponentName(packageName, className)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
