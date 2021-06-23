package com.example.launcher

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import java.util.concurrent.CopyOnWriteArraySet

/**
 * LauncherManager is class that allows clients of Launcher SDK
 * to retrieve list of installed apps, notify about app install/uninstalls
 * @param context [Application] used as context
 */
class LauncherManager private constructor(private val context: Application) {

    companion object {
        private const val SCHEME_PACKAGE = "package"
        private val TAG = LauncherManager::class.java.simpleName

        private var instance: LauncherManager? = null
        private val lock = Any()

        @JvmStatic
        fun getInstance(context: Application): LauncherManager {

            return synchronized(lock) {
                if (instance == null) {
                    instance = LauncherManager(context)
                }
                instance!!
            }
        }
    }

    private val appInstallUninstallListeners = CopyOnWriteArraySet<AppInstallUninstallListener>()
    private val installedApps = CopyOnWriteArraySet<App>()

    /**
     * Function to get [List] of apps installed
     * @return List of [App]'s sorted by app name in ascending order
     */
    fun getAllInstalledApps(): List<App> {
        if (installedApps.isNotEmpty()) return installedApps.sortedBy { it.appName }

        val intent = getLauncherIntent()
        val apps = findAppsUsingIntent(intent)

        installedApps.clear()
        installedApps.addAll(apps)
        return installedApps.sortedBy { it.appName }
    }

    private fun findAppsUsingIntent(intent: Intent): List<App> {
        val packageManager = context.packageManager
        return packageManager.queryIntentActivities(intent, 0)
            .map {
                val packageName = it.activityInfo.packageName
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val versionCode = if (Build.VERSION.SDK_INT >= 28) packageInfo.longVersionCode else packageInfo.versionCode.toLong()
                App(
                    appName = it.loadLabel(packageManager).toString(),
                    packageName = packageName,
                    icon = it.loadIcon(packageManager).getBitmap(),
                    mainActivity = it.activityInfo.name,
                    versionCode = versionCode,
                    versionName = packageInfo.versionName,
                )
            }
    }

    /**
     * Function to register [AppInstallUninstallListener] listeners
     * @param listener [AppInstallUninstallListener] to be registered
     */
    fun registerAppInstallUninstallListener(listener: AppInstallUninstallListener) {
        appInstallUninstallListeners.add(listener)
        if (appInstallUninstallListeners.size > 0) {
            registerForAppInstallUnInstallBroadcast()
        }
    }

    /**
     * Function to unregister [AppInstallUninstallListener] listeners
     * @param listener [AppInstallUninstallListener] to be unregistered
     */
    fun unregisterAppInstallUninstallListener(listener: AppInstallUninstallListener) {
        appInstallUninstallListeners.remove(listener)
        if (appInstallUninstallListeners.isEmpty()) {
            unregisterForAppInstallUnInstallBroadcast()
        }
    }

    /**
     * Function to register [BroadcastReceiver] to receive App install/uninstall
     * broadcast events
     */
    private fun registerForAppInstallUnInstallBroadcast() {
        context.registerReceiver(appInstallUninstallBroadcastReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addDataScheme(SCHEME_PACKAGE)
            }
        )
    }

    /**
     * Function to unregister [BroadcastReceiver] which was registered using
     * {@link #registerForAppInstallUnInstallBroadcast()}
     */
    private fun unregisterForAppInstallUnInstallBroadcast() {
        context.unregisterReceiver(appInstallUninstallBroadcastReceiver)
    }

    /**
     * [BroadcastReceiver] that notifies about app install/uninstall events
     */
    private val appInstallUninstallBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val packageName = intent.data.toString().substringAfter("$SCHEME_PACKAGE:")
            val app: App
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    app = findAppUsingPackageName(packageName)
                    notifyListeners(app, intent.action!!)
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    app = installedApps.find { it.packageName == packageName }!!
                    installedApps.remove(app)
                    notifyListeners(app, intent.action!!)
                }
            }
        }
    }

    /**
     * Function to find app using its package name
     * @param packageName package name
     * @return [App] with given package name
     */
    private fun findAppUsingPackageName(packageName: String): App {
        val intent = getLauncherIntent()
        intent.`package` = packageName
        return findAppsUsingIntent(intent).first()
    }

    /**
     * Function that gives [Intent] with action [Intent.ACTION_MAIN]
     * and category [Intent.CATEGORY_LAUNCHER]
     * @return [Intent]
     */
    private fun getLauncherIntent(): Intent {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        return intent
    }

    /**
     * Function to notify all listeners about app install/uninstall events
     * @param app [App] that got installed
     * @param action either [Intent.ACTION_PACKAGE_ADDED] or [Intent.ACTION_PACKAGE_REMOVED]
     */
    private fun notifyListeners(app: App, action: String) {
        appInstallUninstallListeners.forEach {
            when (action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    it.onAppInstall(app)
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    it.onAppUninstall(app)
                }
            }
        }
    }

    /**
     * Function to launch given [App]
     */
    fun launchApp(app: App){
        context.packageManager.getLaunchIntentForPackage(app.packageName)?.let {
            context.startActivity(it)
        }
    }

    /**
     * Function to uninstall given [App]
     */
    fun uninstallApp(app: App){
        val packageUri: Uri = Uri.parse("$SCHEME_PACKAGE:${app.packageName}")
        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
        uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(uninstallIntent)
    }

    /**
     * Listener that will notify app install/uninstall events
     */
    interface AppInstallUninstallListener {

        /**
         * Function that gets invoked on app install
         * @param app [App] that got installed
         */
        fun onAppInstall(app: App)

        /**
         * Function that gets invoked on app uninstall
         * @param app [App] that got uninstalled
         */
        fun onAppUninstall(app: App)
    }
}