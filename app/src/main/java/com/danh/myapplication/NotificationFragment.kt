package com.danh.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
}