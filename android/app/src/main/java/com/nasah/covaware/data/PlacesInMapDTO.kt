package com.nasah.covaware.data

import android.util.Log
import com.google.gson.annotations.SerializedName
import com.mapbox.mapboxsdk.geometry.LatLng
import com.nasah.covaware.map.HeatMapSquare
import com.nasah.covaware.map.PLaceToVisit
import com.nasah.covaware.map.PLacesToVisit
import com.nasah.covaware.map.Risk
import kotlin.random.Random

data class PlacesInMapDTO(
    @SerializedName("city")
    val city: String?,
    @SerializedName("county")
    val county: String?,
    @SerializedName("hours")
    val hours: Hours?,
    @SerializedName("places")
    val places: List<Place>?
){
    fun toPLacesToVisit(type: com.nasah.covaware.map.Place): PLacesToVisit {
        Log.e("vlad", this.hours?.week?.get(0).toString())
        return PLacesToVisit(
            places?.map {
                PLaceToVisit(
                    location = LatLng(it.geometry!!.lat!!, it.geometry.lng!!),
                    name = it.name!!,
                    id = it.placeId!!,
                    risk = Risk.values()[Random.nextInt(0, 5)],
                    type = type
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
    val placeId: String?
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

