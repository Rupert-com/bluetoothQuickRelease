package com.rupert.bluetoothquickrelease

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.rupert.util.Loggable


class Quicksetting : TileService(), Loggable {

    private val icTileFull by lazy { Icon.createWithResource(this, R.drawable.broken_link_dark) }
    private val icTileEmpty by lazy { Icon.createWithResource(this, R.drawable.broken_link_light) }
    private val bluetooth: Bluetooth = Bluetooth()
    private var storage: Storage? = null

    override fun onTileAdded() {
        updateTile()
        super.onTileAdded()
    }

    override fun onStartListening() {
        if (isLocked) {
            updateTile(state = Tile.STATE_UNAVAILABLE)
            return
        }
        super.onStartListening()
    }


    override fun onClick() {
        storage = Storage(
            getSharedPreferences(
                FullscreenActivity.SHARED_PREFERENCES, MODE_PRIVATE
            )
        )

        bluetooth.scan(
            this,
            null,
            storage!!.reStore<Set<String>>(FullscreenActivity.SAVE_SELECTED_DEVICES, emptySet())
        )

        toast("Devices: " + bluetooth.getSelectedConnectedDevicesName().joinToString { s -> s })

        bluetooth.disconnectSelectedConnectedBluetoothDevices(this)
        onTick()
        super.onClick()
    }

    private fun updateTile(
        state: Int = Tile.STATE_INACTIVE,
        icon: Icon = icTileEmpty
    ) {

        qsTile ?: return
        qsTile.state = state
        qsTile.icon = icon
        qsTile.updateTile()
    }

    private fun onTick() {
        if (true || qsTile.state == Tile.STATE_ACTIVE) {
            updateTile()
        } else {
            updateTile(Tile.STATE_ACTIVE, icTileFull)
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}