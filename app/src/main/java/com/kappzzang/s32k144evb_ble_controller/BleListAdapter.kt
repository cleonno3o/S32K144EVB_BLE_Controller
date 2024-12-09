package com.kappzzang.s32k144evb_ble_controller

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kappzzang.s32k144evb_ble_controller.data.BleScanItem
import com.kappzzang.s32k144evb_ble_controller.databinding.ItemBleBinding

class BleListAdapter(
    private val context: Context
//    private val onScanItemClickListener: () -> Unit
) : ListAdapter<BleScanItem, BleListAdapter.ScanItemViewHolder>(
    object :
        DiffUtil.ItemCallback<BleScanItem>() {
        override fun areItemsTheSame(oldItem: BleScanItem, newItem: BleScanItem): Boolean {
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(oldItem: BleScanItem, newItem: BleScanItem): Boolean =
            oldItem == newItem
    }
) {
    inner class ScanItemViewHolder(
        private val binding: ItemBleBinding,
//        private val onScanItemClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
//                onScanItemClickListener()
            }
        }

        fun bind(bleScanItem: BleScanItem) {
            binding.bleScanItem = bleScanItem
            Log.d("ADAPTER", "bind")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanItemViewHolder {
        val viewHolder = ScanItemViewHolder(
            ItemBleBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
//            onScanItemClickListener
        )
//        Log.d("ADAPTER", "bind")

        return viewHolder
    }

    override fun getItemCount(): Int = currentList.size

    override fun onBindViewHolder(holder: ScanItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}