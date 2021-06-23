package com.example.launcher

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*


@RunWith(MockitoJUnitRunner::class)
class LauncherManagerTest {

    @Mock
    lateinit var context: Application

    @Mock
    lateinit var packageManager: PackageManager

    @Mock
    lateinit var launcherManager: LauncherManager

    @Mock
    lateinit var resolveInfo: ResolveInfo

    @Mock
    lateinit var packageInfo: PackageInfo

    @Mock
    lateinit var activityInfo: ActivityInfo

    @Mock
    lateinit var drawable: BitmapDrawable

    @Mock
    lateinit var bitmap: Bitmap

    @Before
    fun setup() {
        launcherManager = LauncherManager.getInstance(context)
        whenever(context.packageManager).doReturn(packageManager)
        whenever(drawable.bitmap).thenReturn(bitmap)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `getAllApps should give sorted list of apps`() {
        val appsList = getAppsList()
        getAllAppsSetup(appsList)

        val apps = launcherManager.getAllInstalledApps()
        val sortedAppsList = appsList.sortedBy { it.appName }
        assertArrayEquals(sortedAppsList.toTypedArray(), apps.toTypedArray())
    }

    @Test
    fun `given getAllApps called previously should return saved apps list on next call`() {
        val appsList = getAppsList()
        getAllAppsSetup(appsList)

        launcherManager.getAllInstalledApps()
        launcherManager.getAllInstalledApps()

        verify(packageManager, times(1)).queryIntentActivities(any(), any())
    }

    private fun getAllAppsSetup(appsList: List<App>) {
        val resolveInfoList = getResolveInfoList()
        whenever(packageManager.queryIntentActivities(any(), any()))
            .doReturn(resolveInfoList)

        val packageInfoList = getPackageInfoList()
        appsList.forEachIndexed { i, app ->
            whenever(packageManager.getPackageInfo(eq(app.packageName), eq(0))).thenReturn(
                packageInfoList[i]
            )

            whenever(resolveInfoList[i].loadLabel(eq(packageManager))).thenReturn(app.appName)
            whenever(resolveInfoList[i].loadIcon(eq(packageManager))).thenReturn(drawable)
        }
    }

    private fun getResolveInfoList(): List<ResolveInfo> {
        return getAppsList().map {
            val resolveInfo = mock<ResolveInfo>()

            val activityInfo = mock<ActivityInfo>()
            activityInfo.packageName = it.packageName
            activityInfo.name = it.mainActivity

            resolveInfo.activityInfo = activityInfo
            resolveInfo
        }
    }

    private fun getPackageInfoList(): List<PackageInfo> {
        return getAppsList().map { app ->
            val packageInfo = mock<PackageInfo>()
            packageInfo.longVersionCode = app.versionCode
            packageInfo.versionCode = app.versionCode.toInt()
            packageInfo.versionName = app.versionName
            packageInfo
        }
    }

    @Test
    fun `registerAppInstallUninstallListener should register for broadcast receiver`() {
        val appInstallUninstallListener = mock<LauncherManager.AppInstallUninstallListener>()
        launcherManager.registerAppInstallUninstallListener(appInstallUninstallListener)
        verify(context).registerReceiver(any(), any())
    }

    @Test
    fun `unregisterAppInstallUninstallListener should unregister for broadcast receiver`() {
        val appInstallUninstallListener = mock<LauncherManager.AppInstallUninstallListener>()
        launcherManager.unregisterAppInstallUninstallListener(appInstallUninstallListener)
        verify(context).unregisterReceiver(any())
    }

    @Test
    fun `launchApp should open given app`() {
        val app = getAppsList().first()
        val intent = mock<Intent>()
        whenever(packageManager.getLaunchIntentForPackage(eq(app.packageName))).thenReturn(intent)
        launcherManager.launchApp(app)
        verify(context).startActivity(eq(intent))
    }

    @Test
    fun `app uninstall should notify registered listeners`() {
        val appsList = getAppsList()
        getAllAppsSetup(appsList)
        val appToUninstall = launcherManager.getAllInstalledApps().first()

        val appInstallUninstallListener = mock<LauncherManager.AppInstallUninstallListener>()
        launcherManager.registerAppInstallUninstallListener(appInstallUninstallListener)

        val captor = ArgumentCaptor.forClass(BroadcastReceiver::class.java)
        verify(context).registerReceiver(captor.capture(), any())

        val intent = mock<Intent>()
        whenever(intent.action).thenReturn(Intent.ACTION_PACKAGE_REMOVED)
        val uri = mock<Uri>()
        whenever(uri.toString()).thenReturn("package:${appToUninstall.packageName}")
        whenever(intent.data).thenReturn(uri)

        captor.value.onReceive(context, intent)
        verify(appInstallUninstallListener).onAppUninstall(eq(appToUninstall))
    }

    private fun getAppsList(): List<App> {
        return listOf(
            App(
                appName = "Gmail",
                packageName = "com.google.gmail",
                mainActivity = "GMailMainActivity",
                icon = bitmap,
                versionName = "1.0.0",
                versionCode = 1
            ),

            App(
                appName = "Facebook",
                packageName = "com.facebook.app",
                mainActivity = "FacebookMainActivity",
                icon = bitmap,
                versionName = "1.0.1",
                versionCode = 2
            ),

            App(
                appName = "Instagram",
                packageName = "com.facebook.instagram",
                mainActivity = "InstagramMainActivity",
                icon = bitmap,
                versionName = "1.0.2",
                versionCode = 3
            )
        )
    }
}