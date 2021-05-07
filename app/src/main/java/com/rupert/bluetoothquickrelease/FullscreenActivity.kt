package com.rupert.bluetoothquickrelease

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.security.Permission

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity() : AppCompatActivity() {
    private lateinit var btn_Scan: Button
    private lateinit var lv_Devices: ListView
    private lateinit var lv_Devices_selected: ListView

    private var bluetoothDevices: MutableList<BluetoothDevice> = mutableListOf()
    private var bluetoothDevices_selected: MutableList<BluetoothDevice> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)

        btn_Scan = findViewById(R.id.buttonScan)
        lv_Devices = findViewById<ListView>(R.id.listView)
        lv_Devices_selected = findViewById<ListView>(R.id.listView_selected)


        lv_Devices_selected.setOnItemLongClickListener(AdapterView.OnItemLongClickListener() { parent, view, position, id ->
            bluetoothDevices.add(bluetoothDevices_selected.get(position))
            bluetoothDevices_selected.removeAt(position)

            renderBluetoothDevices()
            return@OnItemLongClickListener true
        })

        lv_Devices.setOnItemLongClickListener(AdapterView.OnItemLongClickListener() { parent, view, position, id ->

            bluetoothDevices_selected.add(bluetoothDevices.get(position))
            bluetoothDevices.removeAt(position)

            renderBluetoothDevices()
            return@OnItemLongClickListener true
        })
// AdapterView<?> parent, View view, int position, long id
//        lv_Devices.setOnItemClickListener(AdapterView.OnItemClickListener() { parent, view, position, id ->
//            Toast.makeText(this, "Clicked item : $position", Toast.LENGTH_SHORT).show()
        //      })
        btn_Scan.setOnClickListener { scan() }
    }


    private fun scan() {
        if (!this.hasAllPermissions()) {
            this.requestPermissions(baseContext, this);
            btn_Scan.setText(R.string.btn_scan_retry)
            return;
        }

        if (this.supportsBluetooth()) {
            this.enableBluetooth()
            bluetoothAdapter?.bondedDevices?.forEach { bluetoothDevice ->
                if (!bluetoothDevices.contains(bluetoothDevice) && !bluetoothDevices_selected.contains(
                        bluetoothDevice
                    )
                ) {
                    bluetoothDevices.add(bluetoothDevice)
                }
            }
            renderBluetoothDevices()
        } else {
            btn_Scan.setText(R.string.btn_not_supportet)
        }
    }

    private fun renderBluetoothDevices() {
        val listItems = arrayOfNulls<String>(bluetoothDevices.size)
        val listItems_selected = arrayOfNulls<String>(bluetoothDevices_selected.size)

        for (i in 0 until bluetoothDevices.size) {
            val cDevice = bluetoothDevices[i]
            listItems[i] = cDevice.name
        }

        for (i in 0 until bluetoothDevices_selected.size) {
            val cDevice = bluetoothDevices_selected[i]
            listItems_selected[i] = cDevice.name
        }

        lv_Devices.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        lv_Devices_selected.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems_selected)
    }

    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    fun hasAllPermissions(): Boolean =
        PERMISSIONS().filter { cPermission -> !hasPermissionGranted(cPermission) }.size > 0

    fun supportsBluetooth() = (bluetoothAdapter != null)

    fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        ActivityCompat.startActivityForResult(this, enableBtIntent, Intent_REQUEST_ENABLE_BT, null)
    }

    private fun requestPermissions(baseContext: Context, activity: Activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasPermissionGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS(),
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
        ContextCompat.checkSelfPermission(baseContext, pPermission) == PERMISSION_GRANTED

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
        var Intent_REQUEST_ENABLE_BT: Int = 1
        var Permission_ACCESS_BACKGROUND_LOCATION: Int = 2
        fun PERMISSIONS(): Array<String> = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
}