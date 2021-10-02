package com.nasah.covaware.data

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query

class MapRepository {

	private val retrofit = Retrofit.Builder()
		.baseUrl("https://desolate-thicket-63889.herokuapp.com")
		.addConverterFactory(NullOnEmptyConverterFactory())
		.addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
		.build()

	private val api: MapApi = retrofit.create(MapApi::class.java)

	suspend fun getHeatSquares(
		top: Double,
		bottom: Double,
		left: Double,
		right: Double,
	): List<HeatSquareDTO>?{
		return kotlin.runCatching {
			api.getHeatSquares(top, bottom, left, right)
		}.getOrNull()
	}

	suspend fun findPlaceInMap(
		latitude: Double,
		longtitude: Double,
		placeType: String
	): PlacesInMapDTO?{
		return kotlin.runCatching {
			api.findPlaceInMap(latitude, longtitude, placeType)
		}.getOrNull()
	}
}
