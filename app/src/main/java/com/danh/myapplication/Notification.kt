package com.danh.myapplication

data class Notification(
    val time: String,
    val title: String,
    val image: String,
    val type: String = "normal" // "fall" hoặc "normal"
)
