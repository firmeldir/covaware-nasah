package com.nasah.covaware.map

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
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
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.nasah.covaware.*
import com.nasah.covaware.data.MapRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.cos

private const val HEATMAP_SOURCE_ID = "heatmap-source-id"
private const val HEATMAP_LAYER_ID = "heatmap-layer-id"
private const val COLOR_PROPERTY = "color-property"

abstract class OriginMapActivity : AppCompatActivity() {

	private var mapView: MapView? = null
	private var map: MapboxMap? = null
	private var mapStyle: Style? = null
	private var locationComponent: LocationComponent? = null

	private var heatmapSource: GeoJsonSource? = null
	private var heatmapElements: List<HeatMapSquare> = emptyList()

	private val gpsStatusListener: GpsStatusListener by lazy { GpsStatusListener(applicationContext) }

	abstract fun onMapFullyReady()
	abstract fun onCameraIdle(currentCameraLocation: LatLng)

	protected fun pushPolygons(elements: List<HeatMapSquare>){
		heatmapElements = (elements + heatmapElements).distinctBy { it.location }

		val features = mutableListOf<Feature>()
		for(e in heatmapElements){
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
		heatmapSource?.setGeoJson(featureCollection)
	}

	private fun getCurrentCameraLocation(): LatLng{
		return LatLng(
			map!!.cameraPosition.target.latitude,
			map!!.cameraPosition.target.longitude
		)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		Mapbox.getInstance(this, MAPBOX_ACCESS_TOKEN)
		setContentView(R.layout.activity_map)
		setupGpsListening()

		findViewById<MapView>(R.id.mapView).let{
			mapView = it
			it.onCreate(savedInstanceState)

			setupMap(it)
		}
	}

	private fun setupGpsListening(){
		lifecycleScope.launchWhenCreated {
			gpsStatusListener.asFlow()
				.distinctUntilChanged()
				.onEach {
					when(it){
						GpsStatus.DISABLED -> this@OriginMapActivity.finish()
						GpsStatus.ENABLED -> {}
					}
				}
				.launchIn(this)
		}
	}

	private fun setupMap(a: MapView){
		a.getMapAsync {
			map = it
			it.uiSettings.isCompassEnabled = false
			it.uiSettings.isLogoEnabled = false
			it.uiSettings.isAttributionEnabled = false
			it.uiSettings.isZoomGesturesEnabled = false

			setupStyle(it)
		}
	}

	@SuppressLint("MissingPermission")
	private fun setupStyle(a: MapboxMap){
		a.setStyle(Style.MAPBOX_STREETS){ style ->
			mapStyle = style

			val customLocationComponentOptions = LocationComponentOptions.builder(applicationContext)
				.trackingGesturesManagement(true)
				.build()

			val locationComponentActivationOptions = LocationComponentActivationOptions.builder(applicationContext, mapStyle!!)
				.locationComponentOptions(customLocationComponentOptions)
				.build()

			a.locationComponent.apply {
				locationComponent = this

				activateLocationComponent(locationComponentActivationOptions)
				cameraMode = CameraMode.TRACKING
				renderMode = RenderMode.NORMAL
				isLocationComponentEnabled = true
			}

			setupHeatmap(style)
			setupCamera(a)
			onMapFullyReady()
		}
	}

	private fun setupHeatmap(a: Style){
		heatmapSource = createHeatmapSource(a)
		setupHeatmapLayer(a)
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

	private fun setupCamera(a: MapboxMap){
		a.addOnCameraIdleListener{ onCameraIdle(getCurrentCameraLocation()) }
		a.animateCamera(
			CameraUpdateFactory.newCameraPosition(
				CameraPosition.Builder()
					.apply {
						a.locationComponent.lastKnownLocation?.toLatLng()?.let {
							//target(it)
							target(
								LatLng(40.732345, -73.987333)
							)
						}
						zoom(13.0)
					}
					.build()
			),
			500
		)
	}

	override fun onStart() {
		super.onStart()
		mapView?.onStart()
	}

	override fun onResume() {
		super.onResume()
		mapView?.onResume()
	}

	override fun onPause() {
		super.onPause()
		mapView?.onPause()
	}

	override fun onStop() {
		super.onStop()
		mapView?.onStop()
	}

	override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
		super.onSaveInstanceState(outState, outPersistentState)
		mapView?.onSaveInstanceState(outState)
	}

	override fun onDestroy() {
		super.onDestroy()
		mapView?.onDestroy()

		mapView = null
		map = null
		mapStyle = null
		locationComponent = null

		heatmapSource = null
		heatmapElements = emptyList()
	}

	override fun onLowMemory() {
		super.onLowMemory()
		mapView?.onLowMemory()
	}
}
