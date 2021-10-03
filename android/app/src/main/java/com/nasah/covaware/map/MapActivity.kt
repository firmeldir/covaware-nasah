package com.nasah.covaware.map

import android.annotation.SuppressLint
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material.icons.rounded.Place
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.nasah.covaware.*
import com.nasah.covaware.utils.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos

private const val HEATMAP_SOURCE_ID = "heatmap-source-id"
private const val HEATMAP_LAYER_ID = "heatmap-layer-id"
private const val COLOR_PROPERTY = "color-property"

private const val PLACES_SOURCE_ID = "places-source-id"
private const val PLACES_LAYER_ID = "places-layer-id"
private const val ICON_PROPERTY = "icon-id"
private const val PLACE_KEY = "place-ket"

@ExperimentalAnimationApi
class MapActivity : AppCompatActivity() {

	private val gpsStatusListener: GpsStatusListener by lazy { GpsStatusListener(applicationContext) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setupGpsListening()
		Mapbox.getInstance(this, MAPBOX_ACCESS_TOKEN)
		setContent {
			CovawareTheme {
				Surface {
					MapScreen()
				}
			}
		}
	}

	private fun setupGpsListening(){
		lifecycleScope.launchWhenCreated {
			gpsStatusListener.asFlow()
				.distinctUntilChanged()
				.onEach {
					when(it){
						GpsStatus.DISABLED -> this@MapActivity.finish()
						GpsStatus.ENABLED -> {}
					}
				}
				.launchIn(this)
		}
	}

	// compose staff

	@Composable
	private fun MapScreen(
		viewModel: MapViewModel = viewModel()
	){
		val isCloseButton by viewModel.isCloseButton.collectAsState(initial = false)
		val isPlacesList by viewModel.isPlacesList.collectAsState(initial = false)
		val chosenPlace by viewModel.chosenPlace.collectAsState(initial = null)
		val currentRiskLevel by viewModel.currentRiskLevel.collectAsState(initial = null)
		var map by remember { mutableStateOf<MapboxMap?>(null) }

		LaunchedEffect(true){
			while (this.isActive){
				map?.locationComponent?.lastKnownLocation?.toLatLng()?.let {
					viewModel.currentLocation = it
					delay(1000 * 60 * 10L)
				}
				delay(1000L)
			}
		}

		Scaffold(
			floatingActionButton = {
				FloatingActionButton(
					backgroundColor = MaterialTheme.colors.surface,
					onClick = { viewModel.menuChange() },
				) {
					Crossfade(targetState = !isCloseButton) {
						if(it){
							Icon(Icons.Rounded.LocationSearching, contentDescription = null)
						}else{
							Icon(Icons.Rounded.Close, contentDescription = null)
						}
					}

				}
			},
			isFloatingActionButtonDocked = true,
			bottomBar = {
				BottomAppBar(
					// Defaults to null, that is, No cutout
					cutoutShape = MaterialTheme.shapes.small.copy(
						CornerSize(percent = 50)
					)
				) {
					currentRiskLevel?.let {
						Row {
							Spacer(modifier = Modifier.width(16.dp))
							Text(
								color = MaterialTheme.colors.surface,
								text = "Your risk level: "
							)
							Text(
								color = Color(android.graphics.Color.parseColor(it.color)),
								text = it.riskName
							)
						}
					}
				}
			}
		) {
			Box (Modifier.fillMaxSize()) {
				MapContent({ map = it },viewModel)
				AnimatedVisibility(visible = isPlacesList) {
					LazyColumn{
						items(items = places.value) { place ->
							PlaceItem(place) {
								viewModel.findPlaceInMap(it, map?.locationComponent?.lastKnownLocation?.toLatLng())
							}
						}
					}
				}
				AnimatedVisibility(visible = chosenPlace != null) {
					Column {
						Surface(
							color = MaterialTheme.colors.surface.copy(alpha = 0.8f),
							shape = MaterialTheme.shapes.medium,
							modifier = Modifier
								.fillMaxWidth()
								.padding(12.dp)
								.shadow(4.dp)
						) {
							Text(
								style = MaterialTheme.typography.subtitle2,
								text = "Times to visit ${chosenPlace?.name ?: ""}:",
								modifier = Modifier.padding(vertical = 20.dp, horizontal = 4.dp)
							)
						}
						LazyColumn{
							items(items = chosenPlace?.recommendedHours ?: emptyList()) { hour ->
								HourItem(hour)
							}
						}
					}
				}
			}
		}
	}

