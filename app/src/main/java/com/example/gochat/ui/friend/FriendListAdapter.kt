package com.example.gochat.ui.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gochat.api.Friend
import com.example.gochat.databinding.ItemFriendBinding

class FriendListAdapter(private val onClick: (Friend) -> Unit) :
    ListAdapter<Friend, FriendListAdapter.FriendViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = getItem(position)
        holder.bind(friend)
    }

    inner class FriendViewHolder(private val binding: ItemFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: Friend) {
            binding.textNickname.text = friend.nickname
            binding.textUsername.text = friend.username
            // 使用 Glide 加载头像（如果有）
            friend.avatarUrl?.let { url ->
                Glide.with(itemView)
                    .load(url)
                    .into(binding.imageAvatar)
            }
            itemView.setOnClickListener { onClick(friend) }
        }
    }
}

class FriendDiffCallback : DiffUtil.ItemCallback<Friend>() {
    override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean = oldItem == newItem
}