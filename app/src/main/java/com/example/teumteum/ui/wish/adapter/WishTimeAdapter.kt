package com.example.teumteum.ui.wish.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.databinding.ItemWishTimeBinding
import com.example.teumteum.ui.wish.data.UiTimeSlot

class WishTimeAdapter(
    private val onSelect: (position: Int, slot: UiTimeSlot) -> Unit,
    private val onDirectInput: () -> Unit,
    private val includeDirectFirst: Boolean = true
) : ListAdapter<UiTimeSlot, RecyclerView.ViewHolder>(DIFF) {

    private var selectedPos: Int = RecyclerView.NO_POSITION

    companion object {
        private const val TYPE_DIRECT = 0
        private const val TYPE_SLOT = 1

        private val DIFF = object : DiffUtil.ItemCallback<UiTimeSlot>() {
            override fun areItemsTheSame(oldItem: UiTimeSlot, newItem: UiTimeSlot): Boolean {
                // 같은 시간 라벨이면 동일 항목으로 간주
                return oldItem.startTime == newItem.startTime && oldItem.endTime == newItem.endTime
            }
            override fun areContentsTheSame(oldItem: UiTimeSlot, newItem: UiTimeSlot): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getItemCount(): Int {
        val base = super.getItemCount()
        return if (includeDirectFirst) base + 1 else base
    }

    override fun getItemViewType(position: Int): Int {
        return if (includeDirectFirst && position == 0) TYPE_DIRECT else TYPE_SLOT
    }

    private fun dataIndex(adapterPos: Int): Int =
        if (includeDirectFirst) adapterPos - 1 else adapterPos

    private fun getSlotAt(adapterPos: Int): UiTimeSlot? {
        if (getItemViewType(adapterPos) == TYPE_DIRECT) return null
        val i = dataIndex(adapterPos)
        if (i !in 0 until super.getItemCount()) return null
        return super.getItem(i)
    }

    inner class DirectVH(private val binding: ItemWishTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(isSelected: Boolean) = with(binding) {
            timeSelect01Tv.text = "직접 입력하기"

            val selectedBg = ContextCompat.getColor(root.context, R.color.text_primary)
            val selectedText = ContextCompat.getColor(root.context, R.color.white)
            val defaultBg = ContextCompat.getColor(root.context, R.color.teumteum_gray)
            val defaultText = ContextCompat.getColor(root.context, R.color.text_secondary)

            if (isSelected) {
                select01Button.setBackgroundColor(selectedBg)
                select01Button.setTextColor(selectedText)
            } else {
                select01Button.setBackgroundColor(defaultBg)
                select01Button.setTextColor(defaultText)
            }

            fun handleClick() {
                val newPos = adapterPosition
                if (newPos == RecyclerView.NO_POSITION) return
                val old = selectedPos
                if (old != newPos) {
                    selectedPos = newPos
                    if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
                    notifyItemChanged(newPos)
                }
                onDirectInput()
            }

            time01Cv.setOnClickListener { handleClick() }
            select01Button.setOnClickListener { handleClick() }
        }
    }

    inner class SlotVH(private val binding: ItemWishTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UiTimeSlot, isSelected: Boolean) = with(binding) {
            // 이미 HH:mm 이므로 그대로 표시
            timeSelect01Tv.text = "${item.startTime} ~ ${item.endTime}"

            val selectedBg = ContextCompat.getColor(root.context, R.color.text_primary)
            val selectedText = ContextCompat.getColor(root.context, R.color.white)
            val defaultBg = ContextCompat.getColor(root.context, R.color.teumteum_gray)
            val defaultText = ContextCompat.getColor(root.context, R.color.text_secondary)

            if (isSelected) {
                select01Button.setBackgroundColor(selectedBg)
                select01Button.setTextColor(selectedText)
            } else {
                select01Button.setBackgroundColor(defaultBg)
                select01Button.setTextColor(defaultText)
            }

            fun handleClick() {
                val newPos = adapterPosition
                if (newPos == RecyclerView.NO_POSITION) return
                val old = selectedPos
                if (old != newPos) {
                    selectedPos = newPos
                    if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
                    notifyItemChanged(newPos)
                }
                onSelect(newPos, item)
            }

            time01Cv.setOnClickListener { handleClick() }
            select01Button.setOnClickListener { handleClick() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        val binding = ItemWishTimeBinding.inflate(inf, parent, false)
        return if (viewType == TYPE_DIRECT) DirectVH(binding) else SlotVH(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val isSelected = position == selectedPos
        when (holder) {
            is DirectVH -> holder.bind(isSelected)
            is SlotVH -> holder.bind(getSlotAt(position) ?: return, isSelected)
        }
    }

    fun getSelected(): UiTimeSlot? =
        if (selectedPos == RecyclerView.NO_POSITION || getItemViewType(selectedPos) == TYPE_DIRECT) null
        else getSlotAt(selectedPos)

    fun setSelectedByDataIndex(dataIdx: Int) {
        val adapterPos = if (includeDirectFirst) dataIdx + 1 else dataIdx
        if (adapterPos !in 0 until itemCount) return
        val old = selectedPos
        selectedPos = adapterPos
        if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
        notifyItemChanged(adapterPos)
    }

    fun selectDirectInput() {
        if (!includeDirectFirst) return
        val old = selectedPos
        selectedPos = 0
        if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
        notifyItemChanged(0)
    }

    // 선택 초기화
    fun clearSelection() {
        val old = selectedPos
        selectedPos = RecyclerView.NO_POSITION
        if (old != RecyclerView.NO_POSITION) {
            notifyItemChanged(old)
        }
    }
}
