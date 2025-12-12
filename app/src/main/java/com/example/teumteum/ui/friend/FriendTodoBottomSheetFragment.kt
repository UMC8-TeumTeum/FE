package com.example.teumteum.ui.friend

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.teumteum.R
import com.example.teumteum.ui.friend.adapter.TodoEventAdapter
import com.example.teumteum.ui.friend.data.TeumEvent
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class FriendTodoBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: TodoEventAdapter
    private lateinit var dotsIndicator: DotsIndicator

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(R.drawable.calendar_background)
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_friend_todo, container, false)

        viewPager = view.findViewById(R.id.existingTodoViewPager)
        dotsIndicator = view.findViewById(R.id.dotsIndicator)

        // 샘플 데이터
        val sampleList = listOf(
            TeumEvent("12:00", "14:30", "강아지 산책 가자"),
            TeumEvent("15:00", "16:00", "회의"),
            TeumEvent("17:30", "18:30", "홍대 소품샵 투어")
        )


        adapter = TodoEventAdapter(sampleList)
        viewPager.adapter = adapter

        // DotsIndicator 연결
        dotsIndicator.setViewPager2(viewPager)

        // 버튼 클릭
        view.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener { dismiss() }
        view.findViewById<MaterialButton>(R.id.btnAccept).setOnClickListener {
            // 수락 처리
        }

        return view
    }
}
