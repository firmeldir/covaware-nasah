package com.nasah.covaware.map

enum class GpsStatus {
	ENABLED, DISABLED;

	companion object{
		fun get(status: Boolean): GpsStatus = if(status) ENABLED else DISABLED
	}
}
