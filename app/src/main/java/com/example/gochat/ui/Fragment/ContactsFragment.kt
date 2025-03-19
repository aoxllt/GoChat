package com.example.gochat.ui.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gochat.R
import com.example.gochat.api.Contact
import com.example.gochat.utils.ContactAdapter

class ContactsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_contacts)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // 模拟数据
        val contactList = listOf(
            Contact(R.drawable.gopher, "张三", "好友"),
            Contact(R.drawable.gopher, "李四", "同事"),
            Contact(R.drawable.gopher, "王五", "家人")
        )

        recyclerView.adapter = ContactAdapter(contactList)
    }
}