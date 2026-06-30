package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val name: String = "Amani Silva",
    val level: Int = 4,
    val xp: Int = 1250,
    val streakDays: Int = 8,
    val memberSince: String = "Jan 2024",
    val isPartnerView: Boolean = false,
    val hasCompletedOnboarding: Boolean = false
)
