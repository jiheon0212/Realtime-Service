package com.example.realtimeserivce.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realtimeserivce.adapter.StatusAdapter
import com.example.realtimeserivce.databinding.FragmentMatchWaitBinding
import com.example.realtimeserivce.viewmodel.MatchViewModel
import com.example.realtimeserivce.viewmodel.WaitViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class MatchWaitFragment : Fragment() {
    private lateinit var fragmentMatchWaitBinding: FragmentMatchWaitBinding
    private val auth = Firebase.auth
    private val waitModel: WaitViewModel by viewModels()
    private lateinit var statusAdapter: StatusAdapter
    private val onlinePlayers = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentMatchWaitBinding = FragmentMatchWaitBinding.inflate(layoutInflater, container, false)
        statusAdapter = StatusAdapter(mutableListOf()){}
        waitModel.playerStatus.observe(viewLifecycleOwner) { status ->
            statusAdapter.updateCurrentUserList(status)

            // 2명 이상일 경우 전체 리스트를 받아올 때까지 기다린 후에 searchMatch()를 실행시키고 리스트를 초기화하는 방식 필요
            // 비동기 실행이므로 순차성을 보장하기위해 coroutine으로 작업한다.
            CoroutineScope(Dispatchers.Main).launch {
                status.forEach {
                    if (it.status == "online") {
                        onlinePlayers.add(it.user!!)
                    }
                }
                // 변경된 각각의 데이터에 대해 online인 유저가 2명 이상일 때 searchMatch 메서드가 동작하도록 구현
                if (onlinePlayers.size >= 2) {
                    searchMatch(onlinePlayers)
                }
                // onlinePlayers를 랜덤에 전달한 후에 초기화 시켜준다
                onlinePlayers.clear()
            }
        }

        fragmentMatchWaitBinding.matchWaitRecycler.adapter = statusAdapter
        fragmentMatchWaitBinding.matchWaitRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        matchToggle()
        
        // 매치 대기 상테에서만 방에 입장할 수 있도록 구현
        if (fragmentMatchWaitBinding.btnMatchWait.text == "Match Searching...") {
            // 현재 사용자의 uid가 포함된 방이 개설되면 해당 항목을 통해 onMatchFound 메서드를 작동시킨다.
            waitModel.matchRoomIds.observe(viewLifecycleOwner) {
                it.forEach { matchId ->
                    if (matchId.contains(auth.uid!!)) {
                        onMatchFound(matchId)
                    }
                }
            }
        }
        
        return fragmentMatchWaitBinding.root
    }
    
    // 매치 대기열 Status를 변경하는 메서드 -> button을 통해 toggle 활성화/비활성화
    private fun matchToggle() {
        fragmentMatchWaitBinding.btnMatchWait.setOnClickListener { 
            val type = fragmentMatchWaitBinding.btnMatchWait.text.toString()
            if (type == "Match Ready") {
                waitModel.setReadyStatus(true)
                fragmentMatchWaitBinding.btnMatchWait.text = "Match Searching..."
            } else {
                fragmentMatchWaitBinding.btnMatchWait.text = "Match Ready"
                waitModel.setReadyStatus(false)
            }
        }
    }

    // 매칭 후 matchId를 반환해주는 메서드
    private fun searchMatch(onlinePlayers: MutableList<String>) {
        val randomPlayerPair = mutableListOf<String>()

        repeat(2) {
            val randomIndex = Random.nextInt(onlinePlayers.size)
            randomPlayerPair.add(onlinePlayers[randomIndex])
            onlinePlayers.removeAt(randomIndex)
        }
        val matchRoomId = randomPlayerPair.sortedDescending().joinToString(separator = ",")
        onMatchFound(matchRoomId)
    }
    // 매칭 성사 시, 매치 화면을 호출하는 메서드
    // matchId를 실시간으로 감지하여 이에 사용자의 uid가 포함된다면 그때도 onMatchFound를 동작 시켜줘야한다.
    private fun onMatchFound(id: String) {
        // matchroom id 넘겨주기
        val action = MatchWaitFragmentDirections.actionMatchWaitFragmentToMatchFragment(id)
        findNavController().navigate(action)
    }

    override fun onStop() {
        super.onStop()
        fragmentMatchWaitBinding.btnMatchWait.text = "Match Ready"
        waitModel.setReadyStatus(false)
    }
}