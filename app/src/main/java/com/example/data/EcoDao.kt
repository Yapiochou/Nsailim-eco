package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EcoDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgress)

    @Query("SELECT * FROM completed_lessons")
    fun getAllLessons(): Flow<List<CompletedLesson>>

    @Query("SELECT * FROM completed_lessons WHERE lessonId = :lessonId LIMIT 1")
    fun getLessonById(lessonId: String): Flow<CompletedLesson?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: CompletedLesson)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<CompletedLesson>)

    @Query("SELECT * FROM completed_challenges")
    fun getAllChallenges(): Flow<List<CompletedChallenge>>

    @Query("SELECT * FROM completed_challenges WHERE challengeId = :challengeId LIMIT 1")
    fun getChallengeById(challengeId: String): Flow<CompletedChallenge?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: CompletedChallenge)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<CompletedChallenge>)
}
