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
        country = "US",
        loc = "37.7749,-122.4194",
        timezone = "America/Los_Angeles",
        postal = "94103",
        org = "AS141039 PacketHub S.A.",
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
                country = "US",
                countryCode = "US",
                timezone = "America/Los_Angeles",
                location = com.abk.myip.domain.GeoLocation(37.7749, -122.4194),
                postal = "94103",
                org = "AS141039 PacketHub S.A.",
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

    @Test
    fun `getMyIpInfo parses loc into latitude and longitude`() = runTest {
        val repo = repoReturning(baselineDto.copy(loc = "6.2450,-75.5715"))

        val info = repo.getMyIpInfo()

        assertEquals(6.2450, info.location.latitude)
        assertEquals(-75.5715, info.location.longitude)
    }

    @Test
    fun `getMyIpInfo passes through a null org`() = runTest {
        val repo = repoReturning(baselineDto.copy(org = null))

        val info = repo.getMyIpInfo()

        assertEquals(null, info.org)
    }

    @Test
    fun `getMyIpInfo treats missing postal as empty string`() = runTest {
        val repo = repoReturning(baselineDto.copy(postal = null))

        val info = repo.getMyIpInfo()

        assertEquals("", info.postal)
    }

    private class FakeService(private val dto: IpInfoDto) : IpApiService {
        override suspend fun fetchIpInfo(): IpInfoDto = dto
    }
}
