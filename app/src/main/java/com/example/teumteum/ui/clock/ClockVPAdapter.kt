package com.example.teumteum.ui.clock

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.databinding.ItemClockPageBinding
import com.github.mikephil.charting.charts.PieChart

class ClockVPAdapter(
    private val onBindPage: (chart: PieChart, half: ClockHalf) -> Unit
) : RecyclerView.Adapter<ClockVPAdapter.ViewHolder>() {

    private val halves = listOf(ClockHalf.PM, ClockHalf.AM) // 0=PM, 1=AM

    inner class ViewHolder(val binding: ItemClockPageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemClockPageBinding =
            ItemClockPageBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        binding.root.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBindPage(holder.binding.clockChart, halves[position])
    }

    override fun getItemCount(): Int = halves.size

    // 주어진 Half의 페이지 인덱스
    fun positionOf(half: ClockHalf): Int = if (half == ClockHalf.AM) 1 else 0

    fun refresh(half: ClockHalf) {
        notifyItemChanged(positionOf(half))
    }

    // 양쪽 페이지 모두 갱신
    fun refreshAll() {
        notifyItemChanged(0)
        notifyItemChanged(1)
    }
}