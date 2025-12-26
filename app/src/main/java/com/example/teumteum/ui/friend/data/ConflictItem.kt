package com.example.teumteum.ui.friend.data

/**
 * ViewPager2에 표시될 아이템의 데이터 클래스
 */
data class ConflictItem(
    val title: String,
    val description: String,
    val userName: String,
    val time: String,
    val date: String? = null
    // val profileUrl: String? = null // (프로필 이미지 URL)
)