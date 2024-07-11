package com.example.realtimeserivce.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.realtimeserivce.data.Message
import com.example.realtimeserivce.databinding.PrivateChatReceiverBinding
import com.example.realtimeserivce.databinding.PrivateChatSenderBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class PrivateChatAdapter(private var messages: MutableList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_SEND = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val auth = Firebase.auth

    @SuppressLint("NotifyDataSetChanged")
    fun fetchMessage(messageList: MutableList<Message>) {
        messages = messageList
        notifyDataSetChanged()
    }

    inner class SenderViewHolder(val binding: PrivateChatSenderBinding): RecyclerView.ViewHolder(binding.root)
    inner class ReceiverViewHolder(val binding: PrivateChatReceiverBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val sender = PrivateChatSenderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val receiver = PrivateChatReceiverBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return if (viewType == VIEW_TYPE_SEND) {
            SenderViewHolder(sender)
        } else {
            ReceiverViewHolder(receiver)
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messageData = messages[position]
        val (name, message, timestamp) = messageData

        if (holder.itemViewType == VIEW_TYPE_SEND){
            (holder as SenderViewHolder).binding.apply {
                tvUser.text = name
                tvMessage.text = message
                tvCurrentTime.text = timestamp
            }
        } else {
            (holder as ReceiverViewHolder).binding.apply {
                tvUser.text = name
                tvMessage.text = message
                tvCurrentTime.text = timestamp
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        // messageData 객체에 담긴 uid를 통해 sender인지 recevier인지 구분한다.
        return if (messages[position].name == auth.uid) VIEW_TYPE_SEND else VIEW_TYPE_RECEIVED
    }
    override fun getItemCount(): Int = messages.size
}