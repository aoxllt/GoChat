package com.example.gochat.ui.chat

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.gochat.R
import kotlin.Int

class ChatListFragmentDirections private constructor() {
    companion object {
        fun actionChatListFragmentToChatFragment(friendId: Int = 0): NavDirections =
            ActionOnlyNavDirections(R.id.action_chatListFragment_to_chatFragment).apply {
                arguments.putInt("friendId", friendId)
            }
    }
}