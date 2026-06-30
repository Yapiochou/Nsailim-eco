package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed_challenges")
data class CompletedChallenge(
    @PrimaryKey val challengeId: String,
    val title: String,
    val category: String,
    val duration: String,
    val xpReward: Int,
    val difficulty: String = "Facile",
    val isCompleted: Boolean = false,
    val isPendingValidation: Boolean = false,
    val proofPhotoPath: String? = null,
    val proofDescription: String? = null
)
