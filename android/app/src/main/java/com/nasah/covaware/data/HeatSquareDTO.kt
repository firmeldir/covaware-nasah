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
	"#FFFF00",
	"#ACFF00",
	"#BEFF00",
	"#D1FF00",
	"#E3FF00",
	"#F6FF00",
	"#FFFF00",
	"#FFE300",
	"#FFC700",
	"#FFAC00",
	"#FF9000",
	"#FF7400",
	"#FF6600",
	"#FF5300",
	"#FF4100",
	"#FF2E00",
	"#FF1C00",
	"#FF0900",
	"#FF0000",
	"#EC0000",
	"#DA0000",
	"#C70000",
	"#B50000",
	"#A20000"
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
