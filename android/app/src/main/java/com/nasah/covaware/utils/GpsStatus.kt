package com.nasah.covaware.utils

enum class GpsStatus {
	ENABLED, DISABLED;

	companion object{
		fun get(status: Boolean): GpsStatus = if(status) ENABLED else DISABLED
	}
}
