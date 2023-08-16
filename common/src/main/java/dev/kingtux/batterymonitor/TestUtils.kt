package dev.kingtux.batterymonitor

import java.util.Random

fun randomMACAddress(): String {

    val rand = Random()
    val macAddr = ByteArray(6)
    rand.nextBytes(macAddr)
    macAddr[0] = (macAddr[0].toInt() and 254.toByte()
        .toInt()).toByte()
    val sb = StringBuilder(18)
    for (b in macAddr) {
        if (sb.isNotEmpty()) sb.append(":")
        sb.append(String.format("%02x", b))
    }
    return sb.toString()
}