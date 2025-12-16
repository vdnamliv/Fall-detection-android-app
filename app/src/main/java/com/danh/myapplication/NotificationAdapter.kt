package com.danh.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.danh.myapplication.databinding.ItemNotifationBinding

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
    inner class ViewHolder(private val binding: ItemNotifationBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(notification: Notification){
            binding.tvTime.text=notification.time
            binding.tvTitle.text=notification.title
            binding.root.setOnClickListener {
                listener.clickNotification()
            }
        }
    }
    interface OnItemClick{
        fun clickNotification()
    }
}