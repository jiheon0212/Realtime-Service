package com.example.realtimeserivce.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realtimeserivce.adapter.ChatroomAdapter
import com.example.realtimeserivce.databinding.FragmentChatroomBinding
import com.example.realtimeserivce.viewmodel.ChatroomViewModel

class ChatroomFragment : Fragment() {
    private lateinit var fragmentChatroomBinding: FragmentChatroomBinding
    private val viewModel: ChatroomViewModel by viewModels()
    private lateinit var chatroomAdapter: ChatroomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentChatroomBinding = FragmentChatroomBinding.inflate(layoutInflater, container, false)
        chatroomAdapter = ChatroomAdapter { uid ->
            // adapter를 통해 전달된 uid로 해당하는 user와 메세지 창을 띄워준다.
            val action = ChatroomFragmentDirections.actionChatroomFragmentToMessageFragment(uid)
            findNavController().navigate(action)
        }

        // roomId에 내 uid가 포함된 data가 database에 추가되는지 감지한다.
        // 감지된 data가 존재한다면 recyclerview adapter에 적용시켜주며 view에 비동기로 호출한다.
        viewModel.roomId.observe(viewLifecycleOwner) {
            chatroomAdapter.fetchChatroom(it)
            fragmentChatroomBinding.chatRoomRecycler.scrollToPosition(it.size - 1)
        }

        fragmentChatroomBinding.chatRoomRecycler.run {
            adapter = chatroomAdapter
            layoutManager = LinearLayoutManager(context)
        }
        return fragmentChatroomBinding.root
    }
}