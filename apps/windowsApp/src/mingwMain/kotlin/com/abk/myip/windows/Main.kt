package com.abk.myip.windows

import com.abk.myip.AbkMyIp
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val app = AbkMyIp()
    val info = app.getMyIpInfo()
    val map = app.buildStaticMapUrl(info.location)

    println("IP:        ${info.ip}")
    println("City:      ${info.city}, ${info.region}")
    println("Country:   ${info.country} (${info.countryCode})")
    println("Timezone:  ${info.timezone}")
    println("Location:  ${info.location.latitude}, ${info.location.longitude}")
    println("Map URL:   ${map.value}")
}
