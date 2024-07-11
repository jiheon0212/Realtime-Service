package com.example.realtimeserivce.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.realtimeserivce.R
import com.example.realtimeserivce.databinding.FragmentMatchWaitBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MatchWaitFragment : Fragment() {
    private lateinit var fragmentMatchWaitBinding: FragmentMatchWaitBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentMatchWaitBinding = FragmentMatchWaitBinding.inflate(layoutInflater, container, false)
        return fragmentMatchWaitBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentMatchWaitBinding.btnMatchWait.setOnClickListener {

        }
    }
    
    // 매치 대기열 Status를 변경하는 메서드 -> button을 통해 toggle 활성화/비활성화
    private fun matchToggle() {

    }
    // 매칭 메서드
    private fun searchMatch() {

    }
    // 매칭 성사 시, 매치 화면을 호출하는 메서드
    private fun onMatchFound() {
        val action = MatchWaitFragmentDirections.actionMatchWaitFragmentToMatchFragment()
        findNavController().navigate(action)
    }
}