package com.abk.myip.platform

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.curl.Curl

actual val platformName: String = "Linux"

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(Curl) { config() }
