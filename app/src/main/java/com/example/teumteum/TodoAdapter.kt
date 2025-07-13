package com.example.teumteum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.data.entities.TodoHomeItem

class TodoAdapter(private val todoList: List<TodoHomeItem>) :
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
        holder.startTime.text = item.startTime
        holder.endTime.text = item.endTime

        if (item.isPublic) {
            holder.lockIcon.setImageResource(R.drawable.ic_unlock_sv)
        } else {
            holder.lockIcon.setImageResource(R.drawable.ic_lock_sv)
        }
    }

    override fun getItemCount(): Int = todoList.size
}