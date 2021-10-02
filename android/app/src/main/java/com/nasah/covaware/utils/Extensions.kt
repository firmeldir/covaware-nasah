package com.nasah.covaware.utils

import android.location.Location
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Location.toLatLng() = LatLng(latitude, longitude)

suspend inline fun MapView.awaitMap(): MapboxMap =
	suspendCoroutine { continuation ->
		getMapAsync {
			continuation.resume(it)
		}
	}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
	val context = LocalContext.current
	val mapView = remember { MapView(context) }

	val lifecycle = LocalLifecycleOwner.current.lifecycle
	DisposableEffect(lifecycle, mapView) {
		// Make MapView follow the current lifecycle
		val lifecycleObserver = getMapLifecycleObserver(mapView)
		lifecycle.addObserver(lifecycleObserver)
		onDispose {
			lifecycle.removeObserver(lifecycleObserver)
		}
	}

	return mapView
}

private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
	LifecycleEventObserver { _, event ->
		when (event) {
			Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
			Lifecycle.Event.ON_START -> mapView.onStart()
			Lifecycle.Event.ON_RESUME -> mapView.onResume()
			Lifecycle.Event.ON_PAUSE -> mapView.onPause()
			Lifecycle.Event.ON_STOP -> mapView.onStop()
			Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
			else -> throw IllegalStateException()
		}
	}
