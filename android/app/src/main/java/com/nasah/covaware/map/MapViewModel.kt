package com.nasah.covaware.map

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.mapboxsdk.geometry.LatLng
import com.nasah.covaware.LOG_TAG
import com.nasah.covaware.data.HeatSquareDTO
import com.nasah.covaware.data.MapRepository
import com.nasah.covaware.data.toRisk
import com.nasah.covaware.reg.REG_SHARED_PREF_NAME
import com.nasah.covaware.reg.SHARED_PREF_AGE
import com.nasah.covaware.reg.SHARED_PREF_DISEASE
import com.nasah.covaware.reg.SHARED_PREF_VACCINE
import com.nasah.covaware.utils.generateMarkerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos

class MapViewModel(application: Application) : AndroidViewModel(application){

	var currentLocation: LatLng? = null

	private val repository: MapRepository by lazy { MapRepository() }
	private val sharedPref by lazy { application.getSharedPreferences(REG_SHARED_PREF_NAME, Activity.MODE_PRIVATE) }

	private var heatmapPreviousSquares: List<HeatMapSquare> = emptyList()

	private val isPlaceMenuOpen = MutableStateFlow(false)

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
					array[2] ?: 0.0,

					sharedPref.getInt(SHARED_PREF_AGE, 18),
					sharedPref.getBoolean(SHARED_PREF_DISEASE, false),
					sharedPref.getBoolean(SHARED_PREF_VACCINE, false)
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

	val isCloseButton = isPlaceMenuOpen.combine(_placesToVisit){ open, places ->
		open || places != null
	}

	val isPlacesList = isPlaceMenuOpen.combine(_placesToVisit){ open, places ->
		open && places == null
	}

	private val _chosenPlace = MutableStateFlow<PLaceToVisit?>(null)
	val chosenPlace: Flow<PLaceToVisit?> = _chosenPlace

	private val _currentRiskLevel = MutableStateFlow<Risk?>(null)
	val currentRiskLevel: Flow<Risk?> = merge(
		flow {
			 while (currentCoroutineContext().isActive){
				 currentLocation?.let { latLng ->
					 val array = rhombusFromCoordinates(
//						 LatLng(
//							 40.732345,
//							 -73.987333,
//						 ),
//						 LatLng(
//							 40.732345,
//							 -73.247333
//						 ),
						 latLng,
						 750
					 )
					 _currentRiskLevel.value = repository.getHeatSquares(
						 array[3] ?: 0.0,
						 array[1] ?: 0.0,
						 array[0] ?: 0.0,
						 array[2] ?: 0.0,

						 sharedPref.getInt(SHARED_PREF_AGE, 18),
						 sharedPref.getBoolean(SHARED_PREF_DISEASE, false),
						 sharedPref.getBoolean(SHARED_PREF_VACCINE, false)
					 )?.getOrNull(0)?.let {
					 	it.risk?.toRisk()
					 }
					 kotlinx.coroutines.delay(1000 * 60 * 60L)
				 }
				 kotlinx.coroutines.delay(1000L)
			 }
		},
		_currentRiskLevel
	)

	fun newCameraLocation(location: LatLng){
		Log.i(LOG_TAG, "newCameraLocation()")
		viewModelScope.launch {
			_actualCameraLocation.emit(location)
		}
	}

	fun onPlaceClick(id: String){
		_placesToVisit.value?.value?.find {
			it.id == id
		}?.let {
			_chosenPlace.value = it
		}
	}

	fun menuChange(){
		if(_chosenPlace.value != null){
			_chosenPlace.value = null
			return
		}
		if(_placesToVisit.value != null){
			_placesToVisit.value = null
			return
		}
		isPlaceMenuOpen.value = !isPlaceMenuOpen.value
	}

	private var findPlaceInMapJob: Job? = null

	fun findPlaceInMap(place: Place, currentLocation: LatLng?){
		currentLocation ?: return
		findPlaceInMapJob?.cancel()
		findPlaceInMapJob = viewModelScope.launch {
			_placesToVisit.value = repository.findPlaceInMap(
						currentLocation.latitude,
						currentLocation.longitude,
//				40.732345,
//				-73.987333,
//				40.732345,
//				-73.247333,
				place.id,

				sharedPref.getInt(SHARED_PREF_AGE, 18),
				sharedPref.getBoolean(SHARED_PREF_DISEASE, false),
				sharedPref.getBoolean(SHARED_PREF_VACCINE, false)
			)?.toPLacesToVisit(place)
		}
	}

	private fun rhombusFromCoordinates(location: LatLng, pDistanceInMeters: Int = 5000): Array<Double?>{
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
