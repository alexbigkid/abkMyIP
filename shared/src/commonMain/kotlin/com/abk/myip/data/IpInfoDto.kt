package com.abk.myip.data

import kotlinx.serialization.Serializable

@Serializable
data class IpInfoDto(
    val ip: String,
    val city: String,
    val region: String,
    val country: String,
    val loc: String,
    val timezone: String,
    val postal: String? = null,
    val org: String? = null,
)
