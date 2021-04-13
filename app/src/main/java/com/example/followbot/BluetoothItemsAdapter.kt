package com.example.followbot

import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class BluetoothItemsAdapter(private val bluetoothItems: List<BluetoothDevice>, val onClickListener: View.OnClickListener): RecyclerView.Adapter<BluetoothItemsAdapter.BluetoothItemViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BluetoothItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.bluetooth_item, parent, false)
        return BluetoothItemViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBindViewHolder(
        holder: BluetoothItemViewHolder,
        position: Int
    ) {
        bluetoothItems[position].let {
            holder.bluetoothName.text = it.name
            holder.bluetoothID.text = it.address
        }
        holder.itemView.setOnClickListener(onClickListener)
    }

    override fun getItemCount(): Int {
        return bluetoothItems.size
    }

    class BluetoothItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bluetoothName: TextView = itemView.findViewById(R.id.deviceName)
        val bluetoothID: TextView = itemView.findViewById(R.id.devideID)
    }
}