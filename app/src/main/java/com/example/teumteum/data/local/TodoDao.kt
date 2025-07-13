package com.example.teumteum.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.teumteum.data.entities.Todo
import com.example.teumteum.data.entities.TodoHomeItem

@Dao
interface TodoDao {
    @Insert
    fun insert(todo: Todo)

    @Query("SELECT id, title, startTime, endTime, isPublic FROM TodoTable ORDER BY id DESC")
    fun getAllHomeTodos(): List<TodoHomeItem>
}