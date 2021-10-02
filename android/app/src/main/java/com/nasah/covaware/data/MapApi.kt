package com.nasah.covaware.data

import retrofit2.http.GET
import retrofit2.http.Query

interface MapApi {

	@GET("/getHeatSquares")
	suspend fun getHeatSquares(
		@Query("top") top: Double,
		@Query("bottom") bottom: Double,
		@Query("left") left: Double,
		@Query("right") right: Double
	): List<HeatSquareDTO>
}
