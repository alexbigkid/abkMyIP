package com.abk.myip.usecase

import com.abk.myip.domain.GeoLocation
import com.abk.myip.domain.StaticMapUrl
import kotlin.math.round

class BuildStaticMapUrlUseCase(
    private val span: Double = DEFAULT_SPAN,
) {
    operator fun invoke(location: GeoLocation): StaticMapUrl {
        val lat = location.latitude
        val lon = location.longitude
        val url = buildString {
            append(OSM_EMBED_BASE_URL)
            append("?bbox=")
            append((lon - span).toPlainString()).append(',')
            append((lat - span).toPlainString()).append(',')
            append((lon + span).toPlainString()).append(',')
            append((lat + span).toPlainString())
            append("&layer=mapnik")
            append("&marker=").append(lat.toPlainString()).append(',').append(lon.toPlainString())
        }
        return StaticMapUrl(url)
    }

    private fun Double.toPlainString(): String {
        val rounded = round(this * COORD_PRECISION) / COORD_PRECISION
        val asLong = rounded.toLong()
        return if (asLong.toDouble() == rounded) asLong.toString() else rounded.toString()
    }

    private companion object {
        const val OSM_EMBED_BASE_URL = "https://www.openstreetmap.org/export/embed.html"
        const val DEFAULT_SPAN = 0.05
        const val COORD_PRECISION = 10000.0
    }
}
