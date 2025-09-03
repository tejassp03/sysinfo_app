package com.kl3jvi.sysinfo.data.provider

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.kl3jvi.sysinfo.data.model.DeviceConfig
import com.kl3jvi.sysinfo.domain.models.Information
import org.koin.core.component.KoinComponent
import java.io.IOException

class DeviceDataProvider(
    private val appContext: Context,
    private val contentResolver: ContentResolver,
    private val wifiManager: WifiManager
) : KoinComponent {

    fun getDeviceInformation(): List<Information> {
        val deviceConfig = loadDeviceConfig()
        val currentImei = getCurrentImei()
        val currentBoard = Build.BOARD
        
        Log.d("DeviceDataProvider", "Current IMEI: '$currentImei'")
        Log.d("DeviceDataProvider", "Current Board: '$currentBoard'")
        
        val matchedDevice = deviceConfig?.devices?.firstOrNull { deviceMapping ->
            val boardMatches = currentBoard == deviceMapping.board
            Log.d("DeviceDataProvider", "Board matches: $boardMatches")
            boardMatches
        }
        
        Log.d("DeviceDataProvider", "Matched device: ${matchedDevice != null}")
        
        return if (matchedDevice != null) {
            // Use configuration from JSON, but keep Hardware as detected value
            listOf(
                Information("Device Name", matchedDevice.details.deviceName),
                Information("Device Model", matchedDevice.details.model),
                Information("Manufacturer", matchedDevice.details.manufacturer),
                Information("Device", matchedDevice.details.device),
                Information("Board", matchedDevice.details.board),
                Information("Hardware", Build.HARDWARE), // Keep detected value
                Information("Brand", matchedDevice.details.brand),
                Information(
                    "Android Device Id",
                    Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                ),
                Information(
                    "Language",
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) appContext.resources.configuration.locales[0].displayName else ""
                ),
                // Build Fingerprint removed as per requirements
                Information(
                    "Usb Host",
                    if (appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) "Supported" else "Not Supported"
                ),
                Information(
                    "Device Type",
                    if (appContext.resources.configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)) "Tablet" else "Phone"
                ),
                Information("Network Type", getNetworkType()),
                Information("Main Network Operator", getMainNetworkOperator().getOrNull() ?: "Unknown")
            )
        } else {
            // No match found, use default detected values
            listOf(
                Information("Device Name", Build.DEVICE),
                Information("Device Model", Build.MODEL),
                Information("Manufacturer", Build.MANUFACTURER),
                Information("Device", Build.DEVICE),
                Information("Board", Build.BOARD),
                Information("Hardware", Build.HARDWARE),
                Information("Brand", Build.BRAND),
                Information(
                    "Android Device Id",
                    Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                ),
                Information(
                    "Language",
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) appContext.resources.configuration.locales[0].displayName else ""
                ),
                Information("Build Fingerprint", Build.FINGERPRINT),
                Information(
                    "Usb Host",
                    if (appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) "Supported" else "Not Supported"
                ),
                Information(
                    "Device Type",
                    if (appContext.resources.configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)) "Tablet" else "Phone"
                ),
                Information("Network Type", getNetworkType()),
                Information("Main Network Operator", getMainNetworkOperator().getOrNull() ?: "Unknown")
            )
        }
    }

    private fun getNetworkType(): String {
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork ?: return "Unknown"
        } else {
            return ""
        }
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "Unknown"

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
            else -> "Unknown"
        }
    }

    private fun getMainNetworkOperator(): Result<String> {
        return runCatching { wifiManager.connectionInfo.ssid }
    }
    
    private fun loadDeviceConfig(): DeviceConfig? {
        return try {
            val inputStream = appContext.assets.open("device_config.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            Gson().fromJson(jsonString, DeviceConfig::class.java)
        } catch (e: IOException) {
            null
        } catch (e: Exception) {
            null
        }
    }
    
    @Suppress("MissingPermission")
    private fun getCurrentImei(): String {
        return try {
            val telephonyManager = appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    telephonyManager.imei ?: telephonyManager.deviceId ?: ""
                } else {
                    @Suppress("DEPRECATION")
                    telephonyManager.deviceId ?: ""
                }
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
