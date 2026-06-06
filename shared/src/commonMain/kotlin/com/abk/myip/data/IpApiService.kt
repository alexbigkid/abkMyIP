package com.abk.myip.data

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class IpLookupException(message: String) : RuntimeException(message)

interface IpApiService {
    suspend fun fetchIpInfo(): IpInfoDto
}

private const val IPAPI_ENDPOINT = "https://ipapi.co/json/"

fun IpApiService(httpClient: HttpClient): IpApiService = KtorIpApiService(httpClient)

private class KtorIpApiService(private val client: HttpClient) : IpApiService {
    private val logger = Logger.withTag("IpApiService")

    override suspend fun fetchIpInfo(): IpInfoDto {
        logger.d { "GET $IPAPI_ENDPOINT" }
        val response: HttpResponse = client.get(IPAPI_ENDPOINT)
        if (!response.status.isSuccess()) {
            logger.w { "ipapi.co returned ${response.status.value}" }
            throw IpLookupException("ipapi.co returned status ${response.status.value}")
        }
        logger.d { "ipapi.co OK (${response.status.value})" }
        return response.body()
    }
}
