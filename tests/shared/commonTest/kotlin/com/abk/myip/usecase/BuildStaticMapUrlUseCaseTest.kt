package com.abk.myip.usecase

import com.abk.myip.domain.GeoLocation
import kotlin.test.Test
import kotlin.test.assertTrue

class BuildStaticMapUrlUseCaseTest {

    private val useCase = BuildStaticMapUrlUseCase()
    private val sf = GeoLocation(37.7749, -122.4194)

    @Test
    fun `produces an OpenStreetMap embed url`() {
        val url = useCase(sf).value

        assertTrue(url.startsWith("https://www.openstreetmap.org/export/embed.html"), "url was: $url")
    }

    @Test
    fun `url contains the requested coordinates as a marker`() {
        val url = useCase(sf).value

        assertTrue(url.contains("marker=37.7749,-122.4194"), "url was: $url")
    }

    @Test
    fun `url uses the mapnik tile layer`() {
        val url = useCase(sf).value

        assertTrue(url.contains("layer=mapnik"), "url was: $url")
    }

    @Test
    fun `url bbox surrounds the marker by the default span`() {
        val url = useCase(sf).value

        // SF (37.7749, -122.4194) with default span 0.05 → bbox = west,south,east,north
        assertTrue(url.contains("bbox=-122.4694,37.7249,-122.3694,37.8249"), "url was: $url")
    }

    @Test
    fun `negative coordinates are formatted without scientific notation`() {
        val antarctica = GeoLocation(-77.85, 166.6667)
        val url = useCase(antarctica).value

        assertTrue(url.contains("marker=-77.85,166.6667"), "url was: $url")
    }

    @Test
    fun `whole-number coordinates render without a trailing decimal`() {
        val nullIsland = GeoLocation(0.0, 0.0)
        val url = useCase(nullIsland).value

        assertTrue(url.contains("marker=0,0"), "url was: $url")
    }
}
