package com.harshRajpurohit.musicPlayer.dialogFlow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.harshRajpurohit.musicPlayer.databinding.AdapterMessageOneBinding

class ChatAdapter(private val messageList: List<Message>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(private val binding: AdapterMessageOneBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            if (message.isReceived) {
                binding.messageReceive.apply {
                    visibility = android.view.View.VISIBLE
                    text = message.message
                }
                binding.messageSend.visibility = android.view.View.GONE
            } else {
                binding.messageSend.apply {
                    visibility = android.view.View.VISIBLE
                    text = message.message
                }
                binding.messageReceive.visibility = android.view.View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = AdapterMessageOneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messageList[position])
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}