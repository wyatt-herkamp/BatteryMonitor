package dev.kingtux.batterymonitor.phone.settings

import dev.kingtux.batterymonitor.phone.ui.Theme
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    var theme: Theme = Theme.SYSTEM,
)