package com.danh.myapplication

import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.danh.myapplication.databinding.FragmentNotificationBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var binding: FragmentNotificationBinding
    private lateinit var adapter: NotificationAdapter
    private val notifications = mutableListOf<Notification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kết nối vào node data1
        database = FirebaseDatabase.getInstance().getReference("data1")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo Adapter
        adapter = NotificationAdapter(notifications, object : NotificationAdapter.OnItemClick {
            override fun clickNotification() {
                // Xử lý sự kiện click nếu cần
            }

            override fun clickImage(imageBase64: String) {
                // Hiển thị ảnh full screen
                showImageDialog(imageBase64)
            }
        })

        binding.rcy.layoutManager = LinearLayoutManager(requireContext())
        binding.rcy.adapter = adapter

        loadAllNotifications()
    }

    private fun loadAllNotifications() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Xóa dữ liệu cũ để tránh duplicate
                notifications.clear()

                for (child in snapshot.children) {
                    try {
                        // === SỬA LỖI TẠI ĐÂY: LẤY DỮ LIỆU THỦ CÔNG ===
                        // Thay vì dùng getValue(DataEvent::class.java), ta lấy từng trường và ép về String

                        val type = child.child("type").value?.toString() ?: ""
                        val timestamp = child.child("timestamp").value?.toString() ?: ""
                        val imageUrl = child.child("imageUrl").value?.toString() ?: ""

                        // Tạo title tuỳ theo type
                        val title = if (type == "fall") {
                            "CẢNH BÁO: CÓ NGƯỜI NGÃ"
                        } else {
                            "Phát hiện CÓ NGƯỜI ($type)"
                        }

                        // Thêm vào list hiển thị
                        notifications.add(
                            Notification(
                                time = timestamp, // Dù timestamp là số hay chữ đều hiển thị được
                                title = title,
                                image = imageUrl,
                                type = type // Thêm type để adapter đổi màu
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("NotificationFragment", "Lỗi data node: ${e.message}")
                        // Nếu 1 dòng lỗi, bỏ qua dòng đó và chạy tiếp dòng sau, không crash app
                        continue
                    }
                }

                // Đảo ngược danh sách để tin mới nhất lên đầu
                notifications.reverse()

                // Cập nhật giao diện
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationFragment", "Lỗi tải data: ${error.message}")
            }
        })
    }

    private fun showImageDialog(imageBase64: String) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_viewer)

        val imgFull = dialog.findViewById<ImageView>(R.id.img_full)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btn_close)

        // Decode Base64 và hiển thị ảnh
        try {
            val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imgFull.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Log.e("ImageDialog", "Lỗi decode ảnh: ${e.message}")
        }

        // Đóng dialog khi click nút close hoặc click vào ảnh
        btnClose.setOnClickListener { dialog.dismiss() }
        imgFull.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}