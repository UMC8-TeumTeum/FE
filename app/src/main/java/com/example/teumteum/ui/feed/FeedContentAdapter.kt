import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.data.Feed
import com.example.teumteum.databinding.ItemFeedBinding

class FeedContentAdapter : ListAdapter<Feed, FeedContentAdapter.FeedViewHolder>(DIFF_CALLBACK) {

    inner class FeedViewHolder(private val binding: ItemFeedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Feed) {
            binding.usernameText.text = item.username
            binding.contentText.text = item.content
            binding.timeText.text = getRelativeTime(item.createdAt)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getRelativeTime(createdAt: Long): String {
        val now = System.currentTimeMillis()
        val diffMillis = now - createdAt

        val minutes = diffMillis / 1000 / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            else -> "${days}일 전"
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Feed>() {
            override fun areItemsTheSame(oldItem: Feed, newItem: Feed) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Feed, newItem: Feed) =
                oldItem == newItem
        }
    }
}