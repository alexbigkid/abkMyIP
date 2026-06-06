package com.abk.myip.platform

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

expect val platformName: String

expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient
