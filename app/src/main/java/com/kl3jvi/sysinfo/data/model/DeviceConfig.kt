package com.kl3jvi.sysinfo.data.model

data class DeviceConfig(
    val devices: List<DeviceMapping>
)

data class DeviceMapping(
    val imei_prefix: String,
    val board: String,
    val details: DeviceDetails
)

data class DeviceDetails(
    @com.google.gson.annotations.SerializedName("Device Name")
    val deviceName: String,
    @com.google.gson.annotations.SerializedName("Model")
    val model: String,
    @com.google.gson.annotations.SerializedName("Manufacturer")
    val manufacturer: String,
    @com.google.gson.annotations.SerializedName("Brand")
    val brand: String,
    @com.google.gson.annotations.SerializedName("Device")
    val device: String,
    @com.google.gson.annotations.SerializedName("Board")
    val board: String
)