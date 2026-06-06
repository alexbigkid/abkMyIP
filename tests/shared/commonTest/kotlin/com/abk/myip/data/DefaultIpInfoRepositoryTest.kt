package com.abk.myip.data

import com.abk.myip.domain.IpInfo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultIpInfoRepositoryTest {

    private val baselineDto = IpInfoDto(
        ip = "203.0.113.7",
        city = "San Francisco",
        region = "California",
        country = "United States",
        countryCode = "US",
        timezone = "America/Los_Angeles",
        latitude = 37.7749,
        longitude = -122.4194,
        postal = "94103",
    )

    private fun repoReturning(dto: IpInfoDto): DefaultIpInfoRepository =
        DefaultIpInfoRepository(service = FakeService(dto))

    @Test
    fun `getMyIpInfo maps DTO to domain IpInfo`() = runTest {
        val repo = repoReturning(baselineDto)

        val info = repo.getMyIpInfo()

        assertEquals(
            IpInfo(
                ip = "203.0.113.7",
                city = "San Francisco",
                region = "California",
                country = "United States",
                countryCode = "US",
                timezone = "America/Los_Angeles",
                location = com.abk.myip.domain.GeoLocation(37.7749, -122.4194),
                postal = "94103",
            ),
            info,
        )
    }

    @Test
    fun `getMyIpInfo preserves a different ip from the service`() = runTest {
        val repo = repoReturning(baselineDto.copy(ip = "198.51.100.42", city = "Reykjavik"))

        val info = repo.getMyIpInfo()

        assertEquals("198.51.100.42", info.ip)
        assertEquals("Reykjavik", info.city)
    }

    private class FakeService(private val dto: IpInfoDto) : IpApiService {
        override suspend fun fetchIpInfo(): IpInfoDto = dto
    }
}
