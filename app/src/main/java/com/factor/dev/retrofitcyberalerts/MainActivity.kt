package com.factor.dev.retrofitcyberalerts


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.factor.dev.retrofitcyberalerts.ui.theme.OkHttpCyberAlertsTheme
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.Manifest
import android.annotation.SuppressLint
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager


@Composable
fun NetworkInfoScreen(
    context: Context,
    networkService: NetworkService
) {
    var networkDetails by remember { mutableStateOf("Fetching network info...") }

    LaunchedEffect(Unit) {
        while (true) {
            val networkInfo = getNetworkInfo(context)
            networkDetails = networkInfo.toString() // Display JSON in UI

            // Send the network info to the server
            try {
                networkService.sendNetworkInfo(networkInfo)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            delay(1000) // Wait for 1 second
        }
    }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Network Info", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = networkDetails, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@SuppressLint("MissingPermission")
fun getNetworkInfo(context: Context): NetworkInfo {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // Obtener detalles de la red Wi-Fi actual
    val wifiDetails = if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
        val wifiInfo = wifiManager.connectionInfo
        val ssid = if (wifiInfo.ssid.contains("<unknown ssid>")) {
            "Unable to retrieve SSID (check permissions)"
        } else {
            wifiInfo.ssid
        }

        NetworkInfo.WiFi(
            SSID = ssid,
            BSSID = wifiInfo.bssid,
            ipAddress = wifiInfo.ipAddress.toString(),
            linkSpeedMbps = wifiInfo.linkSpeed,
            frequencyMHz = wifiInfo.frequency,
            signalStrengthRSSI = wifiInfo.rssi
        )
    } else null

    // Escanear redes Wi-Fi disponibles
    val wifiScanResults = wifiManager.scanResults.map {
        NetworkInfo.AvailableWiFi(
            SSID = it.SSID,
            BSSID = it.BSSID,
            frequency = it.frequency,
            signalLevel = it.level
        )
    }

    // Obtener detalles de la red móvil
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

    val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
    val operatorName = if (activeSubscriptionInfoList.isNullOrEmpty()) {
        telephonyManager.networkOperatorName ?: "Unknown Operator"
    } else {
        activeSubscriptionInfoList[0].carrierName.toString()
    }

    val mobileDataDetails = if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
        val networkType = try {
            when (telephonyManager.networkType) {
                TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
                TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
                else -> "Unknown"
            }
        } catch (e: SecurityException) {
            "Unknown (Permission Denied)"
        }

        NetworkInfo.MobileData(
            operatorName = operatorName,
            networkType = networkType,
            signalStrengthRSSI = -80, // Reemplazar con lógica para obtener fuerza de señal
            isRoaming = telephonyManager.isNetworkRoaming
        )
    } else null

    return NetworkInfo(
        networks = NetworkInfo.Networks(wifiDetails, mobileDataDetails),
        availableWiFiNetworks = wifiScanResults
    )
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permisos
        requestPermissions()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://8d89-179-32-79-100.ngrok-free.app/") // Replace with your API URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val networkService = retrofit.create(NetworkService::class.java)

        setContent {
            OkHttpCyberAlertsTheme {
                NetworkInfoScreen(context = this, networkService = networkService)
            }
        }
    }

    private fun requestPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        }
    }

}