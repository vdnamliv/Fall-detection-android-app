package com.danh.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.danh.myapplication.databinding.FragmentHomeBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var binding: FragmentHomeBinding
    private var firstLoad = true   // Biến để bỏ qua dữ liệu cũ lúc mới mở app
    private var lastEventTime: Long = 0 // Thời gian của sự kiện cuối cùng
    
    // Handler để kiểm tra online/offline định kỳ
    private val connectionHandler = Handler(Looper.getMainLooper())
    private val checkConnectionRunnable = object : Runnable {
        override fun run() {
            checkConnectionStatus() // Kiểm tra lại trạng thái
            connectionHandler.postDelayed(this, 10000) // Lặp lại mỗi 10 giây
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kết nối đến data1
        database = FirebaseDatabase.getInstance().getReference("data1")
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Fall alert"
            val desc = "Thông báo phát hiện ngã"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = desc
            }
            val manager = requireContext()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        
        // Đọc số điện thoại từ SharedPreferences
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val emergencyPhone = prefs.getString("emergency_phone", EMERGENCY_PHONE) ?: EMERGENCY_PHONE
        
        // Setup nút SOS (đổi từ btn_sos sang btnEmergencyCall)
        binding.btnEmergencyCall.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$emergencyPhone")
            }
            startActivity(callIntent)
        }
        
        listenLatestEvent()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Bắt đầu vòng lặp kiểm tra kết nối khi mở màn hình
        connectionHandler.post(checkConnectionRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Dừng kiểm tra khi thoát màn hình để tiết kiệm pin
        connectionHandler.removeCallbacks(checkConnectionRunnable)
    }

    private fun listenLatestEvent() {
        database.orderByKey()
            .limitToLast(1)
            .addChildEventListener(object : ChildEventListener {

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    // 1. Kiểm tra Fragment còn sống không để tránh lỗi Crash khi thoát app nhanh
                    if (!isAdded || context == null) return

                    // 2. Logic bỏ qua lần load đầu tiên (dữ liệu lịch sử)
                    if (firstLoad) {
                        firstLoad = false
                        return
                    }

                    try {
                        // 3. Lấy dữ liệu từ Firebase
                        val type = snapshot.child("type").value?.toString() ?: "normal"
                        val imageUrl = snapshot.child("imageUrl").value?.toString() ?: ""
                        val rawTimestamp = snapshot.child("timestamp").value?.toString() ?: "0"

                        // 4. Xử lý thời gian (Lưu lại để check online/offline)
                        lastEventTime = try {
                            var t = rawTimestamp.toLong()
                            if (rawTimestamp.length <= 10) t *= 1000 // Chuyển giây -> mili-giây
                            t
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }

                        Log.d("FIREBASE_DATA", "Type: $type, Time: $lastEventTime")

                        // 5. Cập nhật giao diện ngay lập tức
                        updateUI(type, lastEventTime)

                        // 6. Hiển thị ảnh
                        if (imageUrl.isNotEmpty()) {
                            val bitmap = decodeBase64ToBitmap(imageUrl)
                            if (bitmap != null) {
                                binding.imageView.setImageBitmap(bitmap)
                                binding.imageView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                            }
                        }

                        // 7. Thông báo nếu có ngã
                        if (type == "fall") {
                            showNotification(type)
                        }

                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error parsing data: ${e.message}")
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Hàm kiểm tra và cập nhật trạng thái Online/Offline
    private fun checkConnectionStatus() {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - lastEventTime
        
        // Nếu quá 60 giây không có dữ liệu mới -> Coi như Offline
        val isOffline = diff > 60000
        
        if (isOffline && lastEventTime > 0) {
            // Giao diện Offline (Màu xám)
            binding.cardStatus.setCardBackgroundColor(Color.parseColor("#757575"))
            binding.tvStatusTitle.text = "MẤT KẾT NỐI THIẾT BỊ"
            binding.tvStatusSubtitle.text = "Kiểm tra nguồn điện hoặc Wifi"
            binding.tvConnectionStatus.text = "Offline"
            binding.iconStatus.clearAnimation() // Dừng nhấp nháy
        } else if (lastEventTime > 0) {
            // Nếu Online thì trạng thái được set trong updateUI
            binding.tvConnectionStatus.text = "Online"
        }
    }

    private fun updateUI(type: String, timestamp: Long) {
        // Format giờ hiển thị (Ví dụ: 14:30:25)
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        binding.tvLastUpdate.text = sdf.format(Date(timestamp))

        if (type == "fall") {
            // === TRẠNG THÁI NGUY HIỂM (ĐỎ) ===
            binding.cardStatus.setCardBackgroundColor(Color.parseColor("#D32F2F")) // Đỏ
            binding.tvStatusTitle.text = "CẢNH BÁO: CÓ NGƯỜI NGÃ!"
            binding.tvStatusSubtitle.text = "Hệ thống phát hiện sự cố"
            
            // Hiệu ứng nhấp nháy cảnh báo
            val anim = AlphaAnimation(1.0f, 0.4f)
            anim.duration = 500
            anim.repeatCount = Animation.INFINITE
            anim.repeatMode = Animation.REVERSE
            binding.iconStatus.startAnimation(anim)
            
        } else {
            // === TRẠNG THÁI AN TOÀN (XANH) ===
            binding.cardStatus.setCardBackgroundColor(Color.parseColor("#4CAF50")) // Xanh
            binding.tvStatusTitle.text = "HỆ THỐNG AN TOÀN"
            binding.tvStatusSubtitle.text = "Đang giám sát bình thường"
            binding.iconStatus.clearAnimation()
        }
        
        // Cập nhật trạng thái Online
        binding.tvConnectionStatus.text = "Online"
    }

    private fun decodeBase64ToBitmap(base64String: String?): Bitmap? {
        if (base64String.isNullOrEmpty()) return null
        return try {
            val bytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e("DecodeImage", "Lỗi giải mã ảnh: ${e.message}")
            null
        }
    }

    private fun showNotification(type: String?) {
        // Kiểm tra context lần nữa cho an toàn
        val ctx = context ?: return

        val isFall = type == "fall"
        val title = if (isFall) "🚨 CẢNH BÁO NGÃ!" else "Thông báo IoT"
        val content = if (isFall)
            "Phát hiện người ngã. Nhấn để gọi cấp cứu!"
        else
            "Hệ thống ghi nhận trạng thái mới: $type"

        // Intent mở app khi bấm vào notification
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("FROM_NOTIFICATION", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            ctx,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Đảm bảo bạn có icon này trong drawable
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Chỉ hiện nút gọi điện khi bị ngã
        if (isFall) {
            // Đọc số điện thoại từ SharedPreferences
            val prefs = ctx.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val emergencyPhone = prefs.getString("emergency_phone", EMERGENCY_PHONE) ?: EMERGENCY_PHONE
            
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$emergencyPhone")
            }

            val callPendingIntent = PendingIntent.getActivity(
                ctx,
                1,
                callIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Lưu ý: Icon R.drawable.ic_call phải tồn tại, nếu chưa có hãy tạo hoặc thay bằng icon khác
            builder.addAction(
                android.R.drawable.ic_menu_call,
                "GỌI CẤP CỨU",
                callPendingIntent
            )
        }

        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    companion object {
        private const val CHANNEL_ID = "fall_channel"
        private const val EMERGENCY_PHONE = "0334679392"
    }
}