	@Composable
	private fun HourItem(hour: RecommendedHour){
		Surface(
			color = MaterialTheme.colors.surface.copy(alpha = 0.6f),
			shape = MaterialTheme.shapes.medium,
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
				.shadow(4.dp)
		) {
			Row(
				modifier = Modifier.padding(vertical = 20.dp, horizontal = 4.dp)
			) {
				Text(
					text = hour.timeText + ",",
					maxLines = 1,
					color = MaterialTheme.colors.primary,
					style = MaterialTheme.typography.body1,
					modifier = Modifier.align(Alignment.CenterVertically)
				)
				Spacer(modifier = Modifier.width(16.dp))
				Text(
					text = "risk level: ${hour.risk.riskName}",
					maxLines = 1,
					color = MaterialTheme.colors.secondary,
					style = MaterialTheme.typography.body1,
					modifier = Modifier.align(Alignment.CenterVertically)
				)
			}
		}
	}

	@Composable
	private fun PlaceItem(place: Place, onPlaceClick: (Place) -> Unit){
		Surface(
			color = MaterialTheme.colors.surface.copy(alpha = 0.6f),
			shape = MaterialTheme.shapes.medium,
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
				.shadow(4.dp)
				.clickable { onPlaceClick(place) }
		) {
			Row(
				modifier = Modifier.padding(vertical = 20.dp, horizontal = 4.dp)
			) {
				Image(
					painter = painterResource(place.imageRes),
					contentDescription = null,
					modifier = Modifier.align(Alignment.CenterVertically)
				)
				Text(
					text = place.name,
					maxLines = 1,
					color = MaterialTheme.colors.primary,
					style = MaterialTheme.typography.body2,
					modifier = Modifier.align(Alignment.CenterVertically)
				)
			}
		}
	}

	@Composable
	private fun MapContent(
		onMapReady: (MapboxMap) -> Unit,
		viewModel: MapViewModel = viewModel()
	){
		MapView(viewModel, onMapReady)
	}

	@Composable
	private fun MapView(
		viewModel: MapViewModel = viewModel(),
		onMapReady: (MapboxMap) -> Unit
	){
		val mapView = rememberMapViewWithLifecycle()
		MapViewContainer(mapView, viewModel, onMapReady)
	}

	@Composable
	private fun MapViewContainer(
		mapView: MapView,
		viewModel: MapViewModel = viewModel(),
		onMapReady: (MapboxMap) -> Unit
	){
		val heatmapSquaresState by viewModel.heatmapSquares.collectAsState(initial = HeatMapSquares(emptyList()))
		val placesState by viewModel.placesToVisit.collectAsState(initial = PLacesToVisit(emptyList()))

		var firstTimeShowingMap by remember { mutableStateOf(true)}

		var style by remember { mutableStateOf<Style?>(null) }
		var heatmapSource by remember { mutableStateOf<GeoJsonSource?>(null) }
		var placesSource by remember { mutableStateOf<GeoJsonSource?>(null) }

		val coroutineScope = rememberCoroutineScope()
		AndroidView({ mapView }) { view ->
			if(firstTimeShowingMap){
				coroutineScope.launch {
					view.awaitMap().let { m ->
						Log.e(LOG_TAG, "This should not be called more than two times")

						setupMapboxMap(
							m,
							{ viewModel.newCameraLocation(it) },
							{ viewModel.onPlaceClick(it) }
						){ _, s, h, p ->
							onMapReady(m)
							style = s
							heatmapSource = h
							placesSource = p
						}
					}
				}
				firstTimeShowingMap = false
			}
			Log.i(LOG_TAG, "pushPolygons(${heatmapSquaresState.value})")
			heatmapSource?.pushPolygons(heatmapSquaresState.value)
			Log.i(LOG_TAG, "pushPlaces()")
			placesState.value.onEach {
				Log.i(LOG_TAG, "        $it")
			}
			placesSource?.pushPlaces(placesState.value)
		}
	}

