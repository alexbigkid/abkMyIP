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
import kotlin.coroutines.cancellation.CancellationException
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
            "country": "US",
            "loc": "37.7749,-122.4194",
            "timezone": "America/Los_Angeles",
            "postal": "94103",
            "org": "AS141039 PacketHub S.A."
        }
    """.trimIndent()

    private fun service(
        token: String = "",
        responder: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): IpApiService {
        val client = HttpClient(MockEngine(responder)) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return IpApiService(client, token)
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
        assertEquals("US", dto.country)
        assertEquals("37.7749,-122.4194", dto.loc)
        assertEquals("America/Los_Angeles", dto.timezone)
        assertEquals("94103", dto.postal)
        assertEquals("AS141039 PacketHub S.A.", dto.org)
    }

    @Test
    fun `fetchIpInfo throws IpLookupException on non-success http status`() = runTest {
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
    fun `fetchIpInfo wraps malformed payload as IpLookupException`() = runTest {
        val svc = service {
            respond(
                content = ByteReadChannel("""{"reason": "RateLimited", "error": true}"""),
                status = HttpStatusCode.OK,
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

        assertEquals("https://ipinfo.io/json", capturedUrl)
    }

    @Test
    fun `fetchIpInfo sends Authorization header when token is non-blank`() = runTest {
        var capturedAuth: String? = null
        val svc = service(token = "secret-abc") { request ->
            capturedAuth = request.headers["Authorization"]
            respond(
                content = ByteReadChannel(sampleJson),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        svc.fetchIpInfo()

        assertEquals("Bearer secret-abc", capturedAuth)
    }

    @Test
    fun `fetchIpInfo omits Authorization header when token is blank`() = runTest {
        var capturedAuth: String? = "<not-checked>"
        val svc = service(token = "") { request ->
            capturedAuth = request.headers["Authorization"]
            respond(
                content = ByteReadChannel(sampleJson),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        svc.fetchIpInfo()

        assertEquals(null, capturedAuth)
    }

    @Test
    fun `fetchIpInfo re-throws IpLookupException without wrapping it again`() = runTest {
        val svc = service { throw IpLookupException("from engine") }

        val ex = assertFailsWith<IpLookupException> { svc.fetchIpInfo() }
        assertEquals("from engine", ex.message, "outer Throwable catch must not re-wrap an IpLookupException")
    }

    @Test
    fun `fetchIpInfo propagates CancellationException without wrapping it`() = runTest {
        val svc = service { throw CancellationException("cancelled mid-flight") }

        val ex = assertFailsWith<CancellationException> { svc.fetchIpInfo() }
        assertEquals("cancelled mid-flight", ex.message, "cancellation must propagate, not be swallowed as IpLookupException")
    }

    @Test
    fun `fetchIpInfo falls back to class simple name when wrapped exception has no message`() = runTest {
        val svc = service { throw RuntimeException(null as String?) }

        val ex = assertFailsWith<IpLookupException> { svc.fetchIpInfo() }
        assertEquals("Lookup failed: RuntimeException", ex.message)
    }
}
