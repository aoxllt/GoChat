package com.example.gochat.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gochat.R
import com.example.gochat.api.Contact

class ContactAdapter(private val contactList: List<Contact>) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.contact_avatar)
        val name: TextView = itemView.findViewById(R.id.contact_name)
        val tag: TextView = itemView.findViewById(R.id.contact_tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.avatar.setImageResource(contact.avatarResId)
        holder.name.text = contact.name
        holder.tag.text = contact.tag
    }

    override fun getItemCount(): Int = contactList.size
}