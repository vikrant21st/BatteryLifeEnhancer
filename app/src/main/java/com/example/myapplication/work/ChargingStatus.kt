package com.example.myapplication.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ChargingStatus(
    private val context: Context,
) {
    var isCharging = false
        private set

    var levelPercentage = -1
        private set

    init {
        reload()
    }

    private fun Intent.getLevelPercentage(): Int {
        val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        return level * 100 / scale
    }

    private fun Intent.isCharging() =
        getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            .let { chargePlug ->
                chargePlug == BatteryManager.BATTERY_PLUGGED_USB ||
                        chargePlug == BatteryManager.BATTERY_PLUGGED_AC
            }

    private fun reload() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                context ?: return
                intent ?: return
                isCharging = intent.isCharging()
                levelPercentage = intent.getLevelPercentage()
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        runBlocking {
            withTimeoutOrNull(1.seconds) {
                while (levelPercentage == -1)
                    delay(100.milliseconds)
            }
        }
        context.unregisterReceiver(receiver)
    }
}
