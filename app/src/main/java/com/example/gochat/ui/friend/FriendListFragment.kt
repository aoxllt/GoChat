package com.example.gochat.ui.friend

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
import com.example.gochat.data.database.dao.FriendDao
import com.example.gochat.data.database.dao.UserInfoDao
import com.example.gochat.data.database.entity.Friend
import com.example.gochat.data.database.entity.User
import com.example.gochat.data.database.entity.enums.UserStatus
import com.example.gochat.databinding.FragmentFriendListBinding
import com.example.gochat.utils.TokenManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import retrofit2.Response

class FriendListFragment : Fragment() {

    private var _binding: FragmentFriendListBinding? = null
    private val binding get() = _binding!!
    private val apiService: ApiService by inject()
    private val userInfoDao: UserInfoDao by inject()
    private val friendDao: FriendDao by inject()
    private lateinit var friendAdapter: FriendAdapter
    private val friendList = mutableListOf<Friend>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchFriends()

        // 设置“添加好友”按钮的点击事件
        binding.addFriendButton.setOnClickListener {
            findNavController().navigate(R.id.action_friendListFragment_to_addFriendFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        friendAdapter = FriendAdapter(friendList) { friend ->
            Toast.makeText(requireContext(), "点击了 ${friend.username}", Toast.LENGTH_SHORT).show()
        }
        binding.friendListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendAdapter
            friendAdapter.notifyDataSetChanged()
        }
    }

    private fun fetchFriends() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext())
            val userId = TokenManager.getUserId(requireContext())

            if (token == null || userId == 0) {
                Toast.makeText(requireContext(), "加载好友失败：未登录", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                return@launch
            }

            try {
                val cachedFriends = friendDao.getFriendsByOwnerId(userId)
                if (cachedFriends.isNotEmpty()) {
                    friendList.clear()
                    friendList.addAll(cachedFriends)
                    friendAdapter.notifyDataSetChanged()
                } else {
                    friendAdapter.notifyDataSetChanged()
                }

                val response = apiService.getFriendList("Bearer $token")
                if (response.isSuccessful) {
                    val friendsFromApi = response.body() ?: emptyList()
                    val friends = friendsFromApi.map { user ->
                        Friend(
                            id = user.id,
                            username = user.username,
                            email = user.email,
                            status = user.status,
                            avatarUrl = user.avatarUrl,
                            ownerId = userId
                        )
                    }

                    friendDao.deleteFriendsByOwnerId(userId)
                    friendDao.insertFriends(friends)

                    friendList.clear()
                    friendList.addAll(friends)
                    friendAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "加载好友失败", Toast.LENGTH_SHORT).show()
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

class FriendAdapter(
    private val friendList: List<Friend>,
    private val onItemClick: (Friend) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendList[position]
        holder.bind(friend)
        holder.itemView.setOnClickListener { onItemClick(friend) }
    }

    override fun getItemCount(): Int = friendList.size

    inner class FriendViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val avatarImageView = itemView.findViewById<ImageView>(R.id.friendAvatarImageView)
        private val usernameTextView = itemView.findViewById<TextView>(R.id.friendUsernameTextView)
        private val statusTextView = itemView.findViewById<TextView>(R.id.friendStatusTextView)
        private val statusIcon = itemView.findViewById<ImageView>(R.id.friendStatusIcon)

        fun bind(friend: Friend) {
            usernameTextView.text = friend.username
            statusTextView.text = if (friend.status == UserStatus.ACTIVE) "在线" else "离线"

            Glide.with(itemView.context)
                .load(friend.avatarUrl ?: R.drawable.ic_default_avatar)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(avatarImageView)

            statusIcon.setImageResource(
                if (friend.status == UserStatus.ACTIVE) R.drawable.ic_status_online else R.drawable.ic_status_offline
            )
        }
    }
}