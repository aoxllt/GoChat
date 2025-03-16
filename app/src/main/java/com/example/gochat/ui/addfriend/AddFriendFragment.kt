package com.example.gochat.ui.addfriend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gochat.databinding.FragmentAddFriendBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddFriendFragment : Fragment() {

    private val viewModel: AddFriendViewModel by viewModel() // 使用 Koin 的 viewModel
    private lateinit var binding: FragmentAddFriendBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SearchResultAdapter { friend ->
            viewModel.sendFriendRequest(friend.id)
            Toast.makeText(context, "已发送好友请求", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerViewSearchResults.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        binding.buttonSearch.setOnClickListener {
            val username = binding.editTextSearch.text.toString()
            if (username.isNotBlank()) {
                viewModel.searchUser(username)
            } else {
                Toast.makeText(context, "请输入用户名", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            adapter.submitList(results)
        }
    }
}