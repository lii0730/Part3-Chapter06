package com.example.aop_part3_chapter06.ChatDetail

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aop_part3_chapter06.R
import com.example.aop_part3_chapter06.databinding.ItemChatBinding
import com.google.firebase.auth.FirebaseAuth

class ChatItemAdapter(val activity: Activity) :
    ListAdapter<ChatItem, ChatItemAdapter.ViewHolder>(diffUtil) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    inner class ViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatItem: ChatItem) {
            binding.senderTextView.text = chatItem.senderId
            binding.messageTextView.text = chatItem.message

            if (chatItem.senderId == auth.currentUser?.uid) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    binding.senderTextView.setTextColor(
                        activity.resources.getColor(
                            R.color.MyTextcolor,
                            null
                        )
                    )
                }
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    binding.senderTextView.setTextColor(
                        activity.resources.getColor(
                            R.color.otherTextColor,
                            null
                        )
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ChatItem>() {
            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }
            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}