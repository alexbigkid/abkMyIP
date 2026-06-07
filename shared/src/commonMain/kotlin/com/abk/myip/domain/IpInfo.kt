package com.abk.myip.domain

data class IpInfo(
    val ip: String,
    val city: String,
    val region: String,
    val country: String,
    val countryCode: String,
    val timezone: String,
    val location: GeoLocation,
    val postal: String,
    val org: String? = null,
)
