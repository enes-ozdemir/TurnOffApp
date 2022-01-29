package com.enesozdemir.turnoffapp.util

import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log


class Utility {

    fun turnOffWifi(context: Context) {
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifi.isWifiEnabled = false
    }

    fun turnOffBluetooth() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.disable()
        }
    }

    fun turnOffCamera(context: Context) {
        val cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0];
        cameraManager.setTorchMode(cameraId, true);

    }

    fun openDoNotDisturb(context: Context) {
        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        mNotificationManager!!.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
    }


}