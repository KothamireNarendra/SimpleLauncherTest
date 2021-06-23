@file:Suppress("FunctionName")

package com.example.simplelauncher

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.App
import com.example.simplelauncher.home.HomeViewModel
import com.google.accompanist.coil.rememberCoilPainter

@ExperimentalFoundationApi
@Composable
fun AllAppsListScreen(homeViewModel: HomeViewModel) {
    val items: List<App> by homeViewModel.allInstalledApps.observeAsState(listOf<App>())
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            items.forEach {
                AllAppsListRow(
                    it,
                    { homeViewModel.performLaunch(it) },
                    { homeViewModel.performUninstall(it) }
                )
            }
        }
    }
}


@ExperimentalFoundationApi
@Composable
fun AllAppsListRow(
    app: App,
    onRowClick: () -> Unit,
    onLongRowClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = onRowClick,
                onLongClick = onLongRowClick
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(app.appName, fontSize = 18.sp)
        Image(
            painter = rememberCoilPainter(
                app.icon
            ),
            modifier = Modifier.size(28.dp),
            contentDescription = app.appName
        )
    }
}