package com.nasah.covaware

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.nasah.covaware.map.MapActivity

class MainActivity : AppCompatActivity() {

	private val requestPermissionLauncher =
		registerForActivityResult(
			ActivityResultContracts.RequestPermission()
		) { isGranted: Boolean ->
			if (isGranted) {
				startMap()
			}
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		findViewById<MaterialButton>(R.id.enterButton).setOnClickListener {
			if(ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				startMap()
			}
			else {
				requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
			}
		}
	}

	private fun startMap(){
		if(applicationContext.getSystemService(LocationManager::class.java).isProviderEnabled(LocationManager.GPS_PROVIDER)){
			startActivity(Intent(this, MapActivity::class.java))
		}
	}
}
