package dev.kingtux.batterymonitor.wear.tile


import android.content.Context
import android.util.Log
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders.Tile
import com.google.android.horologist.tiles.SuspendingTileService
import dagger.hilt.android.AndroidEntryPoint
import dev.kingtux.batterymonitor.wear.BatteryMonitorWearApp
import dev.kingtux.batterymonitor.wear.Devices

import javax.inject.Inject

class MainTileService : SuspendingTileService() {
    private lateinit var renderer: BatteryPercentTileRenderer

    private var currentBattery: Devices = Devices()
    private var tileData = BatteryPercentTileRenderer.TileData(currentBattery)
    init {
        currentBattery.updateOccurred = {
            tileData = BatteryPercentTileRenderer.TileData(currentBattery)
            Log.d(TAG, "updateOccurred: $currentBattery $tileData")
            forceTileUpdate(applicationContext)
        }
    }


    override fun onCreate() {
        super.onCreate()
        currentBattery.onCreate(applicationContext)
        renderer = BatteryPercentTileRenderer(this)
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return renderer.produceRequestedResources(
            resourceState = Unit, requestParams = requestParams
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        currentBattery.onDestroy(applicationContext)
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)
        BatteryMonitorWearApp.refreshDevices(applicationContext)
        tileData = BatteryPercentTileRenderer.TileData(currentBattery)
        Log.d(TAG, "onTileEnterEvent: $currentBattery")
    }

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): Tile {
        if (requestParams.currentState.lastClickableId == CLICK) {
            Log.d(TAG, "tileRequest: Clicked")
            // Launch the app
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
        }
        BatteryMonitorWearApp.refreshDevices(applicationContext)
        tileData = BatteryPercentTileRenderer.TileData(currentBattery)
        Log.d(TAG, "tileRequest: $tileData")
        return renderer.renderTimeline(
            tileData,
            requestParams
        )
    }

    companion object {
        const val TAG = "BatteryLevelGetter-Tile"
        const val CLICK = "click"
        fun forceTileUpdate(applicationContext: Context) {
            getUpdater(applicationContext).requestUpdate(MainTileService::class.java)
        }
    }
}