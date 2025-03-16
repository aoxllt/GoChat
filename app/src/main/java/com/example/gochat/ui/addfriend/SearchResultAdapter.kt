package com.example.gochat.ui.addfriend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gochat.databinding.ItemSearchResultBinding
import com.example.gochat.api.Friend

class SearchResultAdapter(private val onAddClick: (Friend) -> Unit) :
    ListAdapter<Friend, SearchResultAdapter.SearchResultViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val friend = getItem(position)
        holder.bind(friend)
    }

    inner class SearchResultViewHolder(private val binding: ItemSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: Friend) {
            binding.textNickname.text = friend.nickname
            binding.textUsername.text = friend.username
            binding.buttonAddFriend.setOnClickListener { onAddClick(friend) }
        }
    }
}

class FriendDiffCallback : DiffUtil.ItemCallback<Friend>() {
    override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean = oldItem == newItem
}