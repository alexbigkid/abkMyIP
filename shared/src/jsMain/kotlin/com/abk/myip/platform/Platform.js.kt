package com.abk.myip.platform

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js

actual val platformName: String = "Web"

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(Js) { config() }
