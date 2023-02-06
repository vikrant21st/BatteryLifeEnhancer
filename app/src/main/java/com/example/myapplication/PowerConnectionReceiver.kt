package com.example.myapplication

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

typealias SaveBatteryStatus = (isCharging: Boolean, levelPercentage: Int) -> Unit

class PowerConnectionReceiver(
    private val onComplete: SaveBatteryStatus,
) : BroadcastReceiver() {
    var isCharging = false
        private set
    var level = 0
        private set

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        isCharging = intent.isCharging()
        level = intent.getLevelPercentage()
        onComplete(isCharging, level)
    }

    companion object {
        private fun Intent.getLevelPercentage(): Int {
            val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            return level * 100 / scale
        }

        private fun Intent.isCharging(): Boolean {
            val chargePlug = getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

            return chargePlug == BatteryManager.BATTERY_PLUGGED_USB ||
                    chargePlug == BatteryManager.BATTERY_PLUGGED_AC
        }
    }
}
