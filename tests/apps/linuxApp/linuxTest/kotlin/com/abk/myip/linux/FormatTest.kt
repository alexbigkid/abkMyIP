package com.abk.myip.linux

import com.abk.myip.domain.GeoLocation
import com.abk.myip.domain.IpInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormatTest {

    private val sample = IpInfo(
        ip = "206.84.81.141",
        city = "Medellín",
        region = "Antioquia",
        country = "Colombia",
        countryCode = "CO",
        timezone = "America/Bogota",
        location = GeoLocation(6.245, -75.5715),
        postal = "050001",
        org = "AS271957 SOMOS NETWORKS",
    )

    @Test
    fun `human format without color contains all expected fields`() {
        val out = formatHuman(sample, color = false)

        assertTrue(out.startsWith("206.84.81.141\n"), "first line should be the IP")
        assertTrue(out.contains("Medellín, Antioquia · CO"), "subtitle missing: $out")
        assertTrue(out.contains("Timezone     America/Bogota"), "timezone row missing or misaligned: $out")
        assertTrue(out.contains("ISP          AS271957 SOMOS NETWORKS"), "ISP row missing: $out")
        assertTrue(out.contains("Coordinates  6.2450, -75.5715"), "coords row missing or misformatted: $out")
    }

    @Test
    fun `human format without color emits no ANSI escapes`() {
        val out = formatHuman(sample, color = false)

        assertFalse(out.contains("["), "expected no ANSI escapes, got: $out")
    }

    @Test
    fun `human format with color emits ANSI escapes`() {
        val out = formatHuman(sample, color = true)

        assertTrue(out.contains("[1m"), "expected bold escape")
        assertTrue(out.contains("[36m"), "expected cyan escape")
        assertTrue(out.contains("[0m"), "expected reset escape")
    }

    @Test
    fun `human format omits ISP row when org is null`() {
        val out = formatHuman(sample.copy(org = null), color = false)

        assertFalse(out.contains("ISP"), "ISP row should be hidden when org is null")
        assertTrue(out.contains("Timezone"), "other rows still present")
    }

    @Test
    fun `human format omits ISP row when org is blank`() {
        val out = formatHuman(sample.copy(org = ""), color = false)

        assertFalse(out.contains("ISP"), "ISP row should be hidden when org is blank")
    }

    @Test
    fun `json format contains all expected fields`() {
        val out = formatJson(sample).trimEnd()

        assertEquals(
            """{"ip":"206.84.81.141","city":"Medellín","region":"Antioquia","country":"Colombia","countryCode":"CO","timezone":"America/Bogota","latitude":6.245,"longitude":-75.5715,"postal":"050001","org":"AS271957 SOMOS NETWORKS"}""",
            out,
        )
    }

    @Test
    fun `json format encodes null org`() {
        val out = formatJson(sample.copy(org = null))

        assertTrue(out.contains("\"org\":null"), "expected null org, got: $out")
    }

    @Test
    fun `json format escapes special characters`() {
        val out = formatJson(sample.copy(city = "He said \"hi\"\nthen left"))

        assertTrue(out.contains("\"city\":\"He said \\\"hi\\\"\\nthen left\""), "quotes/newline not escaped: $out")
    }

    @Test
    fun `fmt4 rounds half up at the fourth decimal`() {
        assertEquals("6.2450", fmt4(6.245))
        assertEquals("-75.5715", fmt4(-75.5715))
        assertEquals("0.0000", fmt4(0.0))
        assertEquals("1.2346", fmt4(1.23456))
    }
}
