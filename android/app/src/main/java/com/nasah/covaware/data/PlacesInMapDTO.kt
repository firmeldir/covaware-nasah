package com.nasah.covaware.data

import android.util.Log
import com.google.gson.annotations.SerializedName
import com.mapbox.mapboxsdk.geometry.LatLng
import com.nasah.covaware.map.*
import kotlin.random.Random

data class PlacesInMapDTO(
    @SerializedName("city")
    val city: String?,
    @SerializedName("county")
    val county: String?,
    @SerializedName("places")
    val places: List<Place>?
){
    fun toPLacesToVisit(type: com.nasah.covaware.map.Place): PLacesToVisit {
        return PLacesToVisit(
            places?.map { place ->
                val risk: Risk = place.risk?.let {
                    when {
                        it > 80 -> Risk.EXTREMELY_HIGH
                        it > 60 -> Risk.HIGH
                        it > 40 -> Risk.MODERATE
                        it > 20 -> Risk.LOW
                        else -> Risk.MINIMAL
                    }
                } ?: Risk.values()[Random.nextInt(0, 5)]

                Log.e("vlad-s", "--- ${place.name}")
                Log.e("vlad-s", "PLaceToVisit: ${place.hours?.week}")

                PLaceToVisit(
                    risk = risk,
                    location = LatLng(place.geometry!!.lat!!, place.geometry.lng!!),
                    name = place.name!!,
                    id = place.placeId!!,
                    type = type,
                    recommendedHours =
                    place.hours?.week?.let { weeks ->
                        val hours = mutableListOf<RecommendedHour>()
                        //on weekends
                        weeks.getOrNull(0)?.hours?.let { list ->
                            for(i in listOf(0, 3, 6, 9, 12, 15, 18, 21)){
                                list.find{ it.hour == i }?.let {
                                    if(it.percentage != 0) {
                                        val risk = when {
                                            it.percentage!! > 80 -> Risk.EXTREMELY_HIGH
                                            it.percentage > 60 -> Risk.HIGH
                                            it.percentage > 40 -> Risk.MODERATE
                                            it.percentage > 20 -> Risk.LOW
                                            else -> Risk.MINIMAL
                                        }

                                        hours.add(
                                            RecommendedHour(
                                                risk = risk,
                                                timeText = "on weekends at $i o'clock"
                                            )
                                        )
                                    }
                                }
                            }
                        } ?: kotlin.run {
                            weeks.getOrNull(6)?.hours?.let { list ->
                                for(i in listOf(0, 3, 6, 9, 12, 15, 18, 21)){
                                    list.find{ it.hour == i }?.let {
                                        if(it.percentage != 0){
                                            val risk = when{
                                                it.percentage!! > 80 -> Risk.EXTREMELY_HIGH
                                                it.percentage > 60 -> Risk.HIGH
                                                it.percentage > 40 -> Risk.MODERATE
                                                it.percentage > 20 -> Risk.LOW
                                                else -> Risk.MINIMAL
                                            }

                                            hours.add(
                                                RecommendedHour(
                                                    risk = risk,
                                                    timeText = "on weekdays at $i o'clock"
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        //on weekdays
                        weeks.getOrNull(1)?.hours?.let { list ->
                            for(i in listOf(0, 3, 6, 9, 12, 15, 18, 21)){
                                list.find{ it.hour == i }?.let {
                                    if(it.percentage != 0){
                                        val risk = when{
                                            it.percentage!! > 80 -> Risk.EXTREMELY_HIGH
                                            it.percentage > 60 -> Risk.HIGH
                                            it.percentage > 40 -> Risk.MODERATE
                                            it.percentage > 20 -> Risk.LOW
                                            else -> Risk.MINIMAL
                                        }

                                        hours.add(
                                            RecommendedHour(
                                                risk = risk,
                                                timeText = "on weekdays at $i o'clock"
                                            )
                                        )
                                    }
                                }
                            }
                        } ?: kotlin.run {
                            weeks.getOrNull(2)?.hours?.let { list ->
                                for(i in listOf(0, 3, 6, 9, 12, 15, 18, 21)){
                                    list.find{ it.hour == i }?.let {
                                        if(it.percentage != 0){
                                            val risk = when{
                                                it.percentage!! > 80 -> Risk.EXTREMELY_HIGH
                                                it.percentage > 60 -> Risk.HIGH
                                                it.percentage > 40 -> Risk.MODERATE
                                                it.percentage > 20 -> Risk.LOW
                                                else -> Risk.MINIMAL
                                            }

                                            hours.add(
                                                RecommendedHour(
                                                    risk = risk,
                                                    timeText = "on weekdays at $i o'clock"
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        hours
                    } ?: emptyList()
                )
            } ?: emptyList()
        )
    }
}

data class Place(
    @SerializedName("geometry")
    val geometry: Geometry?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("place_id")
    val placeId: String?,
    @SerializedName("hours")
    val hours: Hours?,
    @SerializedName("risk")
    val risk: Double? = null,
)

data class Geometry(
    @SerializedName("lat")
    val lat: Double?,
    @SerializedName("lng")
    val lng: Double?
)

enum class Day{
    Sun, Mon, Tue, Wed, Thu, Fri, Sat,
}

data class Hours(
    @SerializedName("formatted_address")
    val formattedAddress: String?,
    @SerializedName("location")
    val location: Location?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("week")
    val week: List<Week>?
)

data class Location(
    @SerializedName("lat")
    val lat: Double?,
    @SerializedName("lng")
    val lng: Double?
)

data class Week(
    @SerializedName("day")
    val day: String?,
    @SerializedName("hours")
    val hours: List<Hour>?
)

data class Hour(
    @SerializedName("hour")
    val hour: Int?,
    @SerializedName("percentage")
    val percentage: Int?
)

