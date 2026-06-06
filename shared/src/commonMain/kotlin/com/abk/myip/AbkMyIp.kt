package com.abk.myip

import com.abk.myip.data.DefaultIpInfoRepository
import com.abk.myip.data.IpApiService
import com.abk.myip.platform.httpClient
import com.abk.myip.usecase.BuildStaticMapUrlUseCase
import com.abk.myip.usecase.GetMyIpInfoUseCase
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AbkMyIp {
    private val client = httpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    private val service = IpApiService(client)
    private val repository = DefaultIpInfoRepository(service)

    val getMyIpInfo: GetMyIpInfoUseCase = GetMyIpInfoUseCase(repository)
    val buildStaticMapUrl: BuildStaticMapUrlUseCase = BuildStaticMapUrlUseCase()
}
