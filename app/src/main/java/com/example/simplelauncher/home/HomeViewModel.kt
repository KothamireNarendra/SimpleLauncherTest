package com.example.simplelauncher.home

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.launcher.App
import com.example.launcher.LauncherManager
import com.example.simplelauncher.utils.ICoroutineDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val launcherManager: LauncherManager,
    private val coroutineDispatchers: ICoroutineDispatchers
) : ViewModel() {

    private var _allInstalledApps = MutableLiveData(listOf<App>())
    val allInstalledApps: LiveData<List<App>> = _allInstalledApps

    private val appInstallUninstallListener =
        object : LauncherManager.AppInstallUninstallListener {
            override fun onAppInstall(app: App) {
                getAllInstalledApps()
            }

            override fun onAppUninstall(app: App) {
                getAllInstalledApps()
            }
        }

    init {
        launcherManager.registerAppInstallUninstallListener(appInstallUninstallListener)
    }

    fun getAllInstalledApps() {
        viewModelScope.launch {
            _allInstalledApps.value = withContext(coroutineDispatchers.computation) {
                launcherManager.getAllInstalledApps()
            }
        }
    }

    fun performLaunch(app: App) {
        launcherManager.launchApp(app)
    }

    fun performUninstall(app: App){
        launcherManager.uninstallApp(app)
    }

    @VisibleForTesting
    public override fun onCleared() {
        super.onCleared()
        launcherManager.unregisterAppInstallUninstallListener(appInstallUninstallListener)
    }
}