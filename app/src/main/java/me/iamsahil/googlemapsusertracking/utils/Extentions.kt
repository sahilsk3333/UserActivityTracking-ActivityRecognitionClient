package me.iamsahil.googlemapsusertracking.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}


fun Context.bitmapDescriptor(
    vectorResId: Int,
    width: Int? = null,
    height: Int? = null,
): BitmapDescriptor? {


    // retrieve the actual drawable
    val drawable = ContextCompat.getDrawable(this, vectorResId) ?: return null


    val dpToPxScale = Resources.getSystem().displayMetrics.density

    // Convert dp values to pixels using the scale factor
    val rightPx = if(width != null)(width * dpToPxScale).toInt() else drawable.intrinsicWidth
    val bottomPx = if(height != null)(height * dpToPxScale).toInt() else drawable.intrinsicHeight

    drawable.setBounds(0, 0, rightPx, bottomPx)
    val bm = Bitmap.createBitmap(
        rightPx,
        bottomPx,
        Bitmap.Config.ARGB_8888
    )

    // draw it onto the bitmap
    val canvas = android.graphics.Canvas(bm)

    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bm)
}