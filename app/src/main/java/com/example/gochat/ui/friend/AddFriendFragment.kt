package com.example.gochat.ui.friend

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gochat.R
import com.example.gochat.data.ApiService
import com.example.gochat.data.database.dao.FriendDao
import com.example.gochat.data.database.entity.Friend
import com.example.gochat.data.database.entity.User
import com.example.gochat.utils.TokenManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import retrofit2.Response

class AddFriendFragment : Fragment() {

    private val apiService: ApiService by inject()
    private val friendDao: FriendDao by inject()
    private var ownerId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ownerId = TokenManager.getUserId(requireContext())
        showAddFriendDialog()
        return null
    }

    private fun showAddFriendDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("添加好友")

        val input = EditText(requireContext())
        input.hint = "请输入好友 ID"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("确认") { _, _ ->
            val friendId = input.text.toString().toIntOrNull()
            if (friendId != null) {
                lifecycleScope.launch {
                    addFriend(friendId)
                }
            } else {
                Toast.makeText(requireContext(), "请输入有效的 ID", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("取消") { dialog, _ ->
            dialog.dismiss()
            parentFragmentManager.popBackStack()
        }

        builder.setCancelable(true)
        builder.show()
    }

    private suspend fun addFriend(friendId: Int) {
        val token = TokenManager.getAccessToken(requireContext())
        if (token == null) {
            Toast.makeText(requireContext(), "未登录", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val response: Response<Friend> = apiService.addFriend("Bearer $token", friendId)
            if (response.isSuccessful) {
                val friend = response.body()
                if (friend != null) {
                    val newFriend = Friend(
                        id = friend.id,
                        username = friend.username,
                        email = friend.email,
                        status = friend.status,
                        avatarUrl = friend.avatarUrl,
                        ownerId = ownerId
                    )
                    friendDao.insertFriends(listOf(newFriend))
                    Toast.makeText(requireContext(), "添加好友成功: ${friend.username}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "添加失败: 好友信息为空", Toast.LENGTH_SHORT).show()
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "未知错误"
                Toast.makeText(requireContext(), "添加失败: $errorBody", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "网络错误，请检查连接", Toast.LENGTH_SHORT).show()
        } finally {
            parentFragmentManager.popBackStack()
        }
    }
}