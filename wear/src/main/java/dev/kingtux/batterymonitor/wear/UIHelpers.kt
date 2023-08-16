package dev.kingtux.batterymonitor.wear

import androidx.compose.ui.graphics.Color
import androidx.wear.protolayout.ColorBuilders.ColorProp
import dev.kingtux.batterymonitor.SharedDevice

const val BELOW_20 = 0xffff0000
const val BELOW_50 = 0xffffa500
const val ABOVE_50 = 0xff00ff00
const val TRACK_BACKGROUND= 0xff000000
fun SharedDevice.getColor(): Color{
    return if (batteryLevelOrZero() > 50) {
        Color(ABOVE_50)
    } else if (batteryLevelOrZero() < 20) {
        Color(BELOW_20)
    } else {
        Color(BELOW_50)
    }
}

fun SharedDevice.getTileColor(): ColorProp{
    return if (batteryLevelOrZero() > 50) {
        ColorProp.Builder(ABOVE_50.toInt()).build()
    } else if (batteryLevelOrZero() < 20) {
        ColorProp.Builder(BELOW_20.toInt()).build()
    } else {
        ColorProp.Builder(BELOW_50.toInt()).build()
    }
}