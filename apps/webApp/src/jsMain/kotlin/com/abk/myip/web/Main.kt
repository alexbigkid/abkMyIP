package com.abk.myip.web

import com.abk.myip.AbkMyIp
import com.abk.myip.BuildConfig
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement

fun main() {
    window.onload = {
        setText("version", BuildConfig.APP_VERSION)
        MainScope().launch {
            val app = AbkMyIp()
            val info = app.getMyIpInfo()
            val map = app.buildStaticMapUrl(info.location)

            setText("ip", info.ip)
            setText("city", "${info.city}, ${info.region}")
            setText("country", "${info.country} (${info.countryCode})")
            setText("timezone", info.timezone)
            setText("coords", "${info.location.latitude}, ${info.location.longitude}")
            (document.getElementById("map") as? HTMLImageElement)?.src = map.value
        }
    }
}

private fun setText(elementId: String, value: String) {
    (document.getElementById(elementId) as? HTMLElement)?.textContent = value
}
