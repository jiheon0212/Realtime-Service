package com.example.realtimeserivce.ui.match

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.NavHostFragment
import com.example.realtimeserivce.R
import com.example.realtimeserivce.databinding.FragmentMatchBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MatchFragment : Fragment() {
    private lateinit var fragmentMatchBinding: FragmentMatchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentMatchBinding = FragmentMatchBinding.inflate(layoutInflater, container, false)
        return fragmentMatchBinding.root
    }
    
    // 타이머 작동 메서드
    private fun startTimer() {

    }
    // 타이머가 끝난 이후 edittext 비활성화 해주는 메서드
    private fun banTypeWord() {

    }
    // 단어가 존재하는지 확인하는 메서드
    private fun checkWord() {

    }
    // 승리, 패배화면 호출 메서드
    private fun matchEndCall() {

    }
    // 종료 후, 매치 대기열 화면으로 이동하는 메서드
    private fun moveToWait() {

    }
}