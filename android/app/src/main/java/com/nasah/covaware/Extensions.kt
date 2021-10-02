package com.nasah.covaware

import android.location.Location
import com.mapbox.mapboxsdk.geometry.LatLng

fun Location.toLatLng() = LatLng(latitude, longitude)

enum class Direction {
	NORTH, SOUTH, EAST, WEST
}

fun squareFromCoordinates(location: LatLng): Array<LatLng>{
	return arrayOf(
		addDistanceInMeters(location.latitude, location.longitude, 500, Direction.NORTH),
		addDistanceInMeters(location.latitude, location.longitude, 500, Direction.SOUTH),
		addDistanceInMeters(location.latitude, location.longitude, 500, Direction.EAST),
		addDistanceInMeters(location.latitude, location.longitude, 500, Direction.WEST)
	)
}

private fun addDistanceInMeters(
	latitude: Double,
	longitude: Double,
	distanceInMeters: Int,
	direction: Direction
): LatLng {
	val equatorCircumference = 6371000
	val polarCircumference = 6356800

	val mPerDegLong = (360 / polarCircumference.toDouble())
	val radLat = latitude * Math.PI / 180
	val mPerDegLat = 360 / (Math.cos(radLat) * equatorCircumference)

	val degDiffLong = distanceInMeters * mPerDegLong
	val degDiffLat = distanceInMeters * mPerDegLat

	val xxNorthLat = latitude + degDiffLong
	val xxSouthLat = latitude - degDiffLong
	val xxEastLong = longitude + degDiffLat
	val xxWestLong = longitude - degDiffLat

	return when (direction) {
		Direction.NORTH -> LatLng(xxNorthLat, longitude)
		Direction.SOUTH -> LatLng(xxSouthLat, longitude)
		Direction.EAST -> LatLng(latitude, xxEastLong)
		Direction.WEST -> LatLng(latitude, xxWestLong)
	}
}
