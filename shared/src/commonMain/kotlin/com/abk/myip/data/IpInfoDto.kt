package com.abk.myip.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IpInfoDto(
    val ip: String,
    val city: String,
    val region: String,
    @SerialName("country_name") val country: String,
    @SerialName("country_code") val countryCode: String,
    val timezone: String,
    val latitude: Double,
    val longitude: Double,
    val postal: String,
    val org: String? = null,
)
