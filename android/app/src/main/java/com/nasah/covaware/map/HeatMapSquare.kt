package com.nasah.covaware.map

import androidx.compose.runtime.Immutable
import com.mapbox.mapboxsdk.geometry.LatLng

@Immutable
data class HeatMapSquares(
	val value: List<HeatMapSquare>
)

@Immutable
data class HeatMapSquare(
	val location: LatLng,
	val color: String
)
