package com.umc.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umc.teumteum.R
import com.umc.teumteum.ui.friend.data.TimeCardItem

class TimeConflictCardAdapter(

    // 시작/종료 시간 클릭 시 → TimePicker 띄우기용
    private val onTimeClick: (
        position: Int,
        isStart: Boolean,
        startBound: String,
        endBound: String,
        current: String
    ) -> Unit,

    //  시작 + 종료 모두 선택 완료 시 호출 (서버 전송용)
    private val onTimeCompleted: (
        startTime: String,
        endTime: String
    ) -> Unit

) : RecyclerView.Adapter<TimeConflictCardAdapter.TimeConflictCardViewHolder>() {

    private val items = mutableListOf<TimeCardItem>()
    private var selectedPosition = RecyclerView.NO_POSITION

    // 서버 전송용 "원본 시간"
    private var rawStartTime: String? = null
    private var rawEndTime: String? = null

    fun setData(newList: List<TimeCardItem>) {
        items.clear()
        items.addAll(newList)
        selectedPosition = RecyclerView.NO_POSITION
        rawStartTime = null
        rawEndTime = null
        notifyDataSetChanged()
    }

    /**
     * 시간 선택 후 UI 갱신 + 원본 시간 저장
     */
    fun updateTime(position: Int, isStart: Boolean, pickedTime: String) {
        if (position !in items.indices) return

        val old = items[position]
        val updated = if (isStart) {
            rawStartTime = pickedTime
            old.copy(startTime = pickedTime)
        } else {
            rawEndTime = pickedTime
            old.copy(endTime = pickedTime)
        }

        items[position] = updated

        // 선택 카드 갱신
        val prev = selectedPosition
        selectedPosition = position
        if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
        notifyItemChanged(position)

        // 시작 + 종료 둘 다 선택됐을 때만 호출
        val start = rawStartTime
        val end = rawEndTime
        if (start != null && end != null) {
            onTimeCompleted(start, end)
        }
    }

    inner class TimeConflictCardViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val startTimeText: TextView = itemView.findViewById(R.id.startTime)
        private val endTimeText: TextView = itemView.findViewById(R.id.endTime)
        private val timeCard: View = itemView.findViewById(R.id.timeCard)

        fun bind(item: TimeCardItem, isSelected: Boolean) {
            startTimeText.text = item.startTime
            endTimeText.text = item.endTime

            timeCard.setBackgroundResource(
                if (isSelected)
                    R.drawable.friend_time_card_bg_selected
                else
                    R.drawable.friend_time_card_bg_default
            )

            // 카드 선택
            timeCard.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                val prev = selectedPosition
                selectedPosition = pos
                if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
            }

            // 시작 시간 클릭
            startTimeText.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                onTimeClick(
                    pos,
                    true,
                    item.initialStartTime,
                    item.initialEndTime,
                    item.startTime
                )
            }

            // 종료 시간 클릭
            endTimeText.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                onTimeClick(
                    pos,
                    false,
                    item.initialStartTime,
                    item.initialEndTime,
                    item.endTime
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : TimeConflictCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_card, parent, false)
        return TimeConflictCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeConflictCardViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedItem(): TimeCardItem? =
        if (selectedPosition in items.indices) items[selectedPosition] else null
}
