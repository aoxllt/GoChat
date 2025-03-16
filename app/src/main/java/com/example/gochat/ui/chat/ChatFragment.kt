//package com.example.gochat.ui.chat
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.gochat.databinding.FragmentChatBinding
//import com.example.gochat.viewmodel.ChatViewModel
//import org.koin.androidx.viewmodel.ext.android.viewModel
//import org.koin.core.parameter.parametersOf
//
//class ChatFragment : Fragment() {
//
//    private val viewModel: ChatViewModel by viewModel { parametersOf(getFriendId()) }
//    private lateinit var binding: FragmentChatBinding
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentChatBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val adapter = MessageAdapter(getCurrentUserId())
//        binding.recyclerViewMessages.apply {
//            layoutManager = LinearLayoutManager(context).apply {
//                stackFromEnd = true // 从底部开始显示
//            }
//            this.adapter = adapter
//        }
//
//        binding.buttonSend.setOnClickListener {
//            val content = binding.editTextMessage.text.toString()
//            if (content.isNotBlank()) {
//                viewModel.sendMessage(getFriendId(), content)
//                binding.editTextMessage.text.clear()
//            } else {
//                Toast.makeText(context, "请输入消息内容", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        viewModel.messages.observe(viewLifecycleOwner) { messages ->
//            adapter.submitList(messages) {
//                binding.recyclerViewMessages.scrollToPosition(messages.size - 1) // 滚动到底部
//            }
//        }
//
//        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
//            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
//        }
//
//        viewModel.loadMessages(getFriendId())
//        viewModel.startWebSocket(getFriendId())
//    }
//
//    private fun getFriendId(): Long {
//        return arguments?.getLong("friendId") ?: throw IllegalArgumentException("Missing friendId")
//    }
//
//    private fun getCurrentUserId(): Long {
//        val prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
//        return prefs.getLong("current_user_id", 1L) // 默认值为 1，实际应从登录状态获取
//    }
//}