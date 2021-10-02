package com.nasah.covaware.map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GpsStatusListener(private val context: Context) {

	fun asFlow() : Flow<GpsStatus> = callbackFlow {

		fun postGpsStatus() = sendBlocking(GpsStatus.get(isLocationEnabled))

		val gpsSwitchStatusReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context, intent: Intent) = postGpsStatus()
		}

		postGpsStatus()

		fun registerReceiver() = context.registerReceiver(
			gpsSwitchStatusReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
		)

		fun unregisterReceiver() = context.unregisterReceiver(gpsSwitchStatusReceiver)

		registerReceiver()

		awaitClose { unregisterReceiver() }
	}


	private val isLocationEnabled get() = context.getSystemService(LocationManager::class.java).isProviderEnabled(LocationManager.GPS_PROVIDER)
}
