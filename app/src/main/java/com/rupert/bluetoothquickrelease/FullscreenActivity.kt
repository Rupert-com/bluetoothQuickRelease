package com.rupert.bluetoothquickrelease

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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

    private var toast: Toast? = null
    private var bluetoothDevices: MutableList<BluetoothDevice> = mutableListOf()
    private var bluetoothDevicesSelected: MutableList<BluetoothDevice> = mutableListOf()

    private fun getSelectedDevicesMACList(): Set<String>? =
        bluetoothDevicesSelected.map { cd -> cd.address }.toSet()


    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_fullscreen)
            storage = Storage(getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE))

            btnScan = findViewById(R.id.buttonScan)
            lvDevices = findViewById(R.id.listView)
            lvDevicesSelected = findViewById(R.id.listView_selected)
            lvDevicesSelected.onItemClickListener = onBluetoothDeviceSelectedClick
            lvDevices.onItemClickListener = onBluetoothDeviceClick
            btnScan.setOnClickListener(onBtnScanClick)

            scan(storage.reStore(SAVE_SELECTED_DEVICES, emptySet()))
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        }
    }

    private val onBtnScanClick = View.OnClickListener {
        scan()
    }

    private val onBluetoothDeviceClick =
        AdapterView.OnItemClickListener { parent, view, position, id ->
            addBluetoothDevice(bluetoothDevicesSelected[position], true)
        }

    private val onBluetoothDeviceSelectedClick =
        AdapterView.OnItemClickListener { parent, view, position, id ->
            addBluetoothDevice(bluetoothDevicesSelected[position])
        }


    private fun addBluetoothDevice(bd: BluetoothDevice, selected: Boolean = false) {
        if (selected && bluetoothDevicesSelected.size < MAX_SELECTED_DEVICES) {
            if (!bluetoothDevicesSelected.contains(bd)) {
                bluetoothDevicesSelected.add(bd)
                if (bluetoothDevices.contains(bd)) {
                    bluetoothDevices.remove(bd)
                }
            }
        } else {
            if (selected && bluetoothDevicesSelected.size >= MAX_SELECTED_DEVICES) {
                toast?.cancel()
                toast = Toast.makeText(
                    baseContext,
                    "You have reached a max of ${MAX_SELECTED_DEVICES} devices",
                    Toast.LENGTH_SHORT
                )
                toast!!.show()
            }
            if (!bluetoothDevices.contains(bd)) {
                bluetoothDevices.add(bd)
                if (bluetoothDevicesSelected.contains(bd)) {
                    bluetoothDevicesSelected.remove(bd)
                }
            }
        }

        renderBluetoothDevices()
        storage.store(SAVE_SELECTED_DEVICES, getSelectedDevicesMACList())
    }

    private fun scan(restoredDevices: Set<String>? = null) {
        if (!this.hasAllPermissions()) {
            this.requestPermissions(baseContext, this);
            btnScan.setText(R.string.btn_scan_retry)
            return;
        }

        if (this.supportsBluetooth()) {
            this.enableBluetooth()
            bluetoothAdapter?.bondedDevices?.forEach ble@{ bluetoothDevice ->
                if (restoredDevices?.contains(bluetoothDevice.address) == true) {
                    addBluetoothDevice(bluetoothDevice, true)
                    return@ble
                }
                addBluetoothDevice(bluetoothDevice)
            }
        } else {
            btnScan.setText(R.string.btn_not_supportet)
        }
    }

    private fun renderBluetoothDevices() {
        val listItems = arrayOfNulls<String>(bluetoothDevices.size)
        val listItemsSelected = arrayOfNulls<String>(bluetoothDevicesSelected.size)

        for (i in 0 until bluetoothDevices.size) {
            val cDevice = bluetoothDevices[i]
            listItems[i] = cDevice.name
        }

        for (i in 0 until bluetoothDevicesSelected.size) {
            val cDevice = bluetoothDevicesSelected[i]
            listItemsSelected[i] = cDevice.name
        }

        lvDevices.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        lvDevicesSelected.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, listItemsSelected)
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private fun hasAllPermissions(): Boolean =
        permissions().any { cPermission -> !hasPermissionGranted(cPermission) }

    private fun supportsBluetooth() = (bluetoothAdapter != null)

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        ActivityCompat.startActivityForResult(
            this, enableBtIntent,
            Intent_REQUEST_ENABLE_BT, null
        )
    }

    private fun requestPermissions(baseContext: Context, activity: Activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasPermissionGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                ActivityCompat.requestPermissions(
                    activity,
                    permissions(),
                    Permission_ACCESS_BACKGROUND_LOCATION
                )
            }
        }
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

                        // Permission Granted
                    }
                }
            }
        }
    }

    private fun hasPermissionGranted(pPermission: String): Boolean =
        ContextCompat.checkSelfPermission(
            baseContext,
            pPermission
        ) == PackageManager.PERMISSION_GRANTED

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

    private fun permissions(): Array<String> =
        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    companion object {
        private const val SHARED_PREFERENCES: String = "SHARED_PREFERENCES"
        private const val SAVE_SELECTED_DEVICES: String = "SAVE_SELECTED_DEVICES"
        private const val MAX_SELECTED_DEVICES: Int = 3
        private const val Intent_REQUEST_ENABLE_BT: Int = 1
        private const val Permission_ACCESS_BACKGROUND_LOCATION: Int = 2
    }
}