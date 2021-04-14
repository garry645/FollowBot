package com.example.followbot

import android.Manifest
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()

        val pairedDevicesAsList = BluetoothAdapter.getDefaultAdapter().bondedDevices.toMutableList()
        val openCameraOnClick =View.OnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
        val bluetoothAdapter = BluetoothItemsAdapter(pairedDevicesAsList, openCameraOnClick)
        val recyclerView = findViewById<RecyclerView>(R.id.deviceList)
        recyclerView.adapter = bluetoothAdapter
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)

    }

    private fun requestPermissions() {
        EasyPermissions.requestPermissions(
                this,
                "In order to use FollowBot you must accept camera and bluetooth permissions",
                0,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH_ADMIN
        )
    }
}