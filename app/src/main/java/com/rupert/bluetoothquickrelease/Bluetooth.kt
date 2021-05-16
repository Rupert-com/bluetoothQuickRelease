package com.rupert.bluetoothquickrelease

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.rupert.util.Loggable
import com.rupert.util.error
import com.rupert.util.info
import java.util.*


class Bluetooth : Permission(), Loggable {
    var bluetoothDevices: MutableList<BluetoothDevice> = mutableListOf()
    var bluetoothDevicesSelected: MutableList<BluetoothDevice> = mutableListOf()


    fun getSelectedDevicesMAC(): Set<String> =
        bluetoothDevicesSelected.map { cd -> cd.address }.toSet()

    fun getSelectedConnectedDevicesName(): Set<String> =
        getSelectedConnectedDevices().map { cd -> cd.name }.toSet()

    fun getSelectedConnectedDevices(): List<BluetoothDevice> =
        bluetoothDevicesSelected.filter { bluetoothDevice ->
            val cMethod = bluetoothDevice::class.java.getMethod("isConnected")
            return@filter cMethod.invoke(bluetoothDevice) as Boolean
        }

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
                if (restoredDevices?.contains(bluetoothDevice.address) == true || bluetoothDevicesSelected.contains(
                        bluetoothDevice
                    )
                ) {
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
        getSelectedConnectedDevices().forEach { bluetoothDevice ->
            if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
                disconnect(context, bluetoothDevice)

                Toast.makeText(context, "disconnect:" + bluetoothDevice.name, Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    context,
                    "not disconnected:" + bluetoothDevice.name,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/bluetooth/BluetoothA2dp.java
    private fun disconnect(context: Context, device: BluetoothDevice) {
        val serviceListener: ServiceListener = object : ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                try {
                    val disconnectMethod =
                        BluetoothA2dp::class.java.getDeclaredMethod(
                            "disconnect",
                            BluetoothDevice::class.java
                        )
                    // TODO was macht diese Accessible genau?
                    disconnectMethod.isAccessible = true
                    val cReturn = disconnectMethod.invoke(proxy, device) as Boolean

                    info("cReturn $cReturn")
                } catch (ex: Throwable) {
                    error("disconnectMethod", ex)
                } finally {
                    bluetoothAdapter!!.closeProfileProxy(profile, proxy)
                }
            }

            override fun onServiceDisconnected(profile: Int) {}
        }
        bluetoothAdapter!!.getProfileProxy(context, serviceListener, BluetoothProfile.A2DP)
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private fun hasAllPermissions(context: Context): Boolean =
        permissions().any { cPermission -> !hasPermissionGranted(context, cPermission) }

    private fun supportsBluetooth() = (bluetoothAdapter != null)

    private fun enableBluetooth(activity: Activity) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        ActivityCompat.startActivityForResult(
            activity,
            enableBtIntent,
            FullscreenActivity.Intent_REQUEST_ENABLE_BT,
            null
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