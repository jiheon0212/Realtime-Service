package com.example.realtimeserivce.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.realtimeserivce.R
import com.example.realtimeserivce.databinding.FragmentMyPageBinding
import com.example.realtimeserivce.viewmodel.FirebaseAuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MyPageFragment : Fragment() {
    private lateinit var fragmentMyPageBinding: FragmentMyPageBinding
    private val viewModel: FirebaseAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentMyPageBinding = FragmentMyPageBinding.inflate(layoutInflater, container, false)
        return fragmentMyPageBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentMyPageBinding.btnFirebaseAuthLogout.setOnClickListener {
            viewModel.signOut()
            navigate()
        }
    }

    private fun navigate() {
        activity?.findViewById<FragmentContainerView>(R.id.main_container)?.visibility = View.GONE
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)?.visibility = View.GONE
        activity?.findViewById<FragmentContainerView>(R.id.login_container)?.visibility = View.VISIBLE

        val navController = (activity?.supportFragmentManager?.findFragmentById(R.id.login_container) as NavHostFragment).navController
        navController.navigate(R.id.firebaseAuthLoginFragment)
    }
}