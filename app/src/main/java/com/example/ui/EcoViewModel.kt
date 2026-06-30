package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CompletedChallenge
import com.example.data.CompletedLesson
import com.example.data.EcoRepository
import com.example.data.UserProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EcoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EcoRepository

    val userProgress: StateFlow<UserProgress?>
    val allLessons: StateFlow<List<CompletedLesson>>
    val allChallenges: StateFlow<List<CompletedChallenge>>

    // Quiz states
    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedAnswerIndex: StateFlow<Int?> = _selectedAnswerIndex.asStateFlow()

    private val _isAnswerChecked = MutableStateFlow(false)
    val isAnswerChecked: StateFlow<Boolean> = _isAnswerChecked.asStateFlow()

    private val _isCorrect = MutableStateFlow<Boolean?>(null)
    val isCorrect: StateFlow<Boolean?> = _isCorrect.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = EcoRepository(database.ecoDao())

        // Initialize state flows
        userProgress = repository.userProgress.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allLessons = repository.allLessons.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allChallenges = repository.allChallenges.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed default database values
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val current = userProgress.value ?: UserProgress()
            repository.insertUserProgress(current.copy(hasCompletedOnboarding = true))
        }
    }

    fun togglePartnerView() {
        viewModelScope.launch {
            val current = userProgress.value ?: UserProgress()
            repository.insertUserProgress(current.copy(isPartnerView = !current.isPartnerView))
        }
    }

    fun updateProfile(name: String) {
        viewModelScope.launch {
            val current = userProgress.value ?: UserProgress()
            repository.insertUserProgress(current.copy(name = name))
        }
    }

    fun selectQuizAnswer(index: Int) {
        if (!_isAnswerChecked.value) {
            _selectedAnswerIndex.value = index
        }
    }

    fun submitQuizAnswer(correctIndex: Int) {
        val selected = _selectedAnswerIndex.value
        if (selected != null && !_isAnswerChecked.value) {
            _isAnswerChecked.value = true
            val correct = selected == correctIndex
            _isCorrect.value = correct
            if (correct) {
                // Award +30 XP
                addXp(30)
            }
        }
    }

    fun resetQuiz() {
        _selectedAnswerIndex.value = null
        _isAnswerChecked.value = false
        _isCorrect.value = null
    }

    fun addXp(amount: Int) {
        viewModelScope.launch {
            val current = userProgress.value ?: UserProgress()
            val newXp = current.xp + amount
            val levelUpThreshold = current.level * 400
            val (newLevel, finalXp) = if (newXp >= levelUpThreshold) {
                Pair(current.level + 1, newXp - levelUpThreshold)
            } else {
                Pair(current.level, newXp)
            }
            repository.insertUserProgress(current.copy(level = newLevel, xp = finalXp))
        }
    }

    fun completeLesson(lessonId: String) {
        viewModelScope.launch {
            val lessons = allLessons.value
            val target = lessons.find { it.lessonId == lessonId }
            if (target != null) {
                repository.insertLesson(target.copy(progressPercent = 100, isCompleted = true))
                // Gain some default lesson XP if not already completed
                if (!target.isCompleted) {
                    addXp(30)
                }
            }
        }
    }

    fun submitChallengeProof(challengeId: String, description: String, photoPath: String) {
        viewModelScope.launch {
            val challenges = allChallenges.value
            val target = challenges.find { it.challengeId == challengeId }
            if (target != null) {
                repository.insertChallenge(
                    target.copy(
                        isPendingValidation = true,
                        proofDescription = description,
                        proofPhotoPath = photoPath
                    )
                )
            }
        }
    }

    fun validateChallenge(challengeId: String) {
        viewModelScope.launch {
            val challenges = allChallenges.value
            val target = challenges.find { it.challengeId == challengeId }
            if (target != null && target.isPendingValidation) {
                repository.insertChallenge(
                    target.copy(
                        isCompleted = true,
                        isPendingValidation = false
                    )
                )
                // Gain challenge XP
                addXp(target.xpReward)
            }
        }
    }

    fun createNewChallenge(title: String, category: String, xpReward: Int, difficulty: String) {
        viewModelScope.launch {
            val id = "custom_" + System.currentTimeMillis()
            val newChallenge = CompletedChallenge(
                challengeId = id,
                title = title,
                category = category,
                duration = "Démarrer",
                xpReward = xpReward,
                difficulty = difficulty
            )
            repository.insertChallenge(newChallenge)
        }
    }
}
