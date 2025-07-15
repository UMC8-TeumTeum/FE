package com.example.teumteum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.data.entities.TodoHomeItem
import com.example.teumteum.ui.edit.TodoEditFragment

class TodoAdapter(private val todoList: List<TodoHomeItem>, private val fragmentManager: FragmentManager) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.tv_todo_title)
        val startTime = itemView.findViewById<TextView>(R.id.tv_start_time)
        val endTime = itemView.findViewById<TextView>(R.id.tv_end_time)
        val lockIcon = itemView.findViewById<ImageView>(R.id.iv_lock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todolist, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val item = todoList[position]
        holder.title.text = item.title
        holder.startTime.text = convertTo24HourFormat(item.startTime)
        holder.endTime.text = convertTo24HourFormat(item.endTime)

        if (item.isPublic) {
            holder.lockIcon.setImageResource(R.drawable.ic_unlock_sv)
        } else {
            holder.lockIcon.setImageResource(R.drawable.ic_lock_sv)
        }

        holder.itemView.setOnClickListener {
            val bottomSheet = TodoEditFragment.newInstanceWithDummy(item)
            bottomSheet.show(fragmentManager, bottomSheet.tag)
        }

    }

    override fun getItemCount(): Int = todoList.size

    private fun convertTo24HourFormat(time: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("a h:mm", java.util.Locale.KOREAN)
            val outputFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.KOREAN)
            val date = inputFormat.parse(time)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            time // 변환 실패 시 원본 반환
        }
    }
}