package com.abk.myip.usecase

import com.abk.myip.domain.GeoLocation
import com.abk.myip.domain.StaticMapUrl

class BuildStaticMapUrlUseCase(
    private val zoom: Int = DEFAULT_ZOOM,
    private val widthPx: Int = DEFAULT_WIDTH,
    private val heightPx: Int = DEFAULT_HEIGHT,
) {
    operator fun invoke(location: GeoLocation): StaticMapUrl {
        val lat = location.latitude.toPlainString()
        val lon = location.longitude.toPlainString()
        val url = buildString {
            append(STATIC_MAP_BASE_URL)
            append("?center=").append(lat).append(',').append(lon)
            append("&zoom=").append(zoom)
            append("&size=").append(widthPx).append('x').append(heightPx)
            append("&markers=").append(lat).append(',').append(lon).append(",red")
        }
        return StaticMapUrl(url)
    }

    private fun Double.toPlainString(): String {
        val asLong = toLong()
        return if (asLong.toDouble() == this) asLong.toString() else toString()
    }

    private companion object {
        const val STATIC_MAP_BASE_URL = "https://staticmap.openstreetmap.de/staticmap.php"
        const val DEFAULT_ZOOM = 12
        const val DEFAULT_WIDTH = 600
        const val DEFAULT_HEIGHT = 400
    }
}
