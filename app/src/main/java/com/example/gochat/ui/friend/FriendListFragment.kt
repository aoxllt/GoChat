//package com.example.gochat.ui.friend
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.gochat.R
//import com.example.gochat.databinding.FragmentFriendListBinding
//import com.example.gochat.viewmodel.FriendListViewModel
//import org.koin.androidx.viewmodel.ext.android.viewModel
//
//class FriendListFragment : Fragment() {
//
//    private val viewModel: FriendListViewModel by viewModel()
//    private lateinit var binding: FragmentFriendListBinding
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentFriendListBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val adapter = FriendListAdapter { friend ->
//            val bundle = Bundle().apply {
//                putLong("friendId", friend.id)
//            }
//            findNavController().navigate(R.id.action_friendList_to_chat, bundle)
//        }
//        binding.recyclerViewFriends.apply {
//            layoutManager = LinearLayoutManager(context)
//            this.adapter = adapter
//        }
//
//        viewModel.friendList.observe(viewLifecycleOwner) { friends ->
//            adapter.submitList(friends)
//        }
//
//        viewModel.loadFriends()
//    }
//}