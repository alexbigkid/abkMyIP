package com.abk.myip.data

import com.abk.myip.domain.GeoLocation
import com.abk.myip.domain.IpInfo

interface IpInfoRepository {
    suspend fun getMyIpInfo(): IpInfo
}

class DefaultIpInfoRepository(private val service: IpApiService) : IpInfoRepository {
    override suspend fun getMyIpInfo(): IpInfo = service.fetchIpInfo().toDomain()
}

private fun IpInfoDto.toDomain(): IpInfo = IpInfo(
    ip = ip,
    city = city,
    region = region,
    country = country,
    countryCode = countryCode,
    timezone = timezone,
    location = GeoLocation(latitude = latitude, longitude = longitude),
    postal = postal,
    org = org,
)
