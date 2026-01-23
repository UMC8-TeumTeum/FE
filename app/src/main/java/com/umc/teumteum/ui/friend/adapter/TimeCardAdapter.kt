package com.umc.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umc.teumteum.R
import com.umc.teumteum.ui.friend.data.TimeCardItem

class TimeCardAdapter(
    private val onTimeClick: (
        position: Int,
        isStart: Boolean,
        startBound: String,
        endBound: String,
        current: String
    ) -> Unit
) : RecyclerView.Adapter<TimeCardAdapter.TimeCardViewHolder>() {

    private val items = mutableListOf<TimeCardItem>()
    private var selectedPosition = RecyclerView.NO_POSITION

    fun setData(newList: List<TimeCardItem>) {
        items.clear()
        items.addAll(newList)
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun updateTime(position: Int, isStart: Boolean, newTime: String) {
        if (position == RecyclerView.NO_POSITION || position !in items.indices) return
        val old = items[position]
        val updated = if (isStart) old.copy(startTime = newTime) else old.copy(endTime = newTime)
        items[position] = updated
        notifyItemChanged(position)
    }

    inner class TimeCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val startTimeText: TextView = itemView.findViewById(R.id.startTime)
        private val endTimeText: TextView = itemView.findViewById(R.id.endTime)
        private val timeCard: View = itemView.findViewById(R.id.timeCard)

        fun bind(item: TimeCardItem, isSelected: Boolean) {
            startTimeText.text = item.startTime
            endTimeText.text = item.endTime

            timeCard.setBackgroundResource(
                if (isSelected) R.drawable.friend_time_card_bg_selected
                else R.drawable.friend_time_card_bg_default
            )

            // 카드 영역 클릭: 선택 하이라이트 갱신
            timeCard.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                val prev = selectedPosition
                selectedPosition = pos
                if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
            }

            // 시작 클릭
            startTimeText.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                onTimeClick(
                    pos,
                    true,
                    item.initialStartTime,   // startBound
                    item.initialEndTime,     // endBound
                    item.startTime    // current
                )
            }

            // 종료 클릭
            endTimeText.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                onTimeClick(
                    pos,
                    false,
                    item.initialStartTime,   // startBound
                    item.initialEndTime,     // endBound
                    item.endTime      // current
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_card, parent, false)
        return TimeCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeCardViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    fun getSelectedItem(): TimeCardItem? =
        if (selectedPosition in items.indices) items[selectedPosition] else null

    override fun getItemCount(): Int = items.size
}