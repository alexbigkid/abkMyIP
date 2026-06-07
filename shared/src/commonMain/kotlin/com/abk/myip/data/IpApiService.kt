package com.abk.myip.data

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException

class IpLookupException(message: String) : RuntimeException(message)

interface IpApiService {
    suspend fun fetchIpInfo(): IpInfoDto
}

private const val IPINFO_ENDPOINT = "https://ipinfo.io/json"

fun IpApiService(httpClient: HttpClient): IpApiService = KtorIpApiService(httpClient)

private class KtorIpApiService(private val client: HttpClient) : IpApiService {
    private val logger = Logger.withTag("IpApiService")

    override suspend fun fetchIpInfo(): IpInfoDto = try {
        logger.d { "GET $IPINFO_ENDPOINT" }
        val response: HttpResponse = client.get(IPINFO_ENDPOINT)
        if (!response.status.isSuccess()) {
            logger.w { "ipinfo.io returned ${response.status.value}" }
            throw IpLookupException("ipinfo.io returned status ${response.status.value}")
        }
        logger.d { "ipinfo.io OK (${response.status.value})" }
        response.body()
    } catch (e: IpLookupException) {
        throw e
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        logger.e(e) { "Lookup failed" }
        throw IpLookupException("Lookup failed: ${e.message ?: e::class.simpleName}")
    }
}
