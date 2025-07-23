package com.example.teumteum.ui.friend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R

class FollowingAdapter(
    private val data: List<FollowUser>,
    private val onProfileClick: (FollowUser) -> Unit
) : RecyclerView.Adapter<FollowingAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.nameTv)
        private val jobTv: TextView = itemView.findViewById(R.id.jobTv)
        private val starIv: ImageButton = itemView.findViewById(R.id.starIv)
        private val profileBtn: ImageButton = itemView.findViewById(R.id.profileLayout)

        fun bind(user: FollowUser) {
            // 이름/직업 세팅
            nameTv.text = user.name
            jobTv.text = " · ${user.job}"

            // 별 아이콘 토글
            starIv.setImageResource(
                if (user.isFavorite) R.drawable.friend01_fill_star
                else R.drawable.friend01_star
            )
            starIv.setOnClickListener {
                user.isFavorite = !user.isFavorite
                notifyItemChanged(adapterPosition)
            }

            // 프로필 사진 클릭 시 콜백
            profileBtn.setOnClickListener {
                onProfileClick(user)
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
