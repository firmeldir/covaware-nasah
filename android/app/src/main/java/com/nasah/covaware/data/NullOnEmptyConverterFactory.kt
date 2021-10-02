package com.nasah.covaware.data

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class NullOnEmptyConverterFactory : Converter.Factory() {
	override fun responseBodyConverter(
		type: Type?,
		annotations: Array<Annotation>?,
		retrofit: Retrofit?
	): Converter<ResponseBody, *>? {
		val delegate = retrofit!!.nextResponseBodyConverter<Any>(this, type!!, annotations!!)
		return Converter<ResponseBody, Any> {
			when (it.contentLength()) {
				0L -> null
				else -> try {
					delegate.convert(it)
				} catch (e: Exception) {
					null
				}
			}
		}
	}
}
