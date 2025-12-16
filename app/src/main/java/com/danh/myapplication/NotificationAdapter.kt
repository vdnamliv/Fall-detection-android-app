package com.danh.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.danh.myapplication.databinding.ItemNotifationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(private val notifications: MutableList<Notification>,private val listener: OnItemClick): RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.ViewHolder {
        val binding= ItemNotifationBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationAdapter.ViewHolder, position: Int) {
       holder.bind(notifications[position])
    }

    override fun getItemCount(): Int {
        return notifications.size
    }
    inner class ViewHolder(val binding: ItemNotifationBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(notification: Notification){
            // Format timestamp đẹp
            val rawTime = notification.time
            try {
                var timeMillis = rawTime.toLong()
                // Nếu là giây (10 số) thì nhân 1000 để thành mili-giây
                if (rawTime.length <= 10) {
                    timeMillis *= 1000
                }
                val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
                val dateString = sdf.format(Date(timeMillis))
                binding.tvTime.text = dateString
            } catch (e: Exception) {
                binding.tvTime.text = rawTime // Nếu lỗi thì hiện số cũ
            }
            
            binding.tvTitle.text = notification.title
            
            // Đổi màu theo type
            val isFall = notification.type == "fall"
            if (isFall) {
                // Màu đỏ cho fall
                binding.tvTitle.setTextColor(Color.parseColor("#D32F2F"))
                binding.tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
                binding.iconStatus.setColorFilter(Color.parseColor("#D32F2F"))
            } else {
                // Màu xanh lá cho normal
                binding.tvTitle.setTextColor(Color.parseColor("#388E3C"))
                binding.tvTitle.setTypeface(null, android.graphics.Typeface.NORMAL)
                binding.iconStatus.setColorFilter(Color.parseColor("#388E3C"))
            }
            
            // Hiển thị thumbnail ảnh
            if (notification.image.isNotEmpty()) {
                val bitmap = decodeBase64ToBitmap(notification.image)
                if (bitmap != null) {
                    binding.imgThumbnail.setImageBitmap(bitmap)
                } else {
                    binding.imgThumbnail.setImageResource(R.drawable.ic_notification)
                }
            } else {
                binding.imgThumbnail.setImageResource(R.drawable.ic_notification)
            }
            
            binding.root.setOnClickListener {
                listener.clickNotification()
            }
        }
        
        private fun decodeBase64ToBitmap(base64String: String?): Bitmap? {
            if (base64String.isNullOrEmpty()) return null
            return try {
                val bytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    interface OnItemClick{
        fun clickNotification()
    }
}