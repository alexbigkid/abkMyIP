package com.abk.myip.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeoLocationTest {

    @Test
    fun `accepts coordinates within valid range`() {
        val loc = GeoLocation(latitude = 0.0, longitude = 0.0)
        assertEquals(0.0, loc.latitude)
        assertEquals(0.0, loc.longitude)
    }

    @Test
    fun `accepts boundary coordinates`() {
        val northPole = GeoLocation(latitude = 90.0, longitude = 180.0)
        val southPole = GeoLocation(latitude = -90.0, longitude = -180.0)
        assertEquals(90.0, northPole.latitude)
        assertEquals(-180.0, southPole.longitude)
    }

    @Test
    fun `rejects latitude above 90`() {
        assertFailsWith<IllegalArgumentException> {
            GeoLocation(latitude = 90.0001, longitude = 0.0)
        }
    }

    @Test
    fun `rejects latitude below minus 90`() {
        assertFailsWith<IllegalArgumentException> {
            GeoLocation(latitude = -90.0001, longitude = 0.0)
        }
    }

    @Test
    fun `rejects longitude above 180`() {
        assertFailsWith<IllegalArgumentException> {
            GeoLocation(latitude = 0.0, longitude = 180.0001)
        }
    }

    @Test
    fun `rejects longitude below minus 180`() {
        assertFailsWith<IllegalArgumentException> {
            GeoLocation(latitude = 0.0, longitude = -180.0001)
        }
    }
}
