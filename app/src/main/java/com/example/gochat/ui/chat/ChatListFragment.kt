//package com.example.gochat.ui.chat
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.fragment.findNavController
//import com.example.gochat.api.MessageApi
//import com.example.gochat.api.ChatSession
//import com.example.gochat.databinding.FragmentChatListBinding
//import com.example.gochat.utils.TokenManager
//import kotlinx.coroutines.launch
//import org.koin.android.ext.android.inject
//
//
//class ChatListFragment : Fragment() {
//
//    private var _binding: FragmentChatListBinding? = null
//    private val binding get() = _binding!!
//    private val messageApi: MessageApi by inject()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentChatListBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        fetchChatList()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    private fun fetchChatList() {
//        lifecycleScope.launch {
//            try {
//                val accessToken = TokenManager.getAccessToken(requireContext())
//                if (accessToken != null) {
//                    val response = messageApi.getChatSessions("Bearer $accessToken")
//                    if (response.isSuccessful) {
//                        val chatSessions = response.body() ?: emptyList()
//                        setupRecyclerView(chatSessions)
//                    } else {
//                        println("Failed to fetch chat list: ${response.code()}")
//                    }
//                } else {
//                    println("Access token is null")
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                println("Error fetching chat list: ${e.message}")
//            }
//        }
//    }
//
//    private fun setupRecyclerView(chatSessions: List<ChatSession>) {
//        val adapter = ChatAdapter(chatSessions) { chatSession ->
//            val action = ChatListFragmentDirections.actionChatListFragmentToChatFragment(chatSession.friendId)
//            findNavController().navigate(action)
//        }
//        binding.recyclerViewChats.adapter = adapter
//    }
//}