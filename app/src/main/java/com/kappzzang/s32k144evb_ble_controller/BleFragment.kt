package com.kappzzang.s32k144evb_ble_controller

import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kappzzang.s32k144evb_ble_controller.data.BleScanItem
import com.kappzzang.s32k144evb_ble_controller.databinding.FragmentBleBinding

class BleFragment : Fragment() {
    private lateinit var binding: FragmentBleBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bleScanner: BluetoothLeScanner
    private var bleScanList = mutableListOf<BleScanItem>()
    private lateinit var bleListAdapter: BleListAdapter
    @RequiresApi(Build.VERSION_CODES.S)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var allGranted = true
            permissions.entries.forEach {
                if (!it.value) {
                    allGranted = false
                }
            }
            if (allGranted) {
                startBleScan()
            } else {
                Toast.makeText(requireContext(), "권한이 일부 거부되어 측정할 수 없어요.", Toast.LENGTH_SHORT).show()
            }
        }

    private val bleScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                val device = it.device
                val deviceName = device.name ?: "Unknown Device"
                val deviceAddress = device.address
                val rssi = it.rssi
                Log.d(TAG, "Device found: $deviceName - $deviceAddress, RSSI: $rssi")
                addDeviceToList(deviceName, deviceAddress, rssi)
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            super.onBatchScanResults(results)
            results?.forEach { result ->
                result?.let {
                    val device = it.device
                    Log.d(TAG,it.device.name.toString())
                    val deviceName = device.name ?: "Unknown Device"
                    val deviceAddress = device.address
                    val rssi = it.rssi
                    Log.d(TAG, "Batch Device found: $deviceName - $deviceAddress, RSSI: $rssi")
                    addDeviceToList(deviceName, deviceAddress, rssi)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBleBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
        initBluetooth()
        initAdapter()
        initSearchBox()

    }

    override fun onPause() {
        super.onPause()
        stopBleScan()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, bleScanList.size.toString())
    }
    @SuppressLint("MissingPermission")
    private fun connectToDevice(bleScanItem: BleScanItem) {
        val device = bluetoothAdapter.getRemoteDevice(bleScanItem.address)
        device.createBond()
        device?.let {
//            it.create
            it.connectGatt(requireContext(), false, @SuppressLint("MissingPermission")
            object : BluetoothGattCallback() {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt?,
                    status: Int,
                    newState: Int
                ) {
                    super.onConnectionStateChange(gatt, status, newState)
                }
            })
        }
    }
    private fun addDeviceToList(name: String, uuid: String, rss: Int) {
        if (bleScanList.none {it.address == uuid}) {
            bleScanList.add(
                BleScanItem(name, uuid, rss)
            )
        } else {
            bleScanList = bleScanList.map {
                if (it.address == uuid) it.copy(rss = rss) else it
            }.toMutableList()
        }
        val filterText = binding.bleSearchBoxEdittext.text.toString()
        if (filterText.isEmpty())
            bleListAdapter.submitList(bleScanList.toMutableList())
        else
            bleListAdapter.submitList(
                bleScanList.filter {
                    it.name.contains(filterText, ignoreCase = true)
                }.toMutableList()
            )
//        Log.d(TAG, bleListAdapter.currentList.size.toString())
    }

    private fun initSearchBox() {
        binding.bleSearchBoxEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    bleListAdapter.submitList(bleScanList.toMutableList())
                } else {
                    bleListAdapter.submitList(
                        bleScanList.filter {
                            it.name.contains(s.toString(), ignoreCase = true)
                        }.toMutableList()
                    )
                }
            }
        })
    }
    private fun initAdapter() {
        bleListAdapter = BleListAdapter(requireContext())
        binding.bleResultRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bleListAdapter
            bleListAdapter.submitList(bleScanList)
        }
    }
    private fun initBluetooth() {
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (!bluetoothManager.adapter.isEnabled) {
            Toast.makeText(requireContext(), "블루투스가 꺼져있어요", Toast.LENGTH_SHORT).show()
        } else {
            bleScanner = bluetoothManager.adapter.bluetoothLeScanner
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun initButton() {
        binding.bleScanStartButton.setOnClickListener {
            checkPermissionsAndGrant()
        }
        binding.bleScanStopButton.setOnClickListener {
            stopBleScan()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermissionsAndGrant() {
        val requiredPermissions = mutableListOf<String>()
        PERMISSIONS_ABOVE_S.forEach {
            if (ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(it)
            }
        }
        if (requiredPermissions.isNotEmpty()) {
            var showRationale = false
            requiredPermissions.forEach {
                if (shouldShowRequestPermissionRationale(it)) {
                    showRationale = true
                }
            }

            if (showRationale) {

            } else {
                requestPermissionLauncher.launch(requiredPermissions.toTypedArray())
            }
        } else {
            startBleScan()
        }
    }

    private fun startBleScan() {
        bleScanList.clear()
        bleScanner.startScan(bleScanCallback)
        Log.d(TAG, "Scan started")
        Toast.makeText(requireContext(), "스캔 시작", Toast.LENGTH_SHORT).show()
        binding.bleScanStartButton.isEnabled = false
        binding.bleScanStopButton.isEnabled = true
    }

    private fun stopBleScan() {
        bleScanner.stopScan(bleScanCallback)
        binding.bleScanStopButton.isEnabled = false
        binding.bleScanStartButton.isEnabled = true
        Log.d(TAG, "Scan stopped")
    }
    companion object {
        const val TAG = "BLE_FRAGMENT"
        @RequiresApi(Build.VERSION_CODES.S)
        val PERMISSIONS_ABOVE_S = listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}