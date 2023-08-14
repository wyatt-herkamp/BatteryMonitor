package dev.kingtux.batterymonitor.tile


import android.content.Context
import android.util.Log
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders.Tile
import com.google.android.horologist.tiles.SuspendingTileService
import dagger.hilt.android.AndroidEntryPoint
import dev.kingtux.batterymonitor.BatteryLevelGetter
import dev.kingtux.batterymonitor.Devices

import javax.inject.Inject

@AndroidEntryPoint
class MainTileService : SuspendingTileService() {
    private lateinit var renderer: BatteryPercentTileRenderer

    @Inject
    lateinit var currentBattery: Devices

    override fun onCreate() {
        super.onCreate()

        renderer = BatteryPercentTileRenderer(this)
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return renderer.produceRequestedResources(
            resourceState = Unit, requestParams = requestParams
        )
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)
        currentBattery.update(applicationContext)
    }
    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): Tile {
        Log.d("BatteryLevelGetter-Watch", "Updated Watch: $currentBattery")
        BatteryLevelGetter.refreshDevices(applicationContext)

        currentBattery.update(applicationContext)

        return renderer.renderTimeline(currentBattery, requestParams)
    }

    companion object {
        fun forceTileUpdate(applicationContext: Context) {
            getUpdater(applicationContext).requestUpdate(MainTileService::class.java)
        }
    }
}