package com.danh.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.danh.myapplication.databinding.FragmentSettingBinding


class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        // Load dữ liệu đã lưu
        binding.edtEmergencyPhone.setText(prefs.getString("emergency_phone", "0334679392"))
        binding.edtUserName.setText(prefs.getString("user_name", ""))
        binding.edtAddress.setText(prefs.getString("address", ""))
        val sensitivity = prefs.getInt("sensitivity", 50)
        binding.seekbarSensitivity.progress = sensitivity
        binding.tvSensitivityValue.text = "Độ nhạy: $sensitivity%"

        // Lưu số điện thoại khẩn cấp
        binding.btnSavePhone.setOnClickListener {
            val phone = binding.edtEmergencyPhone.text.toString()
            if (phone.isNotEmpty()) {
                prefs.edit().putString("emergency_phone", phone).apply()
                Toast.makeText(requireContext(), "Đã lưu số điện thoại!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show()
            }
        }

        // Lưu thông tin người dùng
        binding.btnSaveInfo.setOnClickListener {
            val name = binding.edtUserName.text.toString()
            val address = binding.edtAddress.text.toString()
            prefs.edit()
                .putString("user_name", name)
                .putString("address", address)
                .apply()
            Toast.makeText(requireContext(), "Đã lưu thông tin!", Toast.LENGTH_SHORT).show()
        }

        // SeekBar độ nhạy
        binding.seekbarSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvSensitivityValue.text = "Độ nhạy: $progress%"
                prefs.edit().putInt("sensitivity", progress).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Nút Test System
        binding.btnTestSystem.setOnClickListener {
            sendTestNotification()
            Toast.makeText(requireContext(), "Đã gửi thông báo test!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendTestNotification() {
        val channelId = "fall_channel"
        
        // Tạo channel nếu chưa có
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Fall alert",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🧪 Test Hệ Thống")
            .setContentText("Thông báo test - Hệ thống hoạt động tốt!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(9999, notification)
    }
}