package com.nasah.covaware.map

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.mapboxsdk.geometry.LatLng
import com.nasah.covaware.LOG_TAG
import com.nasah.covaware.data.HeatSquareDTO
import com.nasah.covaware.data.MapRepository
import com.nasah.covaware.utils.generateMarkerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.cos

class MapViewModel : ViewModel(){

	private val repository: MapRepository by lazy { MapRepository() }

	private var heatmapPreviousSquares: List<HeatMapSquare> = emptyList()

	val isPlaceMenuOpen = MutableStateFlow(false)

	private val _actualCameraLocation = MutableSharedFlow<LatLng>(extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST)
	val heatmapSquares: Flow<HeatMapSquares> = _actualCameraLocation
		.distinctUntilChanged()
		.debounce(500)
		.flatMapLatest { latLng ->
			flow {
				val array = rhombusFromCoordinates(latLng)
				val squares = repository.getHeatSquares(
					array[3] ?: 0.0,
					array[1] ?: 0.0,
					array[0] ?: 0.0,
					array[2] ?: 0.0
				)
				emit(squares)
			}
		}
		.map { list ->
			list?.map { it.toHeatMapSquare() } ?: emptyList()
		}
		.map { list ->
			HeatMapSquares((list + heatmapPreviousSquares).distinctBy { it.location }).also {
				heatmapPreviousSquares = it.value
			}
		}

	private val _placesToVisit = MutableStateFlow<PLacesToVisit?>(null)
	val placesToVisit = _placesToVisit.map { it ?: PLacesToVisit(emptyList()) }

	fun newCameraLocation(location: LatLng){
		Log.i(LOG_TAG, "newCameraLocation()")
		viewModelScope.launch {
			_actualCameraLocation.emit(location)
		}
	}

	fun menuChange(){
		isPlaceMenuOpen.value = !isPlaceMenuOpen.value
	}

	private var findPlaceInMapJob: Job? = null

	fun findPlaceInMap(place: Place, currentLocation: LatLng?){
		currentLocation ?: return
		findPlaceInMapJob?.cancel()
		findPlaceInMapJob = viewModelScope.launch {
			_placesToVisit.value = repository.findPlaceInMap(
//						currentLocation.latitude,
//						currentLocation.longitude,
				40.732345,
				-73.987333,
				place.id
			)?.toPLacesToVisit(place)
		}
	}

	fun clearPlaces(){
		_placesToVisit.value = null
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
}
