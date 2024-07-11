package com.example.realtimeserivce.ui.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.realtimeserivce.R
import com.example.realtimeserivce.databinding.FragmentFirebaseAuthLoginBinding
import com.example.realtimeserivce.viewmodel.FirebaseAuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class FirebaseAuthLoginFragment : Fragment() {
    private lateinit var fragmentFirebaseAuthLoginBinding: FragmentFirebaseAuthLoginBinding
    private val viewModel: FirebaseAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentFirebaseAuthLoginBinding = FragmentFirebaseAuthLoginBinding.inflate(layoutInflater, container, false)
        return fragmentFirebaseAuthLoginBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentFirebaseAuthLoginBinding.btnFirebaseAuthLogin.setOnClickListener {
            viewModel.signInAnonymously()
            viewModel.user.observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    navigate()
                } else {
                    Log.d("firebase auth failed", "null")
                }
            }
        }
    }

    private fun navigate() {
        activity?.findViewById<FragmentContainerView>(R.id.main_container)?.visibility = View.VISIBLE
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)?.visibility = View.VISIBLE
        activity?.findViewById<FragmentContainerView>(R.id.login_container)?.visibility = View.GONE

        val navController = (activity?.supportFragmentManager?.findFragmentById(R.id.main_container) as NavHostFragment).navController
        navController.navigate(R.id.homeGroundFragment)
    }
}