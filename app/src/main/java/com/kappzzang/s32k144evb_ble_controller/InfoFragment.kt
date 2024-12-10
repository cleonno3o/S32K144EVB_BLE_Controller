package com.kappzzang.s32k144evb_ble_controller

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.kappzzang.s32k144evb_ble_controller.databinding.FragmentInfoBinding

class InfoFragment : Fragment() {
    private lateinit var binding: FragmentInfoBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
    }

    private fun initWebView() {
        with(binding.mainCameraWebview.settings) {
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        binding.mainCameraTitleTextview.setOnClickListener {
            showAddressInputDialog()
        }
    }

    private fun showAddressInputDialog() {
        // 다이얼로그 뷰 바인딩
        val urlBox = EditText(requireContext())
        // AlertDialog 생성
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Enter URL")
            .setView(urlBox)
            .setPositiveButton("연결하기") { _, _ ->
                val url = urlBox.text.toString()
                if (url.isNotEmpty()) {
                    binding.mainCameraWebview.loadUrl(url)
                }
            }
            .setNegativeButton("취소하기", null)
            .create()
        dialog.show()
    }

    companion object {
        const val TAG = "INFO"
    }
}