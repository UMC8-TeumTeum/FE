package com.example.teumteum.ui.friend.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.ui.friend.data.TimeCardItem

class TimeCardAdapter(
    private val onItemClick: (TimeCardItem) -> Unit
) : RecyclerView.Adapter<TimeCardAdapter.TimeCardViewHolder>() {

    private val items = mutableListOf<TimeCardItem>()
    private var selectedPosition = RecyclerView.NO_POSITION

    fun setData(newList: List<TimeCardItem>) {
        Log.d("ADAPTER", "setData() called with ${newList.size} items")
        items.clear()
        items.addAll(newList)
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    inner class TimeCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val startTimeText: TextView = itemView.findViewById(R.id.startTime)
        private val endTimeText: TextView = itemView.findViewById(R.id.endTime)
        private val timeCard: View = itemView.findViewById(R.id.timeCard)

        fun bind(item: TimeCardItem, isSelected: Boolean) {
            startTimeText.text = item.startTime
            endTimeText.text = item.endTime

            // 배경 설정
            timeCard.setBackgroundResource(
                if (isSelected) R.drawable.friend_time_card_bg_selected
                else R.drawable.friend_time_card_bg_default
            )

            // 클릭 리스너
            timeCard.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition

                // 이전 선택 아이템 갱신
                if (previousPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousPosition)
                }
                // 현재 선택 아이템 갱신
                notifyItemChanged(selectedPosition)

                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_card, parent, false)
        return TimeCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeCardViewHolder, position: Int) {
        Log.d("ADAPTER", "onBindViewHolder: position=$position, item=${items[position]}")
        val isSelected = (position == selectedPosition)
        holder.bind(items[position], isSelected)
    }


    override fun getItemCount(): Int = items.size
}

