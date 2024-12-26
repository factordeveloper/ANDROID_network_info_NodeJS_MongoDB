package com.factor.dev.retrofitcyberalerts


data class NetworkInfo(
    val networks: Networks,
    val availableWiFiNetworks: List<AvailableWiFi> = emptyList()
) {
    data class Networks(
        val WiFi: WiFi?,
        val MobileData: MobileData?
    )

    data class WiFi(
        val SSID: String?,
        val BSSID: String?,
        val ipAddress: String?,
        val linkSpeedMbps: Int?,
        val frequencyMHz: Int?,
        val signalStrengthRSSI: Int?
    )

    data class MobileData(
        val operatorName: String?,
        val networkType: String?,
        val signalStrengthRSSI: Int?,
        val isRoaming: Boolean?
    )

    data class AvailableWiFi(
        val SSID: String?,
        val BSSID: String?,
        val frequency: Int,
        val signalLevel: Int
    )
}
