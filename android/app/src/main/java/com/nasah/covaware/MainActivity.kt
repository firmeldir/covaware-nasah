package com.nasah.covaware

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.nasah.covaware.map.MapActivity
import com.nasah.covaware.reg.REG_SHARED_PREF_NAME
import com.nasah.covaware.reg.RegisterActivity
import com.nasah.covaware.reg.SHARED_PREF_AGE

@ExperimentalAnimationApi
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

		getSharedPreferences(REG_SHARED_PREF_NAME, Activity.MODE_PRIVATE)
			.getInt(SHARED_PREF_AGE, -1).let {
				if(it == -1){
					startActivity(Intent(this, RegisterActivity::class.java))
				}
			}

		findViewById<Button>(R.id.enterButton).setOnClickListener {
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
