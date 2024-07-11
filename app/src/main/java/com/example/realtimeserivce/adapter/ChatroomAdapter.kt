package com.example.realtimeserivce.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.realtimeserivce.data.ChatroomId
import com.example.realtimeserivce.databinding.ChatroomRecyclerBinding

class ChatroomAdapter(private val onItemClick: (String) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class ChatroomViewHolder(val binding: ChatroomRecyclerBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val uid = chatroomList[adapterPosition].visibleName
                    onItemClick(uid)
                }
            }
        }
    }
    var chatroomList = mutableListOf<ChatroomId>()

    // livedata를 통해 전달받은 chatroomId로 adapter를 갱신시켜준다.
    @SuppressLint("NotifyDataSetChanged")
    fun fetchChatroom(chatroomList: MutableList<ChatroomId>) {
        this.chatroomList = chatroomList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ChatroomRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatroomViewHolder(binding)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val roomName = chatroomList[position].visibleName
        (holder as ChatroomViewHolder).binding.tvVisibleName.text = roomName
    }
    override fun getItemCount(): Int = chatroomList.size
}