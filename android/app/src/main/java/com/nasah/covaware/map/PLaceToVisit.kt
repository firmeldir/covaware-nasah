package com.nasah.covaware.map

import androidx.compose.runtime.Immutable
import com.mapbox.mapboxsdk.geometry.LatLng

enum class Risk (val riskName: String, val color: String){
	EXTREMELY_HIGH("Extremely high", "#990000"),
	HIGH("High", "#FF0000"),
	MODERATE("Moderate", "#FF6600"),
	LOW("Low", "#FFFF00"),
	MINIMAL("Minimal", "#99FF00"),
}

@Immutable
data class PLacesToVisit(
	val value: List<PLaceToVisit>
)

fun riskTypeId(risk: Risk, type: Place) = type.name + "_" + risk.riskName

@Immutable
data class PLaceToVisit(
	val location: LatLng,
	val name: String,
	val id: String,
	val risk: Risk,
	val type: Place,
	val recommendedHours: List<RecommendedHour>
)

@Immutable
data class RecommendedHour(
	val risk: Risk,
	val timeText: String
)

