package dev.kingtux.batterymonitor.wear.tile


import android.content.Context
import android.util.Log
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders.Tile
import com.google.android.horologist.tiles.SuspendingTileService
import dagger.hilt.android.AndroidEntryPoint
import dev.kingtux.batterymonitor.wear.BatteryLevelGetter
import dev.kingtux.batterymonitor.wear.Devices

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
        BatteryLevelGetter.refreshDevices(applicationContext)
        currentBattery.updateAllFromDataStore(applicationContext)
        Log.d(TAG, "onTileEnterEvent: $currentBattery")
    }
    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): Tile {
        BatteryLevelGetter.refreshDevices(applicationContext)
        currentBattery.updateAllFromDataStore(applicationContext)
        Log.d(TAG, "onTileEnterEvent: $currentBattery")
        return renderer.renderTimeline(currentBattery, requestParams)
    }

    companion object {
        const val TAG = "BatteryLevelGetter-Tile"
        fun forceTileUpdate(applicationContext: Context) {
            getUpdater(applicationContext).requestUpdate(MainTileService::class.java)
        }
    }
}