package com.abk.myip.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IpInfoDtoTest {

    private val json = Json { encodeDefaults = false }

    private val fullDto = IpInfoDto(
        ip = "203.0.113.7",
        city = "San Francisco",
        region = "California",
        country = "US",
        loc = "37.7749,-122.4194",
        timezone = "America/Los_Angeles",
        postal = "94103",
        org = "AS141039 PacketHub S.A.",
    )

    @Test
    fun `serializes with both optional fields populated`() {
        val out = json.encodeToString(fullDto)

        assertTrue(out.contains("\"postal\":\"94103\""), "postal should be written when non-null: $out")
        assertTrue(out.contains("\"org\":\"AS141039 PacketHub S.A.\""), "org should be written when non-null: $out")
    }

    @Test
    fun `serializes with org null (omits the field when default)`() {
        val out = json.encodeToString(fullDto.copy(org = null))

        assertTrue(out.contains("\"postal\":\"94103\""), "postal should still appear: $out")
        assertTrue(!out.contains("\"org\""), "org should be omitted when default-null: $out")
    }

    @Test
    fun `serializes with postal null (omits the field when default)`() {
        val out = json.encodeToString(fullDto.copy(postal = null))

        assertTrue(!out.contains("\"postal\""), "postal should be omitted when default-null: $out")
        assertTrue(out.contains("\"org\":\"AS141039 PacketHub S.A.\""), "org should still appear: $out")
    }

    @Test
    fun `serializes with both optionals null (omits both fields)`() {
        val out = json.encodeToString(fullDto.copy(postal = null, org = null))

        assertTrue(!out.contains("\"postal\""), "postal should be omitted: $out")
        assertTrue(!out.contains("\"org\""), "org should be omitted: $out")
        assertTrue(out.contains("\"ip\":\"203.0.113.7\""), "required field ip should still be present: $out")
    }

    @Test
    fun `encodeDefaults true forces optional fields to be written even when null`() {
        val forced = Json { encodeDefaults = true }
        val out = forced.encodeToString(fullDto.copy(postal = null, org = null))

        assertTrue(out.contains("\"postal\":null"), "postal=null should be written: $out")
        assertTrue(out.contains("\"org\":null"), "org=null should be written: $out")
    }

    @Test
    fun `encodeDefaults true with non-null optionals writes their values`() {
        val forced = Json { encodeDefaults = true }
        val out = forced.encodeToString(fullDto)

        assertTrue(out.contains("\"postal\":\"94103\""), "postal should be written: $out")
        assertTrue(out.contains("\"org\":\"AS141039 PacketHub S.A.\""), "org should be written: $out")
    }

    @Test
    fun `round-trip preserves all fields`() {
        val out = json.encodeToString(fullDto)
        val back = json.decodeFromString<IpInfoDto>(out)

        assertEquals(fullDto, back)
    }
}
