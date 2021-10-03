package com.nasah.covaware.data

import retrofit2.http.GET
import retrofit2.http.Query

interface MapApi {

	@GET("/getHeatSquares")
	suspend fun getHeatSquares(
		@Query("top") top: Double,
		@Query("bottom") bottom: Double,
		@Query("left") left: Double,
		@Query("right") right: Double,
		@Query("age") age: Int,
		@Query("disease") disease: Boolean,
		@Query("vaccine") vaccine: Boolean
	): List<HeatSquareDTO>

	@GET("/testPlaces")
	suspend fun findPlaceInMap(
		@Query("latitude") latitude: Double,
		@Query("longtitude") longtitude: Double,
		@Query("placeType") placeType: String,
		@Query("age") age: Int,
		@Query("disease") disease: Boolean,
		@Query("vaccine") vaccine: Boolean
	): PlacesInMapDTO
}
