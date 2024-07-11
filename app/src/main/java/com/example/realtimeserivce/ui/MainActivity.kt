package com.example.realtimeserivce.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.realtimeserivce.R
import com.example.realtimeserivce.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var navControllerMain: NavController
    private lateinit var navControllerMatch: NavController
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment1 = supportFragmentManager.findFragmentById(R.id.main_container) as NavHostFragment
        navControllerMain = navHostFragment1.navController
        // bottom navigation과 main container의 navcontroller를 연결시켜준다.
        activityMainBinding.bottomNavigationView.setupWithNavController(navControllerMain)

        // fragment간 이동에 navcontroller를 통해 bottomnavigationview의 가시성을 조정해준다.
        navControllerMain.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeGroundFragment, R.id.chatroomFragment, R.id.myPageFragment, R.id.matchWaitFragment -> {
                    activityMainBinding.bottomNavigationView.visibility = View.VISIBLE
                }
                else -> {
                    activityMainBinding.bottomNavigationView.visibility = View.GONE
                }
            }
        }

        // 뒤로가기 버튼에 navcontroller의 백스택으로 돌아가는 기능을 가진 callback을 등록해준다.
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!navControllerMain.popBackStack()) {
                    finish()
                } else {
                    return
                }
            }
        })

        // 현재 기기에 등록된 auth 정보가 존재 true -> main / false -> login container를 호출해준다.
        if (auth.currentUser != null) {
            activityMainBinding.loginContainer.visibility = View.GONE
            activityMainBinding.mainContainer.visibility = View.VISIBLE
            activityMainBinding.bottomNavigationView.visibility = View.VISIBLE
        } else {
            activityMainBinding.loginContainer.visibility = View.VISIBLE
            activityMainBinding.mainContainer.visibility = View.GONE
            activityMainBinding.bottomNavigationView.visibility = View.GONE
        }
    }
}