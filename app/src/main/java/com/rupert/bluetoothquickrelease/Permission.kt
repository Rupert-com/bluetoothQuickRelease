package com.rupert.bluetoothquickrelease

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

open class Permission() {

    protected fun hasPermissionGranted(context: Context, pPermission: String): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            pPermission
        ) == PackageManager.PERMISSION_GRANTED
}