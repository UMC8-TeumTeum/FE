package com.umc.teumteum.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import com.umc.teumteum.ui.friend.FriendFragment
import com.umc.teumteum.ui.myhome.MyHomeFragment
import com.umc.teumteum.R
import com.umc.teumteum.databinding.ActivityMainBinding
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    // Android 13+(API 33) 알림 권한 요청 런처
    private val requestPostNotiPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("Permission", "POST_NOTIFICATIONS granted=$granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱 첫 진입 시 알림 권한 체크 & 요청 (Android 13+)
        ensurePostNotificationsPermission()

        initBottomNavigation()

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        binding.tutorialOverlayContainer.btnCloseTutorial.setOnClickListener {
            hideTutorialOverlay()
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM", "FCM Token: $token")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Failed to fetch FCM token", e)
            }
    }

    private fun ensurePostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                requestPostNotiPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun initBottomNavigation() {

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, HomeFragment())
            .commitAllowingStateLoss()

        binding.mainBnv.setOnItemSelectedListener { item ->

            // 알림에서 시작된 백스택 제거
            supportFragmentManager.popBackStack(
                "ALARM_FLOW",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )

            supportFragmentManager.popBackStack(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )

            when (item.itemId) {

                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, HomeFragment())
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

    fun hideBottomBar() { binding.mainBnv.visibility = View.GONE }
    fun showBottomBar() { binding.mainBnv.visibility = View.VISIBLE }

    fun showTutorialOverlay() {
        binding.tutorialOverlayContainer.root.visibility = View.VISIBLE
        binding.tutorialOverlayContainer.root.bringToFront()

        binding.mainBnv.visibility = View.GONE
        binding.bottomNavDivider.visibility = View.GONE

        binding.tutorialOverlayContainer.labelTop.highlightText("일정과 수면패턴")
        binding.tutorialOverlayContainer.labelRightTop.highlightText("수면 패턴")
        binding.tutorialOverlayContainer.labelLeftBottom.highlightText("빈틈")
        binding.tutorialOverlayContainer.labelLeftTop.highlightText("오늘의 일정")
        binding.tutorialOverlayContainer.labelBottom.highlightText("오전과 오후")
        binding.tutorialOverlayContainer.labelCalendar.highlightText("캘린더")
        binding.tutorialOverlayContainer.labelTodoList.highlightText("투두리스트")
    }

    fun hideTutorialOverlay() {
        binding.tutorialOverlayContainer.root.visibility = View.GONE
        binding.mainBnv.visibility = View.VISIBLE
        binding.bottomNavDivider.visibility = View.VISIBLE

        supportFragmentManager.setFragmentResult("tutorial_closed", Bundle.EMPTY)
    }

    private fun TextView.highlightText(
        target: String,
        @ColorRes colorRes: Int = R.color.main_1
    ) {
        val fullText = text.toString()
        val start = fullText.indexOf(target)
        if (start == -1) return

        val end = start + target.length
        val spannable = SpannableString(fullText).apply {
            setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, colorRes)),
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        text = spannable
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
