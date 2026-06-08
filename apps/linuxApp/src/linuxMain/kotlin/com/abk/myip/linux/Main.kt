package com.abk.myip.linux

import com.abk.myip.AbkMyIp
import com.abk.myip.BuildConfig
import com.abk.myip.domain.IpInfo
import com.abk.myip.silenceDebugLogs
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import platform.posix.fputs
import platform.posix.stderr
import kotlin.system.exitProcess

private const val USAGE = """abkMyIP — show this machine's public IP and geolocation

Usage:  abkmyip [options]

Options:
  -h, --help        Show this help and exit
  -v, --version     Show version and exit
      --json        Output as JSON instead of human-readable text
      --no-color    Disable ANSI colors in human output
"""

fun main(args: Array<String>) {
    silenceDebugLogs()
    when {
        "--help" in args || "-h" in args -> {
            println(USAGE.trimEnd())
            return
        }
        "--version" in args || "-v" in args -> {
            println("abkMyIP ${BuildConfig.APP_VERSION}")
            return
        }
    }

    val json = "--json" in args
    val color = "--no-color" !in args && !json

    val info = try {
        runBlocking { AbkMyIp().getMyIpInfo() }
    } catch (t: Throwable) {
        eprintln("error: ${t.message ?: t::class.simpleName ?: "unknown failure"}")
        exitProcess(1)
    }

    print(if (json) formatJson(info) else formatHuman(info, color))
}

@OptIn(ExperimentalForeignApi::class)
private fun eprintln(message: String) {
    fputs("$message\n", stderr)
}

internal fun formatHuman(info: IpInfo, color: Boolean): String {
    val reset = if (color) "[0m" else ""
    val bold = if (color) "[1m" else ""
    val dim = if (color) "[2m" else ""
    val cyan = if (color) "[36m" else ""
    val sb = StringBuilder()
    sb.appendLine("$bold$cyan${info.ip}$reset")
    sb.appendLine("$dim${info.city}, ${info.region} · ${info.countryCode}$reset")
    sb.appendLine()
    val rows = buildList {
        add("Timezone" to info.timezone)
        info.org?.takeIf { it.isNotEmpty() }?.let { add("ISP" to it) }
        add("Coordinates" to formatCoords(info.location.latitude, info.location.longitude))
    }
    val labelWidth = rows.maxOf { it.first.length }
    rows.forEach { (label, value) ->
        sb.appendLine("  $dim${label.padEnd(labelWidth)}$reset  $value")
    }
    return sb.toString()
}

internal fun formatJson(info: IpInfo): String = buildString {
    append('{')
    append("\"ip\":").append(jsonStr(info.ip)).append(',')
    append("\"city\":").append(jsonStr(info.city)).append(',')
    append("\"region\":").append(jsonStr(info.region)).append(',')
    append("\"country\":").append(jsonStr(info.country)).append(',')
    append("\"countryCode\":").append(jsonStr(info.countryCode)).append(',')
    append("\"timezone\":").append(jsonStr(info.timezone)).append(',')
    append("\"latitude\":").append(info.location.latitude).append(',')
    append("\"longitude\":").append(info.location.longitude).append(',')
    append("\"postal\":").append(jsonStr(info.postal)).append(',')
    append("\"org\":").append(info.org?.let(::jsonStr) ?: "null")
    append("}\n")
}

private fun jsonStr(s: String): String {
    val escaped = StringBuilder(s.length + 2)
    escaped.append('"')
    for (ch in s) {
        when (ch) {
            '\\' -> escaped.append("\\\\")
            '"' -> escaped.append("\\\"")
            '\n' -> escaped.append("\\n")
            '\r' -> escaped.append("\\r")
            '\t' -> escaped.append("\\t")
            else -> if (ch.code < 0x20) {
                escaped.append("\\u").append(ch.code.toString(16).padStart(4, '0'))
            } else {
                escaped.append(ch)
            }
        }
    }
    escaped.append('"')
    return escaped.toString()
}

private fun formatCoords(lat: Double, lon: Double): String = "${fmt4(lat)}, ${fmt4(lon)}"

internal fun fmt4(value: Double): String {
    val negative = value < 0
    val abs = if (negative) -value else value
    val scaled = (abs * 10000.0 + 0.5).toLong()
    val whole = scaled / 10000
    val frac = scaled % 10000
    val sign = if (negative) "-" else ""
    return "$sign$whole.${frac.toString().padStart(4, '0')}"
}
