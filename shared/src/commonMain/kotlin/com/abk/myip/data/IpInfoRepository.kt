package com.abk.myip.data

import com.abk.myip.domain.GeoLocation
import com.abk.myip.domain.IpInfo

interface IpInfoRepository {
    suspend fun getMyIpInfo(): IpInfo
}

class DefaultIpInfoRepository(private val service: IpApiService) : IpInfoRepository {
    override suspend fun getMyIpInfo(): IpInfo = service.fetchIpInfo().toDomain()
}

private fun IpInfoDto.toDomain(): IpInfo {
    val (lat, lon) = parseLoc(loc)
    return IpInfo(
        ip = ip,
        city = city,
        region = region,
        country = country,
        countryCode = country,
        timezone = timezone,
        location = GeoLocation(latitude = lat, longitude = lon),
        postal = postal ?: "",
        org = org,
    )
}

private fun parseLoc(loc: String): Pair<Double, Double> {
    val parts = loc.split(",")
    require(parts.size == 2) { "Invalid loc format: $loc" }
    return parts[0].toDouble() to parts[1].toDouble()
}
