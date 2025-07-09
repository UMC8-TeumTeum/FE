package com.example.teumteum.Friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.databinding.Friend01ItemRecommendCardBinding

class RecommendAdapter(
    private val onCardClick: () -> Unit
) : RecyclerView.Adapter<RecommendAdapter.RecommendViewHolder>() {

    inner class RecommendViewHolder(val binding: Friend01ItemRecommendCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onCardClick() // 클릭 시 프래그먼트 이동 함수 호출
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = Friend01ItemRecommendCardBinding.inflate(inflater, parent, false)
        return RecommendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendViewHolder, position: Int) {
        // 데이터 바인딩이 있다면 여기에 추가
    }

    override fun getItemCount(): Int = 10 // 예시
}
