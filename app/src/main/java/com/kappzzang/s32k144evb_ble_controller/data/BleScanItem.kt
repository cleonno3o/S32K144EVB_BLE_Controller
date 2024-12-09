package com.kappzzang.s32k144evb_ble_controller.data

data class BleScanItem(
    val name: String,
    val uuid: String,
    val rss: Int
)