	//

	private fun GeoJsonSource.pushPolygons(elements: List<HeatMapSquare>){
		val features = mutableListOf<Feature>()
		for(e in elements){
			val square = squareFromCoordinates(e.location)
			val points: MutableList<Point> = mutableListOf<Point>().apply{
				add(Point.fromLngLat(square[3]!!.longitude, square[3]!!.latitude))
				add(Point.fromLngLat(square[2]!!.longitude, square[2]!!.latitude))
				add(Point.fromLngLat(square[0]!!.longitude, square[0]!!.latitude))
				add(Point.fromLngLat(square[1]!!.longitude, square[1]!!.latitude))
			}
			val listOfList: MutableList<List<Point>> = mutableListOf<List<Point>>().apply{ add(points) }
			val feature = Feature.fromGeometry(Polygon.fromLngLats(listOfList)).apply {
				addStringProperty(COLOR_PROPERTY, e.color)
			}
			features.add(feature)
		}
		val featureCollection = FeatureCollection.fromFeatures(features)
		setGeoJson(featureCollection)
	}

	private fun GeoJsonSource.pushPlaces(elements: List<PLaceToVisit>){
		val features = mutableListOf<Feature>()
		for(e in elements){
			val feature = Feature.fromGeometry(Point.fromLngLat(e.location.longitude, e.location.latitude)).apply {
				addStringProperty(ICON_PROPERTY, riskTypeId(e.risk, e.type))
				addStringProperty(PLACE_KEY, e.id)
			}
			features.add(feature)
		}
		val featureCollection = FeatureCollection.fromFeatures(features)
		setGeoJson(featureCollection)
	}

	@SuppressLint("MissingPermission")
	private fun setupMapboxMap(
		map: MapboxMap,
		onCameraIdle: (LatLng) -> Unit,
		onMapClicked: (String) -> Unit, //id
		onMapReady: (MapboxMap, Style, GeoJsonSource, GeoJsonSource) -> Unit
	){
		map.uiSettings.isCompassEnabled = false
		map.uiSettings.isLogoEnabled = false
		map.uiSettings.isAttributionEnabled = false
		map.uiSettings.isZoomGesturesEnabled = false

		map.setStyle(Style.MAPBOX_STREETS){ style ->
			val customLocationComponentOptions = LocationComponentOptions.builder(applicationContext)
				.trackingGesturesManagement(true)
				.build()

			val locationComponentActivationOptions = LocationComponentActivationOptions.builder(applicationContext, style)
				.locationComponentOptions(customLocationComponentOptions)
				.build()

			map.locationComponent.apply {
				activateLocationComponent(locationComponentActivationOptions)
				cameraMode = CameraMode.TRACKING
				renderMode = RenderMode.NORMAL
				isLocationComponentEnabled = true
			}

			style.addPlacesImagesToStyle()

			val heatmapSource = setupHeatmap(style)
			val placesSource = setupPlaces(style)
			setupCamera(map, { pointF ->
				map.queryRenderedFeatures(pointF, PLACES_LAYER_ID).let {
					onMapClicked(it[0].properties()?.get(PLACE_KEY)?.asString ?: "")
				}
			},
				onCameraIdle
			)
			onMapReady(map, style, heatmapSource, placesSource)
		}
	}

	private fun Style.addPlacesImagesToStyle(){
		for(p in places.value){
			for(r in Risk.values()){
				addImage(
					riskTypeId(r, p),
					generateMarkerView(application.applicationContext, p, r)
				)
			}
		}
	}

