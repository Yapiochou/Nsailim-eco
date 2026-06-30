package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class EcoRepository(private val ecoDao: EcoDao) {

    val userProgress: Flow<UserProgress?> = ecoDao.getUserProgress()
    val allLessons: Flow<List<CompletedLesson>> = ecoDao.getAllLessons()
    val allChallenges: Flow<List<CompletedChallenge>> = ecoDao.getAllChallenges()

    fun getChallengeById(challengeId: String): Flow<CompletedChallenge?> {
        return ecoDao.getChallengeById(challengeId)
    }

    fun getLessonById(lessonId: String): Flow<CompletedLesson?> {
        return ecoDao.getLessonById(lessonId)
    }

    suspend fun insertUserProgress(progress: UserProgress) {
        ecoDao.insertUserProgress(progress)
    }

    suspend fun insertLesson(lesson: CompletedLesson) {
        ecoDao.insertLesson(lesson)
    }

    suspend fun insertChallenge(challenge: CompletedChallenge) {
        ecoDao.insertChallenge(challenge)
    }

    suspend fun initializeDatabaseIfEmpty() {
        val currentProgress = ecoDao.getUserProgress().firstOrNull()
        if (currentProgress == null) {
            // Seed default UserProgress
            ecoDao.insertUserProgress(UserProgress())
        }

        val currentLessons = ecoDao.getAllLessons().firstOrNull()
        if (currentLessons.isNullOrEmpty()) {
            val defaultLessons = listOf(
                CompletedLesson(
                    lessonId = "plastique",
                    title = "Plastique et Déchets",
                    category = "♻️ Plastique et Déchets",
                    duration = "12 Leçons",
                    xpReward = 500,
                    progressPercent = 45,
                    isCompleted = false
                ),
                CompletedLesson(
                    lessonId = "eau",
                    title = "Préservation de l'Eau",
                    category = "💧 Préservation de l'Eau",
                    duration = "8 Leçons",
                    xpReward = 300,
                    progressPercent = 15,
                    isCompleted = false
                ),
                CompletedLesson(
                    lessonId = "habitudes",
                    title = "Habitudes Écolo",
                    category = "🌱 Habitudes Écolo",
                    duration = "15 Leçons",
                    xpReward = 600,
                    progressPercent = 0,
                    isCompleted = false
                )
            )
            ecoDao.insertLessons(defaultLessons)
        }

        val currentChallenges = ecoDao.getAllChallenges().firstOrNull()
        if (currentChallenges.isNullOrEmpty()) {
            val defaultChallenges = listOf(
                CompletedChallenge(
                    challengeId = "reduire_dechets",
                    title = "Réduire les déchets plastiques aujourd'hui",
                    category = "Déchets plastiques",
                    duration = "il reste 10 min",
                    xpReward = 50,
                    difficulty = "Facile"
                ),
                CompletedChallenge(
                    challengeId = "economiser_eau",
                    title = "Économiser 10L d'eau",
                    category = "Eau",
                    duration = "il reste 25 min",
                    xpReward = 50,
                    difficulty = "Facile"
                ),
                CompletedChallenge(
                    challengeId = "eco_gestes",
                    title = "Éco-gestes du quotidien",
                    category = "Éco-gestes",
                    duration = "Démarrer",
                    xpReward = 50,
                    difficulty = "Facile"
                )
            )
            ecoDao.insertChallenges(defaultChallenges)
        }
    }
}
