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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity() : AppCompatActivity() {
    private val SHARED_PREFERENCES: String = "SHARED_PREFERENCES"
    private val SAVE_TEST: String = "SAVE_TEST"
    private val SAVE_SELECTED_DEVICES: String = "SAVE_SELECTED_DEVICES"
    private val MAX_SELECTED_DEVICES: Int = 3
    private lateinit var btn_Scan: Button
    private lateinit var lv_Devices: ListView
    private lateinit var lv_Devices_selected: ListView

    private var cToast: Toast? = null

    private var bluetoothDevices: MutableList<BluetoothDevice> = mutableListOf()
    private var bluetoothDevices_selected: MutableList<BluetoothDevice> = mutableListOf()
    private lateinit var SharedPref: SharedPreferences

    private fun getSelectedDevicesMACList(): Set<String>? =
        bluetoothDevices_selected.map { cd -> cd.address }.toSet()


    override fun onCreate(savedInstanceState: Bundle?) {
        try {

            super.onCreate(savedInstanceState)
            SharedPref = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE)

            setContentView(R.layout.activity_fullscreen)

            btn_Scan = findViewById(R.id.buttonScan)
            lv_Devices = findViewById<ListView>(R.id.listView)
            lv_Devices_selected = findViewById<ListView>(R.id.listView_selected)


            lv_Devices_selected.setOnItemClickListener(AdapterView.OnItemClickListener() { parent, view, position, id ->
                bluetoothDevices.add(bluetoothDevices_selected.get(position))
                bluetoothDevices_selected.removeAt(position)

                renderBluetoothDevices()

                Store(SAVE_SELECTED_DEVICES, getSelectedDevicesMACList())
                //  return@OnItemClickListener true
            })



            lv_Devices.setOnItemClickListener(AdapterView.OnItemClickListener() { parent, view, position, id ->

                if (bluetoothDevices_selected.size < MAX_SELECTED_DEVICES) {
                    bluetoothDevices_selected.add(bluetoothDevices.get(position))
                    bluetoothDevices.removeAt(position)

                    renderBluetoothDevices()
                    Store(SAVE_SELECTED_DEVICES, getSelectedDevicesMACList())
                } else {
                    cToast?.cancel()

                    cToast = Toast.makeText(
                        baseContext,
                        "You have reached a max of $MAX_SELECTED_DEVICES devices",
                        Toast.LENGTH_SHORT
                    )

                    cToast!!.show()
                }
                //  return@OnItemClickListener true
            })

            btn_Scan.setOnClickListener { scan() }

            scan(reStore(SAVE_SELECTED_DEVICES, emptySet()))
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        }
    }


    private fun scan(restoredDevices: Set<String>? = null) {
        if (!this.hasAllPermissions()) {
            this.requestPermissions(baseContext, this);
            btn_Scan.setText(R.string.btn_scan_retry)
            return;
        }

        if (this.supportsBluetooth()) {
            this.enableBluetooth()
            bluetoothAdapter?.bondedDevices?.forEach ble@{ bluetoothDevice ->
                if (restoredDevices?.contains(bluetoothDevice.address) == true) {
                    bluetoothDevices_selected.add(bluetoothDevice)

                    return@ble
                }

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


    private inline fun <reified T> Store(KEY: String, Val: T) {
        when (T::class.java) {
            String::class.java -> SharedPref.edit().putString(KEY, Val as String).apply()
            Set::class.java -> { // TODO auch Set<T> checken... nicht nur T<>
                SharedPref.edit().putStringSet(KEY, Val as Set<String>).apply()
            }
            else -> throw Exception("Unhandled return type")
        }
    }

    private inline fun <reified T> reStore(KEY: String, defaultVal: T? = null): T {
        return when (T::class.java) {
            String::class.java -> SharedPref.getString(KEY, defaultVal?.toString()) as T
            Set::class.java -> SharedPref.getStringSet(
                KEY,
                defaultVal as Set<String>
            ) as T
            else -> throw Exception("Unhandled return type")
        }
    }

    companion object {
        var Intent_REQUEST_ENABLE_BT: Int = 1
        var Permission_ACCESS_BACKGROUND_LOCATION: Int = 2
        fun PERMISSIONS(): Array<String> = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
}