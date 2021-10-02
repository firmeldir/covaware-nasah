package com.nasah.covaware.map

import android.util.Log
import androidx.lifecycle.coroutineScope
import com.mapbox.mapboxsdk.geometry.LatLng
import com.nasah.covaware.data.MapRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlin.math.cos

class MapActivity : OriginMapActivity() {

	private val repository: MapRepository by lazy { MapRepository() }

	private val _getLocation = MutableSharedFlow<LatLng>(
		extraBufferCapacity = 64,
		onBufferOverflow = BufferOverflow.DROP_OLDEST
	)

	override fun onMapFullyReady() {
		lifecycle.coroutineScope.launchWhenCreated {
			_getLocation.distinctUntilChanged().debounce(500).onEach { latLng ->
				Log.e("vlad", "request: $latLng")
				val array = rhombusFromCoordinates(latLng)
				val squares = repository.getHeatSquares(
					array[3] ?: 0.0,
					array[1] ?: 0.0,
					array[0] ?: 0.0,
					array[2] ?: 0.0
				)
				Log.e("vlad", "squares: $squares ")
				Log.e("vlad", "top: ${array[3]} bottom: ${array[1]} left: ${array[0]} right: ${array[2]}")
				pushPolygons(squares?.map { it.toHeatMapSquare() } ?: emptyList())
			}.launchIn(this)
		}
	}

	private fun rhombusFromCoordinates(location: LatLng): Array<Double?>{
		val pDistanceInMeters = 5000
		val array = Array<Double?>(4){ null }
		val latRadian: Double = Math.toRadians(location.latitude)
		val degLatKm = 110.574235
		val degLongKm = 110.572833 * cos(latRadian)
		val deltaLat = pDistanceInMeters / 1000.0 / degLatKm
		val deltaLong = pDistanceInMeters / 1000.0 / degLongKm
		array[0] = location.latitude - deltaLat   //left
		array[1] = location.longitude - deltaLong //bottom
		array[2] = location.latitude + deltaLat   //right
		array[3] = location.longitude + deltaLong //top
		return array
	}

	override fun onCameraIdle(currentCameraLocation: LatLng) {
		Log.e("vlad", "camera: $currentCameraLocation")
		lifecycle.coroutineScope.launchWhenStarted {
			_getLocation.emit(currentCameraLocation)
		}
	}
}
