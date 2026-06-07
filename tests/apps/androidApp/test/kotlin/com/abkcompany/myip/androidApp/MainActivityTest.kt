package com.abkcompany.myip.androidApp

import com.abk.myip.AbkMyIp
import kotlin.test.Test
import kotlin.test.assertNotNull

class MainActivityTest {

    @Test
    fun `AbkMyIp wires up on the Android target`() {
        val app = AbkMyIp()
        assertNotNull(app)
    }
}