	private fun setupHeatmap(style: Style): GeoJsonSource{
		val heatmapSource = createHeatmapSource(style)
		setupHeatmapLayer(style)
		return heatmapSource
	}

	private fun createHeatmapSource(style: Style): GeoJsonSource {
		val fillFeatureCollection = FeatureCollection.fromFeatures(arrayOf())
		val fillGeoJsonSource = GeoJsonSource(HEATMAP_SOURCE_ID, fillFeatureCollection)
		style.addSource(fillGeoJsonSource)
		return fillGeoJsonSource
	}

	private fun setupHeatmapLayer(style: Style){
		val fillLayer = FillLayer(HEATMAP_LAYER_ID, HEATMAP_SOURCE_ID)
		fillLayer.setProperties(
			PropertyFactory.fillColor(
				Expression.get(COLOR_PROPERTY)
			),
			PropertyFactory.fillOpacity(.6f),
		)
		style.addLayer(fillLayer)
	}

	private fun setupPlaces(style: Style): GeoJsonSource{
		val heatmapSource = createPlacesSource(style)
		setupPlacesLayer(style)
		return heatmapSource
	}

	private fun createPlacesSource(style: Style): GeoJsonSource {
		val fillFeatureCollection = FeatureCollection.fromFeatures(arrayOf())
		val fillGeoJsonSource = GeoJsonSource(PLACES_SOURCE_ID, fillFeatureCollection)
		style.addSource(fillGeoJsonSource)
		return fillGeoJsonSource
	}

	private fun setupPlacesLayer(style: Style){
		val fillLayer = SymbolLayer(PLACES_LAYER_ID, PLACES_SOURCE_ID)
		fillLayer.setProperties(
			PropertyFactory.iconImage(
				Expression.get(ICON_PROPERTY)
			),
			PropertyFactory.iconAllowOverlap(true),
			PropertyFactory.iconIgnorePlacement(true)
		)
		style.addLayerAbove(fillLayer, HEATMAP_LAYER_ID)
	}

	private fun squareFromCoordinates(location: LatLng): Array<LatLng?>{
		val pDistanceInMeters = 500
		val array = Array<LatLng?>(4){ null }
		val latRadian: Double = Math.toRadians(location.latitude)
		val degLatKm = 110.574235
		val degLongKm = 110.572833 * cos(latRadian)
		val deltaLat = pDistanceInMeters / 1000.0 / degLatKm
		val deltaLong = pDistanceInMeters / 1000.0 / degLongKm
		val minLat = location.latitude - deltaLat
		val minLong = location.longitude - deltaLong
		val maxLat = location.latitude + deltaLat
		val maxLong = location.longitude + deltaLong

		array[0] = LatLng(minLat, minLong)
		array[1] = LatLng(minLat, maxLong)
		array[2] = LatLng(maxLat, minLong)
		array[3] = LatLng(maxLat, maxLong)
		return array
	}

	private fun setupCamera(a: MapboxMap, onMapClicked: (PointF) -> Unit, onCameraIdle: (LatLng) -> Unit){
		fun project(latLng: LatLng) : PointF = a.projection.toScreenLocation(latLng)

		a.addOnCameraIdleListener { onCameraIdle(getCurrentCameraLocation(a)) }
		a.addOnMapClickListener { latLng ->
			onMapClicked(project(latLng))
			false
		}

		a.animateCamera(
			CameraUpdateFactory.newCameraPosition(
				CameraPosition.Builder()
					.apply {
						a.locationComponent.lastKnownLocation?.toLatLng()?.let {
							target(it)
							//target(LatLng(40.732345, -73.987333))
							//target(LatLng(40.732345, -73.247333))
						}
						zoom(13.0)
					}
					.build()
			),
			500
		)
	}

	private fun getCurrentCameraLocation(map: MapboxMap): LatLng{
		return LatLng(
			map.cameraPosition.target.latitude,
			map.cameraPosition.target.longitude
		)
	}
}
