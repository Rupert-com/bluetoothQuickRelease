package com.rupert.bluetoothquickrelease

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity() : AppCompatActivity() {
    private lateinit var btnScan: Button
    private lateinit var lvDevices: ListView
    private lateinit var lvDevicesSelected: ListView
    private lateinit var storage: Storage
    private lateinit var bluetooth: Bluetooth

    private var toast: Toast? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_fullscreen)
            storage = Storage(getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE))
            bluetooth = Bluetooth()

            btnScan = findViewById(R.id.buttonScan)
            lvDevices = findViewById(R.id.listView)
            lvDevicesSelected = findViewById(R.id.listView_selected)
            lvDevicesSelected.onItemClickListener = onBluetoothDeviceSelectedClick
            lvDevices.onItemClickListener = onBluetoothDeviceClick
            btnScan.setOnClickListener(onBtnScanClick)

            progressBluetoothScanResult(
                bluetooth.scan(
                    baseContext,
                    this,
                    storage.reStore(SAVE_SELECTED_DEVICES, emptySet())
                )
            )

            renderBluetoothDevices()
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        }
    }

    private fun progressBluetoothScanResult(result: ScanReturn) {

        when (result) {
            ScanReturn.ERROR_PERMISSION -> btnScan.setText(R.string.btn_scan_retry)
            ScanReturn.ERROR_DEVICE_NOT_SUPPORTED -> btnScan.setText(R.string.btn_not_supportet)
            ScanReturn.SUCCESS -> return
            else -> throw NotImplementedError("ScanReturn.SUCCESS not Supportet yet")
        }

    }

    private val onBtnScanClick = View.OnClickListener {
        progressBluetoothScanResult(
            bluetooth.scan(baseContext, this)
        )

        // bluetooth.disconnectSelectedConnectedBluetoothDevices(this)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val onBluetoothDeviceClick =
        AdapterView.OnItemClickListener { parent, view, position, id ->
            addBluetoothDevice(position, true)
        }

    private val onBluetoothDeviceSelectedClick =
        AdapterView.OnItemClickListener { parent, view, position, id ->
            addBluetoothDevice(position)
        }


    private fun addBluetoothDevice(position: Int, selected: Boolean = false) {
        val result = bluetooth.addBluetoothDevice(position, selected)

        if (result == false) {
            toast?.cancel()
            toast = Toast.makeText(
                baseContext,
                "You have reached a max of ${FullscreenActivity.MAX_SELECTED_DEVICES} devices",
                Toast.LENGTH_SHORT
            )
            toast!!.show()
        }

        renderBluetoothDevices()
        storage.store(SAVE_SELECTED_DEVICES, bluetooth.getSelectedDevicesMAC())
    }

    private fun renderBluetoothDevices() {
        val listItems = arrayOfNulls<String>(bluetooth.bluetoothDevices.size)
        val listItemsSelected = arrayOfNulls<String>(bluetooth.bluetoothDevicesSelected.size)

        for (i in 0 until bluetooth.bluetoothDevices.size) {
            val cDevice = bluetooth.bluetoothDevices[i]
            listItems[i] = cDevice.name
        }

        for (i in 0 until bluetooth.bluetoothDevicesSelected.size) {
            val cDevice = bluetooth.bluetoothDevicesSelected[i]
            listItemsSelected[i] = cDevice.name
        }

        lvDevices.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        lvDevicesSelected.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, listItemsSelected)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Permission_ACCESS_BACKGROUND_LOCATION -> {
                for ((cCount, permission) in permissions.withIndex()) {
                    if (grantResults[cCount] == PackageManager.PERMISSION_GRANTED) {
                    }
                }
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Intent_REQUEST_ENABLE_BT -> {
                if (resultCode == RESULT_OK) {

                }
            }
        }
    }

    companion object {
        const val SHARED_PREFERENCES: String = "SHARED_PREFERENCES"
        const val SAVE_SELECTED_DEVICES: String = "SAVE_SELECTED_DEVICES"
        const val MAX_SELECTED_DEVICES: Int = 3
        const val Intent_REQUEST_ENABLE_BT: Int = 1
        const val Permission_ACCESS_BACKGROUND_LOCATION: Int = 2
    }
}