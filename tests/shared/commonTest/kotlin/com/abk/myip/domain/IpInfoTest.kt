package com.abk.myip.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class IpInfoTest {

    private val baseline = IpInfo(
        ip = "203.0.113.7",
        city = "San Francisco",
        region = "California",
        country = "United States",
        countryCode = "US",
        timezone = "America/Los_Angeles",
        location = GeoLocation(latitude = 37.7749, longitude = -122.4194),
        postal = "94103",
    )

    @Test
    fun `data class equality holds for identical values`() {
        val copy = baseline.copy()
        assertEquals(baseline, copy)
    }

    @Test
    fun `data class equality fails when ip differs`() {
        val other = baseline.copy(ip = "198.51.100.42")
        assertNotEquals(baseline, other)
    }

    @Test
    fun `location is exposed as a GeoLocation`() {
        assertEquals(37.7749, baseline.location.latitude)
        assertEquals(-122.4194, baseline.location.longitude)
    }
}
