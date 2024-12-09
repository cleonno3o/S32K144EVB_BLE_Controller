package com.kappzzang.s32k144evb_ble_controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kappzzang.s32k144evb_ble_controller.databinding.FragmentBleBinding

class BleFragment : Fragment() {
    private lateinit var binding: FragmentBleBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBleBinding.inflate(inflater, container, false)
        return binding.root
    }
}