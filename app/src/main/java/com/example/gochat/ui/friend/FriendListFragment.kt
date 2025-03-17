package com.example.gochat.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gochat.databinding.FragmentFriendListBinding

class FriendListFragment : Fragment() {

    private var _binding: FragmentFriendListBinding? = null
    private val binding get() = _binding!!

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
        // 初始化代码（如果需要）
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}