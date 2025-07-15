package com.example.teumteum.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TodoTable")
data class Todo(
    val title: String,
    val startTime: String,
    val alarms: String,
    val endTime: String,
    val isPublic: Boolean,
    val isIncluded: Boolean
){
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}

data class TodoHomeItem(
    val id: Int,
    val title: String,
    val startTime: String,
    val endTime: String,
    val isPublic: Boolean
)