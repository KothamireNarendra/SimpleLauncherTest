package com.example.simplelauncher

import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.launcher.App
import com.example.launcher.LauncherManager
import com.example.simplelauncher.home.HomeViewModel
import com.example.simplelauncher.utils.AppCoroutineDispatchers
import com.example.simplelauncher.utils.ICoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class HomeViewModelTest {

    private lateinit var coroutineDispatchers: ICoroutineDispatchers
    private lateinit var homeViewModel: HomeViewModel

    @Mock
    lateinit var launcherManager: LauncherManager

    @Mock
    lateinit var appIcon: Bitmap

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(TestCoroutineDispatcher())
        coroutineDispatchers = AppCoroutineDispatchers(computation = Dispatchers.Main)
        homeViewModel = HomeViewModel(launcherManager, coroutineDispatchers)
    }

    @Test
    fun getAllInstalledApps() = runBlockingTest {
        val expectedApps = getFakeApps()
        whenever(launcherManager.getAllInstalledApps()).thenReturn(expectedApps)
        homeViewModel.getAllInstalledApps()
        val actualApps = homeViewModel.allInstalledApps.value!!
        assertTrue(expectedApps.size == actualApps.size
                    && expectedApps.containsAll(actualApps)
                    && actualApps.containsAll(expectedApps))
    }

    @Test
    fun onCleared() {
        homeViewModel.onCleared()
        verify(launcherManager).unregisterAppInstallUninstallListener(any<LauncherManager.AppInstallUninstallListener>())
    }

    @Test
    fun performLaunch() {
        val appToLaunch = getFakeApps().first()
        homeViewModel.performLaunch(appToLaunch)
        verify(launcherManager).launchApp(eq(appToLaunch))
    }

    @Test
    fun performUninstall() {
        val appToUninstall = getFakeApps().first()
        homeViewModel.performUninstall(appToUninstall)
        verify(launcherManager).uninstallApp(eq(appToUninstall))
    }

    private fun getFakeApps(): List<App>{
        return listOf(
            App(
                appName = "Facebook",
                packageName = "com.facebook.app",
                icon = appIcon,
                mainActivity = "mainActivity",
                versionCode = 1,
                versionName = "1.0.1"
            ),
            App(
                appName = "Outlook",
                packageName = "com.microsoft.outlook",
                icon = appIcon,
                mainActivity = "mainActivity",
                versionCode = 2,
                versionName = "1.0.1"
            )
        )
    }
}