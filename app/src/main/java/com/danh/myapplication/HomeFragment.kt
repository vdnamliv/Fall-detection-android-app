package com.danh.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.danh.myapplication.databinding.FragmentHomeBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class HomeFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var binding: FragmentHomeBinding
    private var firstLoad = true   // Biến để bỏ qua dữ liệu cũ lúc mới mở app

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
        listenLatestEvent()
        return binding.root
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
                        // 3. SỬA LỖI QUAN TRỌNG: Lấy dữ liệu thủ công để tránh lỗi Type Mismatch
                        // Dù server gửi số hay chữ, .toString() đều xử lý được hết
                        val type = snapshot.child("type").value?.toString() ?: ""
                        val imageUrl = snapshot.child("imageUrl").value?.toString() ?: ""
                        // val timestamp = snapshot.child("timestamp").value?.toString() ?: "" // Nếu cần dùng timestamp

                        Log.d("FIREBASE_DATA", "Type: $type")

                        // 4. Cập nhật giao diện
                        val text = if (type == "fall") "CẢNH BÁO: CÓ NGƯỜI NGÃ!"
                        else "TRẠNG THÁI: ${type.uppercase()}"

                        binding.textView.text = text

                        // 5. Xử lý ảnh (Decode Base64)
                        if (imageUrl.isNotEmpty()) {
                            val bitmap = decodeBase64ToBitmap(imageUrl)
                            if (bitmap != null) {
                                binding.imageView.setImageBitmap(bitmap)
                            } else {
                                // Nếu ảnh lỗi thì set ảnh mặc định (nếu có)
                                // binding.imageView.setImageResource(R.drawable.ic_launcher_background)
                            }
                        }

                        // 6. Hiện thông báo
                        showNotification(type)

                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Lỗi xử lý data: ${e.message}")
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun decodeBase64ToBitmap(base64String: String?): Bitmap? {
        if (base64String.isNullOrEmpty()) return null
        return try {
            // Thêm cờ NO_WRAP để tránh lỗi xuống dòng trong chuỗi Base64
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
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$EMERGENCY_PHONE")
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