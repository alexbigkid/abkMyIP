package com.abk.myip.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IpApiServiceTest {

    private val sampleJson = """
        {
            "ip": "203.0.113.7",
            "city": "San Francisco",
            "region": "California",
            "country_name": "United States",
            "country_code": "US",
            "timezone": "America/Los_Angeles",
            "latitude": 37.7749,
            "longitude": -122.4194,
            "postal": "94103",
            "org": "AS141039 PacketHub S.A."
        }
    """.trimIndent()

    private fun service(
        responder: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): IpApiService {
        val client = HttpClient(MockEngine(responder)) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return IpApiService(client)
    }

    @Test
    fun `fetchIpInfo deserializes a complete response`() = runTest {
        val svc = service {
            respond(
                content = ByteReadChannel(sampleJson),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        val dto = svc.fetchIpInfo()

        assertEquals("203.0.113.7", dto.ip)
        assertEquals("San Francisco", dto.city)
        assertEquals("California", dto.region)
        assertEquals("United States", dto.country)
        assertEquals("US", dto.countryCode)
        assertEquals("America/Los_Angeles", dto.timezone)
        assertEquals(37.7749, dto.latitude)
        assertEquals(-122.4194, dto.longitude)
        assertEquals("94103", dto.postal)
        assertEquals("AS141039 PacketHub S.A.", dto.org)
    }

    @Test
    fun `fetchIpInfo throws on non-success http status`() = runTest {
        val svc = service {
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.ServiceUnavailable,
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        assertFailsWith<IpLookupException> {
            svc.fetchIpInfo()
        }
    }

    @Test
    fun `fetchIpInfo calls the configured endpoint`() = runTest {
        var capturedUrl: String? = null
        val svc = service { request ->
            capturedUrl = request.url.toString()
            respond(
                content = ByteReadChannel(sampleJson),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        svc.fetchIpInfo()

        assertEquals("https://ipapi.co/json/", capturedUrl)
    }
}
