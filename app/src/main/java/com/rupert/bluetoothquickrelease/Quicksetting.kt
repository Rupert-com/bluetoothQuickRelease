package com.rupert.bluetoothquickrelease

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService


class Quicksetting : TileService() {
    private var bluetooth: Bluetooth = Bluetooth()

    private var storage: Storage = Storage(
        getSharedPreferences(
            FullscreenActivity.SHARED_PREFERENCES, MODE_PRIVATE
        )
    )

    override fun onTileAdded() {
        super.onTileAdded()
        val tile = qsTile
        tile.state = Tile.STATE_ACTIVE
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        bluetooth.scan(
            baseContext,
            null,
            storage.reStore<Set<String>>(FullscreenActivity.SAVE_SELECTED_DEVICES)
        )

        bluetooth.disconnectSelectedConnectedBluetoothDevices(baseContext)
    }
}