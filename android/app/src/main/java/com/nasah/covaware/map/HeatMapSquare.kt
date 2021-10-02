package com.nasah.covaware.map

import com.mapbox.mapboxsdk.geometry.LatLng

data class HeatMapSquare(
	val location: LatLng,
	val color: String
)
