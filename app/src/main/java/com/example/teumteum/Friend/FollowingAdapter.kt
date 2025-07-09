package com.example.teumteum.Friend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R

class FollowingAdapter(private val data: List<FollowUser>) :
    RecyclerView.Adapter<FollowingAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.nameTv)
        private val jobTv: TextView = itemView.findViewById(R.id.jobTv)
        private val starIv: ImageButton = itemView.findViewById(R.id.starIv)

        fun bind(user: FollowUser) {
            nameTv.text = user.name
            jobTv.text = " · ${user.job}"

            // 초기 별 아이콘 설정
            starIv.setImageResource(
                if (user.isFavorite) R.drawable.friend01_fill_star
                else R.drawable.friend01_star
            )

            // 클릭 이벤트로 즐겨찾기 상태 변경
            starIv.setOnClickListener {
                user.isFavorite = !user.isFavorite
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend01_item_following, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}
