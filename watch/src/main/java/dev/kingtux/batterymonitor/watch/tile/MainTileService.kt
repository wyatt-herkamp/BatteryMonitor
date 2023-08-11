package dev.kingtux.batterymonitor.watch.tile


import android.content.Context
import android.util.Log
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders.Tile
import com.google.android.gms.wearable.Wearable
import com.google.android.horologist.tiles.SuspendingTileService
import dagger.hilt.android.AndroidEntryPoint
import dev.kingtux.batterymonitor.watch.Devices

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
        Wearable.getMessageClient(applicationContext)
            .sendMessage("battery", "get-devices", ByteArray(0))

    }
    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): Tile {
        Log.d("BatteryLevelGetter-Watch", "Updated Watch: $currentBattery")
        Wearable.getMessageClient(applicationContext)
            .sendMessage("battery", "get-devices", ByteArray(0))

        return renderer.renderTimeline(getTileData(), requestParams)
    }
    private fun getTileData(): BatteryPercentTileRenderer.TileData {
        return BatteryPercentTileRenderer.TileData(
            currentBattery.watch, currentBattery.deviceOne, currentBattery.deviceTwo, currentBattery.deviceThree
        )
    }

    companion object {
        fun forceTileUpdate(applicationContext: Context) {
            getUpdater(applicationContext).requestUpdate(MainTileService::class.java)
        }
    }
}