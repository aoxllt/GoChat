package com.example.gochat.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.gochat.R
import com.example.gochat.data.ApiService
import com.example.gochat.data.database.dao.ChatDao
import com.example.gochat.data.database.dao.UserInfoDao
import com.example.gochat.data.database.entity.Chat
import com.example.gochat.data.database.entity.User
import com.example.gochat.databinding.FragmentChatListBinding
import com.example.gochat.utils.TokenManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import retrofit2.Response

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    private val apiService: ApiService by inject()
    private val userInfoDao: UserInfoDao by inject()
    private val chatDao: ChatDao by inject()
    private lateinit var chatAdapter: ChatAdapter
    private val chatList = mutableListOf<Chat>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setupRecyclerView()
        fetchChats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    private fun setupRecyclerView() {
//        chatAdapter = ChatAdapter(chatList) { chat ->
//            val action = ChatListFragmentDirections.actionChatListFragmentToChatDetailFragment(chat.id)
//            findNavController().navigate(action)
//        }
//        binding.chatListRecyclerView.apply {
//            layoutManager = LinearLayoutManager(context)
//            adapter = chatAdapter
//            chatAdapter.notifyDataSetChanged()
//        }
//    }

    private fun fetchChats() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext())
            val userId = TokenManager.getUserId(requireContext())

            if (token == null || userId == 0) {
                Toast.makeText(requireContext(), "加载聊天失败：未登录", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                return@launch
            }

            try {
                val cachedChats = chatDao.getChatsByUserId(userId)
                if (cachedChats.isNotEmpty()) {
                    chatList.clear()
                    chatList.addAll(cachedChats)
                    chatAdapter.notifyDataSetChanged()
                } else {
                    chatAdapter.notifyDataSetChanged()
                }

                val response = apiService.getChatList("Bearer $token")
                if (response.isSuccessful) {
                    val chatsFromApi = response.body() ?: emptyList()
                    val chats = chatsFromApi.map { chat ->
                        Chat(
                            id = chat.id,
                            userId = userId,
                            friendId = chat.friendId,
                            username = chat.username,
                            lastMessage = chat.lastMessage,
                            timestamp = chat.timestamp,
                            avatarUrl = chat.avatarUrl
                        )
                    }

                    chatDao.deleteChatsByUserId(userId)
                    chatDao.insertChats(chats)

                    chatList.clear()
                    chatList.addAll(chats)
                    chatAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "加载聊天失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "网络错误，请检查连接", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}

class ChatAdapter(
    private val chatList: List<Chat>,
    private val onItemClick: (Chat) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.bind(chat)
        holder.itemView.setOnClickListener { onItemClick(chat) }
    }

    override fun getItemCount(): Int = chatList.size

    inner class ChatViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImage)
        private val usernameTextView: TextView = itemView.findViewById(R.id.textChatName)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.textLastMessage)
        private val timestampTextView: TextView = itemView.findViewById(R.id.textLastMessageTime)

        fun bind(chat: Chat) {
            usernameTextView.text = chat.username
            lastMessageTextView.text = chat.lastMessage ?: "无消息"
            timestampTextView.text = chat.timestamp?.let { formatTimestamp(it) } ?: "未知时间"

            Glide.with(itemView.context)
                .load(chat.avatarUrl ?: R.drawable.ic_default_avatar)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(avatarImageView)
        }

        private fun formatTimestamp(timestamp: Long): String {
            return android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", timestamp).toString()
        }
    }
}