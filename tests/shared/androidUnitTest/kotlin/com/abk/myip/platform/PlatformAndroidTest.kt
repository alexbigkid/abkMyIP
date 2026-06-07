package com.abk.myip.platform

import kotlin.test.Test
import kotlin.test.assertEquals

class PlatformAndroidTest {

    @Test
    fun `platformName is Android on the Android target`() {
        assertEquals("Android", platformName)
    }
}
