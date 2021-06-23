package com.example.simplelauncher.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.core.view.WindowCompat
import com.example.simplelauncher.AllAppsListScreen
import com.example.simplelauncher.LauncherTheme
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalFoundationApi
@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private val mainViewModel by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LauncherTheme{
                AllAppsListScreen(mainViewModel)
            }
        }
        mainViewModel.getAllInstalledApps()
    }


}