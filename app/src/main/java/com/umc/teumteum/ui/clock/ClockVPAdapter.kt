package com.umc.teumteum.ui.clock

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.github.mikephil.charting.charts.PieChart

class ClockVPAdapter<VB : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    private val chartOf: (VB) -> PieChart,
    private val onBindPage: (chart: PieChart, half: ClockHalf) -> Unit
) : RecyclerView.Adapter<ClockVPAdapter<VB>.ViewHolder>() {

    private val halves = listOf(ClockHalf.AM, ClockHalf.PM) // 0=AM, 1=PM

    inner class ViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root) {
        val chart: PieChart = chartOf(binding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vb = inflate(LayoutInflater.from(parent.context), parent, false)
        vb.root.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        return ViewHolder(vb)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBindPage(holder.chart, halves[position])
    }

    override fun getItemCount(): Int = halves.size

    // 주어진 Half의 페이지 인덱스
    fun positionOf(half: ClockHalf): Int = if (half == ClockHalf.AM) 0 else 1

    fun refresh(half: ClockHalf) {
        notifyItemChanged(positionOf(half))
    }

    // 양쪽 페이지 모두 갱신
    fun refreshAll() {
        notifyItemChanged(0)
        notifyItemChanged(1)
    }
}