package com.example.launcher

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

/**
 * Extension function to get [Bitmap] from [Drawable]
 * @return [Bitmap]
 */
internal fun Drawable.getBitmap(): Bitmap {

    if(this is BitmapDrawable) return bitmap

    val bmp = Bitmap.createBitmap(
        intrinsicWidth,
        intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bmp)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bmp
}