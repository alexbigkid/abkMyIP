package com.abk.myip

import com.abk.myip.platform.httpClient
import com.abk.myip.platform.platformName
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AbkMyIpSmokeTest {

    @Test
    fun `AbkMyIp constructs and exposes both use cases`() {
        val app = AbkMyIp()

        assertNotNull(app.getMyIpInfo, "getMyIpInfo use case should be wired")
        assertNotNull(app.buildStaticMapUrl, "buildStaticMapUrl use case should be wired")
    }

    @Test
    fun `platformName actual is non-blank on every host`() {
        assertTrue(platformName.isNotBlank(), "expected non-blank platformName, got: '$platformName'")
    }

    @Test
    fun `httpClient can be constructed with the default empty config`() {
        val client = httpClient()

        assertNotNull(client, "httpClient() with no args should return a client")
        client.close()
    }
}
