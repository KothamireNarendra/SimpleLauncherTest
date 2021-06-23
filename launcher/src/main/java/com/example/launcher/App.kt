package com.example.launcher

import android.graphics.Bitmap

data class App(
    val appName: String,
    val packageName: String,
    val icon: Bitmap,
    val mainActivity: String,
    val versionCode: Long,
    val versionName: String
)
