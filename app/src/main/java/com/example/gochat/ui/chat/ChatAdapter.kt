package com.example.gochat.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gochat.R
import com.example.gochat.api.ChatSession
import com.example.gochat.databinding.ItemChatBinding

class ChatAdapter(
    private val chatList: List<ChatSession>,
    private val onItemClick: (ChatSession) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount(): Int = chatList.size

    inner class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatSession: ChatSession) {
            binding.textChatName.text = chatSession.friendName
            binding.textLastMessage.text = chatSession.lastMessage
            binding.textLastMessageTime.text = chatSession.lastMessageTime

            if (chatSession.friendAvatarUrl != null) {
                Glide.with(binding.root.context)
                    .load(chatSession.friendAvatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(binding.avatarImage)
            } else {
                binding.avatarImage.setImageResource(R.drawable.ic_default_avatar)
            }

            binding.root.setOnClickListener {
                onItemClick(chatSession)
            }
        }
    }
}