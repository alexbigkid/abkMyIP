package com.abk.myip.usecase

import com.abk.myip.domain.GeoLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildStaticMapUrlUseCaseTest {

    private val useCase = BuildStaticMapUrlUseCase()
    private val sf = GeoLocation(37.7749, -122.4194)

    @Test
    fun `produces an OpenStreetMap static map url`() {
        val url = useCase(sf).value

        assertTrue(url.startsWith("https://staticmap.openstreetmap.de/staticmap.php"))
    }

    @Test
    fun `url contains the requested coordinates as center`() {
        val url = useCase(sf).value

        assertTrue(url.contains("center=37.7749,-122.4194"), "url was: $url")
    }

    @Test
    fun `url contains the requested coordinates as a marker`() {
        val url = useCase(sf).value

        assertTrue(url.contains("markers=37.7749,-122.4194"), "url was: $url")
    }

    @Test
    fun `url uses the default zoom and size`() {
        val url = useCase(sf).value

        assertTrue(url.contains("zoom=12"), "url was: $url")
        assertTrue(url.contains("size=600x400"), "url was: $url")
    }

    @Test
    fun `negative coordinates are formatted without scientific notation`() {
        val antarctica = GeoLocation(-77.85, 166.6667)
        val url = useCase(antarctica).value

        assertEquals(true, url.contains("center=-77.85,166.6667"), "url was: $url")
    }
}
