package com.example.teumteum.ui.main

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.teumteum.ui.feed.FeedFragment
import com.example.teumteum.ui.friend.FriendFragment
import com.example.teumteum.ui.myhome.MyHomeFragment
import com.example.teumteum.R
import com.example.teumteum.receiver.ScreenOnReceiver
import com.example.teumteum.databinding.ActivityMainBinding
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var screenOnReceiver: ScreenOnReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBottomNavigation()

        // 화면 켜짐 감지 리시버 등록
        screenOnReceiver = ScreenOnReceiver()
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        registerReceiver(screenOnReceiver, filter)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM", "FCM Token: $token")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Failed to fetch FCM token", e)
            }
    }

    private fun initBottomNavigation() {

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, HomeFragment())
            .commitAllowingStateLoss()

        binding.mainBnv.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, HomeFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.fragment_feed -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, FeedFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.fragment_friend -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, FriendFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.fragment_myhome -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MyHomeFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    fun hideBottomBar() {
        binding.mainBnv.visibility = View.GONE
    }

    fun showBottomBar() {
        binding.mainBnv.visibility = View.VISIBLE
    }
}