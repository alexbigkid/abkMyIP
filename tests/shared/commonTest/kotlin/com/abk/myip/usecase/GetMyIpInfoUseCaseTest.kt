package com.abk.myip.usecase

import com.abk.myip.data.IpInfoRepository
import com.abk.myip.domain.GeoLocation
import com.abk.myip.domain.IpInfo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetMyIpInfoUseCaseTest {

    private val baseline = IpInfo(
        ip = "203.0.113.7",
        city = "San Francisco",
        region = "California",
        country = "United States",
        countryCode = "US",
        timezone = "America/Los_Angeles",
        location = GeoLocation(37.7749, -122.4194),
        postal = "94103",
    )

    @Test
    fun `invoke returns IpInfo produced by the repository`() = runTest {
        val useCase = GetMyIpInfoUseCase(FakeRepository(baseline))

        val result = useCase()

        assertEquals(baseline, result)
    }

    @Test
    fun `invoke is transparent to alternate ips`() = runTest {
        val other = baseline.copy(ip = "198.51.100.42", city = "Reykjavik")
        val useCase = GetMyIpInfoUseCase(FakeRepository(other))

        val result = useCase()

        assertEquals("198.51.100.42", result.ip)
        assertEquals("Reykjavik", result.city)
    }

    private class FakeRepository(private val info: IpInfo) : IpInfoRepository {
        override suspend fun getMyIpInfo(): IpInfo = info
    }
}
