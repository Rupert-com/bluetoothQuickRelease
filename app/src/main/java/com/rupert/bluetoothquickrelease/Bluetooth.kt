package com.rupert.bluetoothquickrelease

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Build
import android.system.Os.socket
import androidx.core.app.ActivityCompat


class Bluetooth : Permission() {
    var bluetoothDevices: MutableList<BluetoothDevice> = mutableListOf()
    var bluetoothDevicesSelected: MutableList<BluetoothDevice> = mutableListOf()


    fun getSelectedDevicesMACList(): Set<String>? =
        bluetoothDevicesSelected.map { cd -> cd.address }.toSet()

    /**
     * @return
     * *true*: All added perfectly
     * *false*: [FullscreenActivity.MAX_SELECTED_DEVICES] reached
     * *null*: Nothing changed
     */
    fun addBluetoothDevice(position: Int, selected: Boolean = false): Boolean? {
        if (selected && bluetoothDevicesSelected.size < FullscreenActivity.MAX_SELECTED_DEVICES)
            return addBluetoothDevice(bluetoothDevices[position], selected)
        return addBluetoothDevice(bluetoothDevicesSelected[position], selected)
    }

    fun addBluetoothDevice(bd: BluetoothDevice, selected: Boolean = false): Boolean? {
        var success: Boolean? = null

        if (selected && bluetoothDevicesSelected.size < FullscreenActivity.MAX_SELECTED_DEVICES) {

            if (!bluetoothDevicesSelected.contains(bd)) {
                success = bluetoothDevicesSelected.add(bd)
                if (bluetoothDevices.contains(bd)) {
                    bluetoothDevices.remove(bd)
                }
            }
        } else {
            if (selected && bluetoothDevicesSelected.size >= FullscreenActivity.MAX_SELECTED_DEVICES)
                return false

            if (!bluetoothDevices.contains(bd)) {
                success = bluetoothDevices.add(bd)

                if (bluetoothDevicesSelected.contains(bd)) {
                    bluetoothDevicesSelected.remove(bd)
                }
            }
        }

        return success
    }

    /**
     * @param activity must be set if a activity avariable. It is used to check the permission and to enable Bluetooth
     */
    fun scan(
        context: Context,
        activity: Activity? = null,
        restoredDevices: Set<String>? = null
    ): ScanReturn {
        if (!this.hasAllPermissions(context)) {
            if (activity != null)
                this.requestPermissions(context, activity);
            return ScanReturn.ERROR_PERMISSION;
        }

        if (this.supportsBluetooth()) {
            if (activity != null)
                this.enableBluetooth(activity)
            bluetoothAdapter?.bondedDevices?.forEach ble@{ bluetoothDevice ->
                if (restoredDevices?.contains(bluetoothDevice.address) == true) {
                    addBluetoothDevice(bluetoothDevice, true)
                    return@ble
                }
                addBluetoothDevice(bluetoothDevice)
            }

            return ScanReturn.SUCCESS
        }
        return ScanReturn.ERROR_DEVICE_NOT_SUPPORTED
    }

    fun disconnectSelectedConnectedBluetoothDevices(context: Context) {
        bluetoothDevicesSelected.forEach { bluetoothDevice ->
            if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
                disconnect(context, bluetoothDevice)
            }
        }
    }

    private fun disconnect(context: Context, device: BluetoothDevice) {
        val serviceListener: BluetoothProfile.ServiceListener = object :
            BluetoothProfile.ServiceListener {
            override fun onServiceDisconnected(profile: Int) {}

            @SuppressLint("DiscouragedPrivateApi")
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                val disconnect = BluetoothA2dp::class.java.getDeclaredMethod(
                    "disconnect",
                    BluetoothDevice::class.java
                )
                disconnect.isAccessible = true
                disconnect.invoke(proxy, device)
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(profile, proxy)
            }
        }
        BluetoothAdapter.getDefaultAdapter()
            .getProfileProxy(context, serviceListener, BluetoothProfile.A2DP)
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private fun hasAllPermissions(context: Context): Boolean =
        permissions().any { cPermission -> !hasPermissionGranted(context, cPermission) }

    private fun supportsBluetooth() = (bluetoothAdapter != null)

    private fun enableBluetooth(activity: Activity) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        ActivityCompat.startActivityForResult(
            activity, enableBtIntent,
            FullscreenActivity.Intent_REQUEST_ENABLE_BT, null
        )
    }

    private fun requestPermissions(context: Context, activity: Activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasPermissionGranted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                ActivityCompat.requestPermissions(
                    activity,
                    permissions(),
                    FullscreenActivity.Permission_ACCESS_BACKGROUND_LOCATION
                )
            }
        }
    }


    private fun permissions(): Array<String> =
        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)


}

enum class ScanReturn {
    ERROR_PERMISSION, ERROR_DEVICE_NOT_SUPPORTED, SUCCESS
}