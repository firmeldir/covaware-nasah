package com.nasah.covaware.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.nasah.covaware.R
import com.nasah.covaware.map.Place
import com.nasah.covaware.map.Risk
import de.hdodenhof.circleimageview.CircleImageView


fun View.setLayoutParamsInDp(width: Int, height: Int){
	val factor = this.context.resources.displayMetrics.density
	val params = FrameLayout.LayoutParams((width * factor).toInt(), (height * factor).toInt())
	this.layoutParams = params
}

fun View.generateSymbol(): Bitmap {
	val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
	this.measure(measureSpec, measureSpec)

	val measuredWidth = this.measuredWidth
	val measuredHeight = this.measuredHeight

	this.layout(0, 0, measuredWidth, measuredHeight)

	val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
	bitmap.eraseColor(Color.TRANSPARENT)

	val canvas = Canvas(bitmap)
	this.draw(canvas)

	return bitmap
}

fun generateMarkerView(context: Context, type: Place, risk: Risk): Bitmap{

	val view = LayoutInflater.from(context).inflate(R.layout.marker_place, null)
		.apply { setLayoutParamsInDp(48, 48) }

	val imageHolder = view.findViewById<CircleImageView>(R.id.image)
	imageHolder.setImageDrawable(
		LayerDrawable(
			arrayOf(
				ColorDrawable(Color.WHITE),
				AppCompatResources.getDrawable(context, type.imageRes)
			)
		)
	)
	imageHolder.borderColor = Color.parseColor(risk.color)

	return view.generateSymbol()
}
