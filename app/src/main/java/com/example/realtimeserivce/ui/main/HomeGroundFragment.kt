package com.example.realtimeserivce.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realtimeserivce.R
import com.example.realtimeserivce.adapter.StatusAdapter
import com.example.realtimeserivce.databinding.FragmentHomeGroundBinding
import com.example.realtimeserivce.viewmodel.FirebaseAuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeGroundFragment : Fragment() {
    private lateinit var fragmentHomeGroundBinding: FragmentHomeGroundBinding
    private lateinit var statusAdapter: StatusAdapter
    private val viewModel: FirebaseAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentHomeGroundBinding = FragmentHomeGroundBinding.inflate(layoutInflater, container, false)
        statusAdapter = StatusAdapter(mutableListOf()) { uid ->
            // safeargs에 클릭한 상대방 아이콘에 담겨있는 uid를 담아 messageFragment로 navigate해주기
            val action = HomeGroundFragmentDirections.actionHomeGroundFragmentToMessageFragment(uid)
            findNavController().navigate(action)
        }

        fragmentHomeGroundBinding.recyclerStatus.run {
            adapter = statusAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // db에 등록된 사용자 접속 상태를 감지하여 변경해준다.
        viewModel.currentStatus.observe(viewLifecycleOwner) { list ->
            statusAdapter.updateCurrentUserList(list)
        }
        return fragmentHomeGroundBinding.root
    }

    // 현재 view의 상태에 따라 플레이어 상태 변경
    override fun onResume() {
        super.onResume()
        /* view.gone에서는 사용자 ui상으로 보이지 않는 것이기 때문에 여전히 fragment가 동작하기 때문에
        container가 현재 활성화 되어있는지 체크 후 true를 반환할 때, 접속 상태를 online으로 표기한다. */
        if (checkControllerView()) {
            viewModel.setOnlineStatus(true)
        } else viewModel.setOnlineStatus(false)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setOnlineStatus(false)
    }

    private fun checkControllerView(): Boolean {
        val currentContainerViewType = activity?.findViewById<FragmentContainerView>(R.id.main_container)?.visibility
        return if (currentContainerViewType == View.VISIBLE) true else false
    }
}