package com.example.realtimeserivce.ui.main

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realtimeserivce.R
import com.example.realtimeserivce.adapter.PrivateChatAdapter
import com.example.realtimeserivce.databinding.FragmentMessageBinding
import com.example.realtimeserivce.viewmodel.PrivateChatViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MessageFragment : Fragment() {
    private lateinit var fragmentMessageBinding: FragmentMessageBinding
    private val auth = Firebase.auth
    private val viewModel: PrivateChatViewModel by viewModels()
    private val messageAdapter = PrivateChatAdapter(mutableListOf())
    private val args: MessageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentMessageBinding = FragmentMessageBinding.inflate(layoutInflater, container, false)
        val layoutManager = LinearLayoutManager(context)

        fragmentMessageBinding.messageRecycler.adapter = messageAdapter
        fragmentMessageBinding.messageRecycler.layoutManager = layoutManager

        fragmentMessageBinding.inputLayout.setEndIconOnClickListener {
            // text가 비어있으면, inputlayout의 에러를 유발시키고 문자열이 비어있지 않으면 송신한다.
            val text = fragmentMessageBinding.etMessage.text.toString()
            if (text.isEmpty()) {
                fragmentMessageBinding.inputLayout.error = "message is empty"
            } else {
                viewModel.sendMessage(text, getChatroomId())
                fragmentMessageBinding.etMessage.setText("")
                hideKeyboard(it)
            }
        }
        // text가 다시 입력될 때 inputlayout의 error를 없애준다.
        fragmentMessageBinding.etMessage.addTextChangedListener {
            fragmentMessageBinding.inputLayout.error = null
        }

        viewModel.message.observe(viewLifecycleOwner) {
            // fetchMessage 동작 시, recycler view 는 position을 최하단으로 맞춰준다.
            messageAdapter.fetchMessage(it)
            fragmentMessageBinding.messageRecycler.scrollToPosition(it.size - 1)
        }

        return fragmentMessageBinding.root
    }

    override fun onResume() {
        super.onResume()
        // message data의 변화를 지속적으로 감지하며 변경사항이 있으면 message livedata에 전달해 recyclerview를 통해 호출한다.
        viewModel.messageListChange(getChatroomId())
    }

    private fun getChatroomId(): String {
        // safeargs를 통해 클릭 한 상대방 아이콘의 uid를 가져온다.
        // 상대방도 내 uid가 채팅방 이름에 포함되면 화면에 갱신되도록 설정
        val targetUid = args.uid
        val myUid = auth.uid!!

        // 각각 다른 이름의 chatroomId가 생성되기에 크기 비교를 통해서 같은 사람의 대화는 항상 같은 chatroomId가 형성되도록 설정해준다.
        return if (targetUid < myUid) targetUid+myUid else myUid+targetUid
    }

    // 키보드를 내려주는 method
    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}