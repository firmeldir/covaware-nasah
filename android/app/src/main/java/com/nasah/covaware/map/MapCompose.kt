package com.nasah.covaware.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView

//@Preview
//@Composable
//fun MapPreview() {
//	Map()
//}
//
//@Composable
//fun Map(
//	modifier: Modifier = Modifier
//){
//	AndroidView(
//		modifier = modifier,
//		factory = { context ->
//			Mapbox.getInstance(
//				context,
//				public_api_key
//			)
//			MapView(context).apply {
//				getMapAsync { mapboxMap ->
//					mapboxMap.setStyle(Style.MAPBOX_STREETS)
//
//					val position = CameraPosition.Builder()
//						.zoom(19.0)
//						.build()
//
//					mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1)
//				}
//			}
//		}
//	)
//}
