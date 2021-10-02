package com.nasah.covaware.map

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.nasah.covaware.R

@Immutable
data class Places(
	val value: List<Place>
)

@Immutable
data class Place(
	val id: String,
	val name: String,
	val image: ImageVector?,
	@DrawableRes val imageRes: Int
)

val places = Places(
	listOf(
		Place("library", "Library", Icons.Rounded.LocalLibrary, R.drawable.round_local_library_20),
		Place("amusement_park", "Amusement park", Icons.Rounded.Attractions, R.drawable.round_attractions_20),
		Place("art_gallery", "Art gallery", Icons.Rounded.ColorLens, R.drawable.round_color_lens_20),
		Place("bar", "Bar", Icons.Rounded.LocalBar, R.drawable.round_local_bar_20),
		Place("beauty_salon", "Beauty salon", Icons.Rounded.Brush, R.drawable.round_brush_20),
		Place("movie_theater", "Movie theater", Icons.Rounded.Theaters, R.drawable.round_theaters_20),
		Place("museum", "Museum", Icons.Rounded.Museum, R.drawable.round_museum_20),
		Place("night_club", "Night club", Icons.Rounded.Nightlife, R.drawable.round_nightlife_20),
		Place("church", "Church", null, R.drawable.round_church_20),
		Place("drugstore", "Drugstore", Icons.Rounded.LocalPharmacy, R.drawable.round_local_pharmacy_20),
		Place("shopping_mall", "Shopping mall", Icons.Rounded.LocalMall, R.drawable.round_local_mall_20),
		Place("spa", "Spa", Icons.Rounded.Spa, R.drawable.round_spa_20),
		Place("subway_station", "Subway station", Icons.Rounded.Subway, R.drawable.round_subway_20),
		Place("supermarket", "Supermarket", Icons.Rounded.LocalGroceryStore, R.drawable.round_local_grocery_store_20),
		Place("synagogue", "Synagogue", null, R.drawable.round_synagogue_20),
		Place("hair_care", "Hair care", Icons.Rounded.FaceRetouchingNatural, R.drawable.round_face_retouching_natural_20),
		Place("laundry", "Laundry", Icons.Rounded.LocalLaundryService, R.drawable.round_local_laundry_service_20),
		Place("gym", "Fitness center", Icons.Rounded.FitnessCenter, R.drawable.round_fitness_center_20),
		Place("restaurant", "Restaurant", Icons.Rounded.Restaurant, R.drawable.round_restaurant_20),
		Place("cafe", "Cafe", Icons.Rounded.LocalCafe, R.drawable.round_local_cafe_20)
	)
)
