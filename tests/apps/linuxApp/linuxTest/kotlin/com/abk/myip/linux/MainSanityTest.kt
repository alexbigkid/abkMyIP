package com.abk.myip.linux

import kotlin.test.Test
import kotlin.test.assertNotNull

class MainSanityTest {

    @Test
    fun `main function exists`() {
        val fn: (Array<String>) -> Unit = ::main
        assertNotNull(fn)
    }
}
