package com.abk.myip.web

import com.abk.myip.AbkMyIp
import com.abk.myip.BuildConfig
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement

fun main() {
    window.onload = {
        setText("version", BuildConfig.APP_VERSION)
        MainScope().launch {
            try {
                val app = AbkMyIp()
                val info = app.getMyIpInfo()
                val map = app.buildStaticMapUrl(info.location)

                setText("ip", info.ip)
                setText("city", "${info.city}, ${info.region}")
                setText("country", info.countryCode)
                setText("timezone", info.timezone)
                setText("coords", "${info.location.latitude}, ${info.location.longitude}")
                setText("org", info.org ?: "—")
                (document.getElementById("map") as? HTMLIFrameElement)?.src = map.value
            } catch (t: Throwable) {
                setText("error", "Lookup failed: ${t.message ?: t::class.simpleName}")
            }
        }
    }
}

private fun setText(elementId: String, value: String) {
    (document.getElementById(elementId) as? HTMLElement)?.textContent = value
}
