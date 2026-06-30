package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed_lessons")
data class CompletedLesson(
    @PrimaryKey val lessonId: String,
    val title: String,
    val category: String,
    val duration: String,
    val xpReward: Int,
    val progressPercent: Int = 0,
    val isCompleted: Boolean = false
)
