package com.pillsense.app.core.util.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import timber.log.Timber

enum class AppPermission(val manifestPermission: String) {
    NOTIFICATIONS(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.POST_NOTIFICATIONS
        else ""
    ),
    EXACT_ALARM(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            Manifest.permission.SCHEDULE_EXACT_ALARM
        else ""
    ),
    CAMERA(Manifest.permission.CAMERA);
}

object PermissionManager {

    fun isGranted(context: Context, permission: AppPermission): Boolean {
        if (permission.manifestPermission.isBlank()) return true
        return ContextCompat.checkSelfPermission(
            context, permission.manifestPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(
        launcher: ActivityResultLauncher<String>,
        permission: AppPermission
    ) {
        if (permission.manifestPermission.isBlank()) return
        Timber.d("PermissionManager: solicitando %s", permission.manifestPermission)
        launcher.launch(permission.manifestPermission)
    }

    fun requestMultiplePermissions(
        launcher: ActivityResultLauncher<Array<String>>,
        permissions: List<AppPermission>
    ) {
        val toRequest = permissions
            .filter { it.manifestPermission.isNotBlank() }
            .map { it.manifestPermission }
            .toTypedArray()
        if (toRequest.isNotEmpty()) launcher.launch(toRequest)
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun openBatteryOptimizationSettings(activity: Activity) {
        Timber.d("PermissionManager: abriendo ajustes de optimización de batería")
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "No se pudo abrir ajustes de batería, abriendo ajustes generales")
            activity.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }
}
