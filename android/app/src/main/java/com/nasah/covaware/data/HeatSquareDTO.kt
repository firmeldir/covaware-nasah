package com.nasah.covaware.data

import android.util.Log
import com.google.gson.annotations.SerializedName
import com.mapbox.mapboxsdk.geometry.LatLng
import com.nasah.covaware.map.HeatMapSquare
import java.util.*
import kotlin.random.Random

private const val MAX_HEAT_VALUE = 60000
private const val MIN_HEAT_VALUE = 0

private val COLORS = listOf(
	"#99FF00",
	"#9EFF00",
	"#A2FF00",
	"#A7FF00",
	"#ACFF00",
	"#B0FF00",
	"#B9FF00",
	"#BEFF00",
	"#C3FF00",
	"#CCFF00",
	"#D1FF00",
	"#D5FF00",
	"#DAFF00",
	"#DFFF00",
	"#E3FF00",
	"#E8FF00",
	"#ECFF00",
	"#F1FF00",
	"#F6FF00",
	"#FAFF00",
	"#FFFF00",
	"#FFFA00",
	"#FFF600",
	"#FFF100",
	"#FFEC00",
	"#FFE800",
	"#FFE300",
	"#FFDF00",
	"#FFDA00",
	"#FFD500",
	"#FFD100",
	"#FFCC00",
	"#FFCC00",
	"#FFC300",
	"#FFB900",
	"#FFB500",
	"#FFB000",
	"#FFAC00",
	"#FFA700",
	"#FF9900",
	"#FF9900",
	"#FF9400",
	"#FF8B00",
	"#FF8200",
	"#FF7900",
	"#FF6F00",
	"#FF6B00",
	"#FF6600",
	"#FF6600",
	"#FF6100",
	"#FF5D00",
	"#FF5800",
	"#FF4F00",
	"#FF4600",
	"#FF3C00",
	"#FF3300",
	"#FF2A00",
).reversed()

private val PERIOD = (MAX_HEAT_VALUE - MIN_HEAT_VALUE) / COLORS.size

private val BORDERS = COLORS.mapIndexed { index, _ ->
	PERIOD * (COLORS.size - 1 - index)
} // 10 9 8 7

data class HeatSquareDTO(
	@SerializedName("density") val density: Double? = null,
	@SerializedName("latitude") val latitude: Double? = null,
	@SerializedName("longtitude") val longtitude: Double? = null
){
	fun toHeatMapSquare(): HeatMapSquare {
		return HeatMapSquare(
			location = LatLng(latitude ?: 0.0, longtitude ?: 0.0),
			color = COLORS[BORDERS.indexOfFirst { (density ?: 0.0) >= it }]
		)
	}
}
