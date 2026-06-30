package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.CompletedChallenge
import com.example.data.CompletedLesson
import com.example.data.UserProgress
import com.example.ui.EcoViewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EcoApp()
            }
        }
    }
}

enum class EcoScreen {
    Onboarding,
    Dashboard,
    Learn,
    CourseDetails,
    Quiz,
    LessonSuccess,
    Challenges,
    ChallengeDetail,
    ChallengeValidation,
    ChallengeSuccess,
    Rewards,
    Profile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoApp(viewModel: EcoViewModel = viewModel()) {
    val userProgress by viewModel.userProgress.collectAsStateWithLifecycle()
    val lessons by viewModel.allLessons.collectAsStateWithLifecycle()
    val challenges by viewModel.allChallenges.collectAsStateWithLifecycle()

    val backstack = remember { mutableStateListOf<EcoScreen>(EcoScreen.Onboarding) }
    val currentScreen = backstack.lastOrNull() ?: EcoScreen.Onboarding

    // Selected items for detail screens
    var selectedLessonId by remember { mutableStateOf("plastique") }
    var selectedChallengeId by remember { mutableStateOf("reduire_dechets") }

    // Dialog state for profile rename
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf("") }

    // Dialog state for creating sponsored challenge
    var showCreateChallengeDialog by remember { mutableStateOf(false) }
    var newChallengeTitle by remember { mutableStateOf("") }
    var newChallengeCategory by remember { mutableStateOf("") }
    var newChallengeXp by remember { mutableStateOf("100") }
    var newChallengeDifficulty by remember { mutableStateOf("Facile") }

    // Onboarding redirection or seeding check
    LaunchedEffect(userProgress) {
        userProgress?.let {
            if (it.hasCompletedOnboarding && backstack.size == 1 && backstack.first() == EcoScreen.Onboarding) {
                backstack.clear()
                backstack.add(EcoScreen.Dashboard)
            }
        }
    }

    // Back button handling
    BackHandler(enabled = backstack.size > 1) {
        backstack.removeAt(backstack.lastIndex)
    }

    fun navigateTo(screen: EcoScreen) {
        if (screen == EcoScreen.Dashboard || screen == EcoScreen.Learn || screen == EcoScreen.Challenges || screen == EcoScreen.Rewards || screen == EcoScreen.Profile) {
            // Clear backstack to tab level
            backstack.clear()
            backstack.add(screen)
        } else {
            backstack.add(screen)
        }
    }

    Scaffold(
        bottomBar = {
            // Hide bottom navigation on onboarding or splash success screens
            if (currentScreen != EcoScreen.Onboarding && 
                currentScreen != EcoScreen.ChallengeSuccess && 
                currentScreen != EcoScreen.LessonSuccess &&
                currentScreen != EcoScreen.Quiz
            ) {
                val isPartner = userProgress?.isPartnerView == true
                NavigationBar(
                    containerColor = EcoCardLowest,
                    windowInsets = WindowInsets.navigationBars,
                    tonalElevation = 8.dp
                ) {
                    if (isPartner) {
                        // Corporate RSE Navigation
                        NavigationBarItem(
                            selected = currentScreen == EcoScreen.Dashboard,
                            onClick = { navigateTo(EcoScreen.Dashboard) },
                            icon = { Icon(Icons.Default.List, contentDescription = "Tableau de bord") },
                            label = { Text("Tableau de bord", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EcoPrimary,
                                selectedTextColor = EcoPrimary,
                                indicatorColor = EcoPrimaryContainer.copy(alpha = 0.2f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == EcoScreen.Profile,
                            onClick = { navigateTo(EcoScreen.Profile) },
                            icon = { Icon(Icons.Default.Person, contentDescription = "RSE Profil") },
                            label = { Text("Profil RSE", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EcoPrimary,
                                selectedTextColor = EcoPrimary,
                                indicatorColor = EcoPrimaryContainer.copy(alpha = 0.2f)
                            )
                        )
                    } else {
                        // Standard Apprenant Navigation
                        NavigationBarItem(
                            selected = currentScreen == EcoScreen.Dashboard,
                            onClick = { navigateTo(EcoScreen.Dashboard) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Accueil") },
                            label = { Text("Accueil", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EcoPrimary,
                                selectedTextColor = EcoPrimary,
                                indicatorColor = EcoPrimaryContainer.copy(alpha = 0.2f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == EcoScreen.Learn || currentScreen == EcoScreen.CourseDetails,
                            onClick = { navigateTo(EcoScreen.Learn) },
                            icon = { Icon(Icons.Default.Menu, contentDescription = "Apprendre") },
                            label = { Text("Apprendre", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EcoPrimary,
                                selectedTextColor = EcoPrimary,
                                indicatorColor = EcoPrimaryContainer.copy(alpha = 0.2f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == EcoScreen.Challenges || currentScreen == EcoScreen.ChallengeDetail || currentScreen == EcoScreen.ChallengeValidation,
                            onClick = { navigateTo(EcoScreen.Challenges) },
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Défis") },
                            label = { Text("Défis", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EcoPrimary,
                                selectedTextColor = EcoPrimary,
                                indicatorColor = EcoPrimaryContainer.copy(alpha = 0.2f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == EcoScreen.Rewards,
                            onClick = { navigateTo(EcoScreen.Rewards) },
                            icon = { Icon(Icons.Filled.Star, contentDescription = "Récompenses") },
                            label = { Text("Récompenses", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EcoPrimary,
                                selectedTextColor = EcoPrimary,
                                indicatorColor = EcoPrimaryContainer.copy(alpha = 0.2f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == EcoScreen.Profile,
                            onClick = { navigateTo(EcoScreen.Profile) },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                            label = { Text("Profil", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EcoPrimary,
                                selectedTextColor = EcoPrimary,
                                indicatorColor = EcoPrimaryContainer.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EcoBackground)
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                EcoScreen.Onboarding -> {
                    OnboardingScreen(
                        onStartClick = {
                            viewModel.completeOnboarding()
                            navigateTo(EcoScreen.Dashboard)
                        }
                    )
                }

                EcoScreen.Dashboard -> {
                    val isPartner = userProgress?.isPartnerView == true
                    if (isPartner) {
                        CorporateDashboardScreen(
                            progress = userProgress ?: UserProgress(),
                            onCreateChallengeClick = { showCreateChallengeDialog = true }
                        )
                    } else {
                        StudentDashboardScreen(
                            progress = userProgress ?: UserProgress(),
                            lessons = lessons,
                            onLessonClick = { id ->
                                selectedLessonId = id
                                navigateTo(EcoScreen.CourseDetails)
                            },
                            onDailyChallengeClick = {
                                selectedChallengeId = "economiser_eau"
                                navigateTo(EcoScreen.ChallengeDetail)
                            }
                        )
                    }
                }

                EcoScreen.Learn -> {
                    LearnScreen(
                        lessons = lessons,
                        onLessonClick = { id ->
                            selectedLessonId = id
                            navigateTo(EcoScreen.CourseDetails)
                        }
                    )
                }

                EcoScreen.CourseDetails -> {
                    val lesson = lessons.find { it.lessonId == selectedLessonId }
                    if (lesson != null) {
                        CourseDetailsScreen(
                            lesson = lesson,
                            onBackClick = { backstack.removeAt(backstack.lastIndex) },
                            onLaunchQuizClick = {
                                viewModel.resetQuiz()
                                navigateTo(EcoScreen.Quiz)
                            }
                        )
                    }
                }

                EcoScreen.Quiz -> {
                    val selectedAnswer by viewModel.selectedAnswerIndex.collectAsStateWithLifecycle()
                    val isChecked by viewModel.isAnswerChecked.collectAsStateWithLifecycle()
                    val isCorrect by viewModel.isCorrect.collectAsStateWithLifecycle()

                    QuizScreen(
                        selectedAnswerIndex = selectedAnswer,
                        isAnswerChecked = isChecked,
                        isCorrect = isCorrect,
                        onAnswerSelect = { viewModel.selectQuizAnswer(it) },
                        onSubmitAnswer = { viewModel.submitQuizAnswer(1) }, // Option index 1 "Bouteille en plastique" is correct
                        onNextClick = {
                            viewModel.completeLesson(selectedLessonId)
                            navigateTo(EcoScreen.LessonSuccess)
                        }
                    )
                }

                EcoScreen.LessonSuccess -> {
                    val progress = userProgress ?: UserProgress()
                    LessonSuccessScreen(
                        progress = progress,
                        onGoToChallengeClick = {
                            selectedChallengeId = "reduire_dechets"
                            navigateTo(EcoScreen.ChallengeDetail)
                        },
                        onBackToLearnClick = {
                            navigateTo(EcoScreen.Learn)
                        }
                    )
                }

                EcoScreen.Challenges -> {
                    ChallengesListScreen(
                        challenges = challenges,
                        onChallengeClick = { id ->
                            selectedChallengeId = id
                            navigateTo(EcoScreen.ChallengeDetail)
                        }
                    )
                }

                EcoScreen.ChallengeDetail -> {
                    val challenge = challenges.find { it.challengeId == selectedChallengeId }
                    if (challenge != null) {
                        ChallengeDetailScreen(
                            challenge = challenge,
                            onBackClick = { backstack.removeAt(backstack.lastIndex) },
                            onAcceptClick = { navigateTo(EcoScreen.ChallengeValidation) }
                        )
                    }
                }

                EcoScreen.ChallengeValidation -> {
                    val challenge = challenges.find { it.challengeId == selectedChallengeId }
                    if (challenge != null) {
                        ChallengeValidationScreen(
                            challenge = challenge,
                            onBackClick = { backstack.removeAt(backstack.lastIndex) },
                            onSubmitClick = { desc, photo ->
                                viewModel.submitChallengeProof(selectedChallengeId, desc, photo)
                                navigateTo(EcoScreen.ChallengeSuccess)
                            }
                        )
                    }
                }

                EcoScreen.ChallengeSuccess -> {
                    val challenge = challenges.find { it.challengeId == selectedChallengeId }
                    ChallengeSuccessScreen(
                        onBackToHomeClick = {
                            if (challenge != null) {
                                viewModel.validateChallenge(challenge.challengeId)
                            }
                            navigateTo(EcoScreen.Dashboard)
                        }
                    )
                }

                EcoScreen.Rewards -> {
                    val progress = userProgress ?: UserProgress()
                    RewardsScreen(
                        progress = progress,
                        onContinueLearnClick = { navigateTo(EcoScreen.Learn) },
                        onSeeChallengesClick = { navigateTo(EcoScreen.Challenges) }
                    )
                }

                EcoScreen.Profile -> {
                    val progress = userProgress ?: UserProgress()
                    ProfileScreen(
                        progress = progress,
                        onTogglePartnerClick = { viewModel.togglePartnerView() },
                        onEditProfileClick = {
                            renameInput = progress.name
                            showRenameDialog = true
                        },
                        onSeeRewardsClick = { navigateTo(EcoScreen.Rewards) }
                    )
                }
            }
        }
    }

    // Modal dialog for renaming profile
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Modifier le profil") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Votre nom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(renameInput)
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoPrimary),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Modal dialog for corporate campaign RSE creation
    if (showCreateChallengeDialog) {
        AlertDialog(
            onDismissRequest = { showCreateChallengeDialog = false },
            title = { Text("Créer une nouvelle campagne RSE") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = newChallengeTitle,
                        onValueChange = { newChallengeTitle = it },
                        label = { Text("Titre de la campagne") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newChallengeCategory,
                        onValueChange = { newChallengeCategory = it },
                        label = { Text("Catégorie (ex: Déchets, Eau)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newChallengeXp,
                        onValueChange = { newChallengeXp = it },
                        label = { Text("Récompense XP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Difficulté :", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Facile", "Moyen", "Difficile").forEach { diff ->
                            FilterChip(
                                selected = newChallengeDifficulty == diff,
                                onClick = { newChallengeDifficulty = diff },
                                label = { Text(diff) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val xpInt = newChallengeXp.toIntOrNull() ?: 100
                        viewModel.createNewChallenge(
                            title = newChallengeTitle,
                            category = newChallengeCategory,
                            xpReward = xpInt,
                            difficulty = newChallengeDifficulty
                        )
                        showCreateChallengeDialog = false
                        newChallengeTitle = ""
                        newChallengeCategory = ""
                        newChallengeXp = "100"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoPrimary),
                    enabled = newChallengeTitle.isNotBlank() && newChallengeCategory.isNotBlank()
                ) {
                    Text("Lancer la campagne")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateChallengeDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

// ---------------------------------------------------------
// ONBOARDING SCREEN
// ---------------------------------------------------------
@Composable
fun OnboardingScreen(onStartClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Logo N'sailim-Eco
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = "Logo",
                tint = EcoPrimaryContainer,
                modifier = Modifier.size(36.dp)
            )
            Text(
                "N'sailim-Eco",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = EcoPrimary
            )
        }

        // Beautiful Illustration Wrapper
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(CircleShape)
                .background(EcoCardLow)
                .border(2.dp, EcoPrimaryContainer.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&q=80&w=400",
                contentDescription = "Illustration onboarding",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Floating +XP Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 24.dp)
                    .background(EcoCardLowest, RoundedCornerShape(12.dp))
                    .border(1.dp, EcoPrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Star",
                        tint = EcoSecondaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("+XP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Onboarding Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Faites Grandir Votre Monde.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = EcoOnBackground,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                "Rejoignez une communauté d'éco-champions. Apprenez, suivez votre impact et gagnez des récompenses pour un avenir plus vert.",
                fontSize = 16.sp,
                color = EcoOnSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // CTA Gamified Button
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onStartClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("commencer_onboarding_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Commencer", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "J'ai déjà un compte",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = EcoPrimary,
                modifier = Modifier
                    .clickable { onStartClick() }
                    .padding(8.dp)
            )
        }
    }
}

// ---------------------------------------------------------
// STUDENT DASHBOARD SCREEN (Accueil)
// ---------------------------------------------------------
@Composable
fun StudentDashboardScreen(
    progress: UserProgress,
    lessons: List<CompletedLesson>,
    onLessonClick: (String) -> Unit,
    onDailyChallengeClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome and Head Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=128",
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                    Column {
                        Text(
                            "Bonjour ${progress.name} 👋",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoOnSurface
                        )
                        Text(
                            "Prêt à augmenter votre éco-impact aujourd'hui ?",
                            fontSize = 12.sp,
                            color = EcoOnSurfaceVariant
                        )
                    }
                }
                // Star notifications toggle/badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, EcoOutlineVariant, CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = EcoOnSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }

        // Progression Hero Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EcoPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Niveau ${progress.level}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "XP Icon",
                                    tint = EcoSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "${progress.xp} XP",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Série de ${progress.streakDays} jours 🔥",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular Progress Graphic
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val progressPercent = (progress.xp.toFloat() / (progress.level * 400f)).coerceIn(0f, 1f)
                        Canvas(modifier = Modifier.size(110.dp)) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.2f),
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = EcoSecondaryContainer,
                                startAngle = -90f,
                                sweepAngle = progressPercent * 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            "${(progressPercent * 100).toInt()}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val xpNeeded = (progress.level * 400) - progress.xp
                        Text(
                            "Prochaine récompense dans $xpNeeded XP",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Mission Éco Du Jour (Daily Mission / Challenge)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🌱", fontSize = 18.sp)
                            Text(
                                "MISSION ÉCO DU JOUR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(EcoSecondaryContainer.copy(alpha = 0.2f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "+50 XP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoOnSecondaryContainer
                            )
                        }
                    }

                    Column {
                        Text(
                            "Économiser 10L d'eau",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoOnSurface
                        )
                        Text(
                            "Fermez le robinet pendant que vous vous brossez les dents et prenez une douche plus courte aujourd'hui.",
                            fontSize = 14.sp,
                            color = EcoOnSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Button(
                        onClick = onDailyChallengeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EcoPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("start_mission_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Commencer la mission", fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowForward, contentDescription = "Démarrer")
                        }
                    }
                }
            }
        }

        // Current Badge Tracker
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(EcoCardLow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏅", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Badge Éco Explorateur",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "75%",
                                style = MaterialTheme.typography.bodySmall,
                                color = EcoOnSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Linear progress indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(EcoCardHigh, shape = RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.75f)
                                    .background(EcoPrimaryContainer, shape = RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }

        // Continued Learning section
        item {
            Text(
                text = "Continuer l'apprentissage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EcoOnSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Course item 1
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Image mockup
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EcoCardLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("♻️", fontSize = 32.sp)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text("Déchets plastiques", style = MaterialTheme.typography.bodySmall, color = EcoOnSurfaceVariant)
                            Text("Réduction des déchets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .background(EcoCardHigh, shape = RoundedCornerShape(3.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(0.4f)
                                            .background(EcoPrimary, shape = RoundedCornerShape(3.dp))
                                    )
                                }
                                Text("il reste 10 min", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }

                // Course item 2
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoCardLowest)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EcoCardLow), 
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💧", fontSize = 32.sp)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text("Eau", style = MaterialTheme.typography.bodySmall, color = EcoOnSurfaceVariant)
                            Text("Préservation de l'eau", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .background(EcoCardHigh, shape = RoundedCornerShape(3.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(0.15f)
                                            .background(EcoPrimary, shape = RoundedCornerShape(3.dp))
                                    )
                                }
                                Text("il reste 25 min", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        }

        // Mini-jeu block lock
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoCardLow.copy(alpha = 0.75f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, EcoCardHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(EcoCardHighest, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Verrouillé", tint = EcoOnSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("🎮 Mini-jeu", fontWeight = FontWeight.Bold, color = EcoOnSurface)
                        Text("Débloquer au niveau 5", style = MaterialTheme.typography.bodySmall, color = EcoOnSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// LEARN / COURSES LIST SCREEN
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    lessons: List<CompletedLesson>,
    onLessonClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Rechercher des leçons...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = EcoCardLow,
                    unfocusedContainerColor = EcoCardLow,
                    focusedBorderColor = EcoPrimary,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(EcoCardLow, RoundedCornerShape(12.dp))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.List, contentDescription = "Filtrer", tint = EcoOnSurface)
            }
        }

        // Learning Paths
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(lessons) { lesson ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Category Image Placeholder
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EcoCardLow)
                            ) {
                                val imageModel = when (lesson.lessonId) {
                                    "plastique" -> "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?auto=format&fit=crop&q=80&w=256"
                                    "eau" -> "https://images.unsplash.com/photo-1518173946687-a4c8a383392e?auto=format&fit=crop&q=80&w=256"
                                    else -> "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&q=80&w=256"
                                }
                                AsyncImage(
                                    model = imageModel,
                                    contentDescription = lesson.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        lesson.category,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EcoOnSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(EcoSecondaryContainer.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "+${lesson.xpReward} XP",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EcoOnSecondaryContainer
                                        )
                                    }
                                }
                                Text(
                                    "Apprenez à gérer les ressources de manière éco-responsable.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EcoOnSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📚 ${lesson.duration}",
                                style = MaterialTheme.typography.bodySmall,
                                color = EcoOnSurfaceVariant
                            )
                            Text(
                                text = "⚡ Débutant",
                                style = MaterialTheme.typography.bodySmall,
                                color = EcoOnSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress line and button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .background(EcoOutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(lesson.progressPercent / 100f)
                                        .background(EcoPrimaryContainer, RoundedCornerShape(4.dp))
                                )
                            }
                            Text(
                                "${lesson.progressPercent}%",
                                fontWeight = FontWeight.Bold,
                                color = EcoPrimaryContainer,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onLessonClick(lesson.lessonId) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("continue_lesson_${lesson.lessonId}")
                        ) {
                            Text(
                                if (lesson.progressPercent == 0) "Commencer le Parcours" else "Continuer la Leçon",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Recommended block
            item {
                Text(
                    "Recommandé pour vous",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = EcoOnSurface,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Rec item 1
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { },
                        colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EcoCardLow)
                            ) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1502082553048-f009c37129b9?auto=format&fit=crop&q=80&w=150",
                                    contentDescription = "Bouteille",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("🕒 3m", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                "La Vie d'une Bouteille",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text("+50 XP", fontSize = 10.sp, color = EcoPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Rec item 2
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { },
                        colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EcoCardLow)
                            ) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&q=80&w=150",
                                    contentDescription = "Douche",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("🕒 5m", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                "Défi Douche 5 Mins",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text("+75 XP", fontSize = 10.sp, color = EcoPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// COURSE DETAILS SCREEN (Détails du cours)
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailsScreen(
    lesson: CompletedLesson,
    onBackClick: () -> Unit,
    onLaunchQuizClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    lesson.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = EcoSurface),
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Favorite, contentDescription = "Favoris", tint = EcoPrimary)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Image
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?auto=format&fit=crop&q=80&w=600",
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Text(
                                "Gestion des déchets",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoPrimary
                            )
                        }
                    }
                }
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🕒", fontSize = 16.sp)
                        Text("Lecture de 5 min", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("⭐", fontSize = 16.sp)
                        Text("+30 XP", fontSize = 12.sp, color = EcoSecondaryContainer)
                    }
                }
            }

            // Course content
            item {
                Text(
                    text = "Apprenez à gérer efficacement les déchets ménagers et à réduire votre empreinte plastique grâce à ces étapes pratiques.",
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 26.sp,
                    color = EcoOnSurface
                )
            }

            // Interactive lesson units list
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val units = listOf(
                        Pair("Les 3 R", "Réduire, Réutiliser, Recycler - la base de la gestion des déchets."),
                        Pair("Tri des déchets", "Séparer correctement les matières recyclables du compost et des ordures."),
                        Pair("Compostage", "Transformez les déchets de cuisine en un sol riche en nutriments.")
                    )
                    units.forEachIndexed { idx, unit ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(EcoPrimaryContainer.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${idx + 1}", fontWeight = FontWeight.Bold, color = EcoPrimary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(unit.first, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Text(
                                    unit.second,
                                    fontSize = 14.sp,
                                    color = EcoOnSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Key takeaways
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EcoCardLow, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Points clés à retenir",
                            fontWeight = FontWeight.Bold,
                            color = EcoPrimary,
                            fontSize = 16.sp
                        )
                        Text("• De petites habitudes conduisent à de grands changements.", fontSize = 14.sp)
                        Text("• Le plastique met des siècles à se décomposer.", fontSize = 14.sp)
                        Text("• Le tri est la première étape vers le recyclage.", fontSize = 14.sp)
                    }
                }
            }

            // CTA Button to Launch Quiz
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onLaunchQuizClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("lancer_quiz_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Lancer le Quiz", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Icon(Icons.Default.ArrowForward, contentDescription = "Suivant")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Complétez le quiz pour gagner de l'XP et débloquer du nouveau contenu.",
                        fontSize = 12.sp,
                        color = EcoOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// QUIZ SCREEN
// ---------------------------------------------------------
@Composable
fun QuizScreen(
    selectedAnswerIndex: Int?,
    isAnswerChecked: Boolean,
    isCorrect: Boolean?,
    onAnswerSelect: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Quiz Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.clickable { onNextClick() })
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .height(10.dp)
                    .background(EcoOutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .background(EcoPrimaryContainer, RoundedCornerShape(5.dp))
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "XP", tint = EcoSecondaryContainer, modifier = Modifier.size(16.dp))
                Text("12", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Lequel de ces objets du quotidien met le plus de temps à se décomposer dans une décharge ?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = EcoOnSurface,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large beautiful recycling visual
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?auto=format&fit=crop&q=80&w=400",
                contentDescription = "Bouteille plastique",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quiz options
        val options = listOf("Peau de banane", "Bouteille en plastique", "T-shirt en coton", "Sac en papier")
        val emojis = listOf("🍌", "🧴", "👕", "🛍️")

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            options.forEachIndexed { idx, option ->
                val isSelected = selectedAnswerIndex == idx
                val borderBrushColor = if (isAnswerChecked && idx == 1) {
                    EcoPrimaryContainer
                } else if (isSelected) {
                    EcoPrimary
                } else {
                    EcoOutlineVariant.copy(alpha = 0.4f)
                }

                val backgroundColor = if (isAnswerChecked && idx == 1) {
                    EcoPrimaryContainer.copy(alpha = 0.15f)
                } else if (isSelected) {
                    EcoCardLow
                } else {
                    EcoCardLowest
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, borderBrushColor),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAnswerSelect(idx) }
                        .testTag("quiz_option_$idx")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(emojis[idx], fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                            Text(
                                option,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) EcoPrimary else EcoOnSurface,
                                fontSize = 16.sp
                            )
                        }
                        if (isAnswerChecked && idx == 1) {
                            Icon(Icons.Default.Check, contentDescription = "Correct", tint = EcoPrimaryContainer)
                        }
                    }
                }
            }
        }

        // Action / Verification Panel
        if (selectedAnswerIndex != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                if (!isAnswerChecked) {
                    Button(
                        onClick = onSubmitAnswer,
                        colors = ButtonDefaults.buttonColors(containerColor = EcoPrimary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("submit_quiz_button")
                    ) {
                        Text("Vérifier", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Correct layout shown
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCorrect == true) "🎉 Correct !" else "Faux. La bonne réponse est : Bouteille en plastique",
                                color = if (isCorrect == true) EcoPrimaryContainer else Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (isCorrect == true) {
                                Box(
                                    modifier = Modifier
                                        .background(EcoSecondaryContainer.copy(alpha = 0.2f), CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("+30 XP", fontWeight = FontWeight.Bold, color = EcoOnSecondaryContainer)
                                }
                            }
                        }
                        Button(
                            onClick = onNextClick,
                            colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("next_quiz_button")
                        ) {
                            Text("Suivant", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// LESSON SUCCESS SCREEN (Félicitations)
// ---------------------------------------------------------
@Composable
fun LessonSuccessScreen(
    progress: UserProgress,
    onGoToChallengeClick: () -> Unit,
    onBackToLearnClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Trophy illustration
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(EcoCardLow),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1578269174936-2709b5a8e0f3?auto=format&fit=crop&q=80&w=256",
                contentDescription = "Trophy Success",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🎉 Félicitations !",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = EcoOnSurface
            )
            Text(
                "Vous avez terminé cette leçon avec succès.",
                fontSize = 16.sp,
                color = EcoOnSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Level details block
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Niveau ${progress.level}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Box(
                        modifier = Modifier
                            .background(EcoSecondaryContainer, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Gagné +30 XP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = EcoOnSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(EcoOutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.83f) // simulated Level Progress
                            .background(EcoPrimaryContainer, RoundedCornerShape(5.dp))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1250 / 1500 XP", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                    Text("250 XP avant le Niveau 5", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(EcoCardLow, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("🔥", fontSize = 14.sp)
                    Text("Série de ${progress.streakDays} jours", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Lock info
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoCardLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Lock", tint = EcoPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Plus que 20 XP pour débloquer votre prochain badge.", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Atteignez le niveau 5 pour débloquer le mini-jeu.", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onGoToChallengeClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("action_go_challenge_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Passer au défi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Icon(Icons.Default.ArrowForward, contentDescription = "Suivant")
                }
            }

            OutlinedButton(
                onClick = onBackToLearnClick,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, EcoPrimaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Retour à l'apprentissage", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EcoPrimaryContainer)
            }
        }
    }
}

// ---------------------------------------------------------
// CHALLENGES LIST SCREEN (Défis)
// ---------------------------------------------------------
@Composable
fun ChallengesListScreen(
    challenges: List<CompletedChallenge>,
    onChallengeClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Défis écologiques",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = EcoOnSurface
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(challenges) { challenge ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChallengeClick(challenge.challengeId) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(EcoPrimaryContainer.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (challenge.challengeId.startsWith("custom_")) "💡" else "♻️",
                                    fontSize = 28.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        challenge.category,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = EcoOnSurfaceVariant
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(EcoSecondaryContainer, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "+${challenge.xpReward} XP",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EcoOnSecondaryContainer
                                        )
                                    }
                                }
                                Text(
                                    challenge.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = EcoOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            if (challenge.isCompleted) EcoPrimaryContainer 
                                            else if (challenge.isPendingValidation) EcoSecondaryContainer 
                                            else EcoOutlineVariant, 
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = if (challenge.isCompleted) "Validé" 
                                           else if (challenge.isPendingValidation) "En attente" 
                                           else "Non commencé",
                                    fontSize = 12.sp,
                                    color = EcoOnSurfaceVariant
                                )
                            }
                            Text(
                                text = "Difficulté: ${challenge.difficulty}",
                                fontSize = 12.sp,
                                color = EcoOnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// CHALLENGE DETAIL SCREEN (Détail du défi)
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailScreen(
    challenge: CompletedChallenge,
    onBackClick: () -> Unit,
    onAcceptClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Défi Éco", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = EcoSurface),
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero card visual
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?auto=format&fit=crop&q=80&w=600",
                            contentDescription = "Recyclage",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Specs Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.background(EcoCardLow, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(challenge.difficulty, fontWeight = FontWeight.Bold, color = EcoPrimary, fontSize = 12.sp)
                    }
                    Box(modifier = Modifier.background(EcoCardLow, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(challenge.duration, fontWeight = FontWeight.Bold, color = EcoPrimary, fontSize = 12.sp)
                    }
                    Box(modifier = Modifier.background(EcoSecondaryContainer, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("+${challenge.xpReward} XP", fontWeight = FontWeight.Bold, color = EcoOnSecondaryContainer, fontSize = 12.sp)
                    }
                }
            }

            // Title block
            item {
                Text(challenge.title, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = EcoOnSurface)
                Text(
                    "Accomplissez cette action écologique simple dans votre vie quotidienne pour impacter positivement votre communauté.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EcoOnSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Conditions
            item {
                Text("Conditions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoOnSurface)
                Card(
                    colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Check, contentDescription = "Check", tint = EcoPrimaryContainer)
                            Text("Récoltez au moins 5 bouteilles en plastique.", fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Check, contentDescription = "Check", tint = EcoPrimaryContainer)
                            Text("Placez-les dans la bonne poubelle de recyclage.", fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Check, contentDescription = "Check", tint = EcoPrimaryContainer)
                            Text("Prenez une photo comme preuve de validation.", fontSize = 14.sp)
                        }
                    }
                }
            }

            // Useful tips
            item {
                Text("Conseils utiles", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoOnSurface)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EcoCardHigh, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💡", fontSize = 16.sp)
                            Text("Vérifiez les symboles de recyclage locaux pour assurer un bon tri.", fontSize = 14.sp, color = EcoOnSurface)
                        }
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💧", fontSize = 16.sp)
                            Text("Rincez d'abord les bouteilles pour éviter la contamination.", fontSize = 14.sp, color = EcoOnSurface)
                        }
                    }
                }
            }

            // CTA Action Buttons
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (challenge.isCompleted) {
                        Button(
                            onClick = {},
                            enabled = false,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Défi déjà validé !", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onAcceptClick,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("relever_defi_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Relever le défi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Icon(Icons.Default.ArrowForward, contentDescription = "Accept")
                            }
                        }
                        Button(
                            onClick = onBackClick,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoCardLowest),
                            border = BorderStroke(2.dp, EcoPrimaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Sauvegarder pour plus tard", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EcoPrimaryContainer)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// CHALLENGE VALIDATION SCREEN (Validation / Preuve)
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeValidationScreen(
    challenge: CompletedChallenge,
    onBackClick: () -> Unit,
    onSubmitClick: (String, String) -> Unit
) {
    var descriptionInput by remember { mutableStateOf("") }
    var mockPhotoUploaded by remember { mutableStateOf(false) }

    // Checkbox checklist variables
    var checkPhoto by remember { mutableStateOf(false) }
    var checkRequirements by remember { mutableStateOf(false) }
    var checkDesc by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Validation du défi", fontWeight = FontWeight.Bold, color = EcoPrimary) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = EcoSurface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Challenge summary bento box
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(EcoPrimaryContainer.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♻️", fontSize = 28.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(EcoSecondaryContainer, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("+${challenge.xpReward} XP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EcoOnSecondaryContainer)
                            }
                            Text(challenge.difficulty, fontSize = 12.sp, color = EcoOnSurfaceVariant)
                        }
                        Text(challenge.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EcoOnSurface)
                    }
                }
            }

            // Photo Upload
            Text("Envoyer la preuve", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoOnSurface)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(2.dp, EcoOutlineVariant, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (mockPhotoUploaded) EcoCardLow else EcoCardLowest)
                    .clickable { mockPhotoUploaded = true },
                contentAlignment = Alignment.Center
            ) {
                if (mockPhotoUploaded) {
                    // Show mock preview
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?auto=format&fit=crop&q=80&w=400",
                        contentDescription = "Uploaded Proof",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Changer de photo", color = Color.White, fontSize = 12.sp)
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Upload", tint = EcoPrimary, modifier = Modifier.size(36.dp))
                        Text("Télécharger une photo", fontWeight = FontWeight.Bold, color = EcoOnSurface)
                        Text("Prend en charge JPG, PNG (Max 5Mo)", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                    }
                }
            }

            // Description text area
            Text("Dites-nous ce que vous avez fait", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoOnSurface)
            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                placeholder = { Text("Décrivez votre action écologique...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("challenge_desc_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Checklist
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, EcoOutlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Avant de soumettre", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EcoOnSurface)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { checkPhoto = !checkPhoto }) {
                        Icon(
                            imageVector = if (checkPhoto) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = "Status",
                            tint = if (checkPhoto) EcoPrimaryContainer else EcoOutline
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Photo claire montrant l'action", color = EcoOnSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { checkRequirements = !checkRequirements }) {
                        Icon(
                            imageVector = if (checkRequirements) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = "Status",
                            tint = if (checkRequirements) EcoPrimaryContainer else EcoOutline
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Exigences du défi entièrement respectées", color = EcoOnSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { checkDesc = !checkDesc }) {
                        Icon(
                            imageVector = if (checkDesc) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = "Status",
                            tint = if (checkDesc) EcoPrimaryContainer else EcoOutline
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Brève description ajoutée", color = EcoOnSurfaceVariant)
                    }
                }
            }

            // CTAs
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val photoPath = if (mockPhotoUploaded) "mock_path_to_recycling.jpg" else null
                        onSubmitClick(descriptionInput, photoPath ?: "empty")
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EcoPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("validate_challenge_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Validation du défi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Icon(Icons.Default.Send, contentDescription = "Submit")
                    }
                }

                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, EcoPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Annuler", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EcoPrimary)
                }
            }
        }
    }
}

// ---------------------------------------------------------
// CHALLENGE SUCCESS SCREEN (Défi envoyé !)
// ---------------------------------------------------------
@Composable
fun ChallengeSuccessScreen(
    onBackToHomeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Success illustration (Diverse African individual holding glowing plant)
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(EcoCardLow),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&q=80&w=256",
                contentDescription = "Success",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🎉 Défi envoyé !",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = EcoPrimary
            )
            Text(
                "Votre soumission est en cours d'examen. Vous recevrez vos XP une fois validée.",
                fontSize = 16.sp,
                color = EcoOnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp).padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Level Details Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Niveau 4", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Box(
                        modifier = Modifier
                            .background(EcoSecondaryContainer, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("1 250 XP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EcoOnSecondaryContainer)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(EcoOutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.75f)
                            .background(EcoPrimaryContainer, RoundedCornerShape(5.dp))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Palier actuel : Jeune pousse", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                    Text("Suivant : Arbrisseau", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pending state indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EcoCardLow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(EcoCardHighest, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏳", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("En attente de validation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("+200 XP après approbation", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // CTA Back button
        Button(
            onClick = onBackToHomeClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("back_to_home_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = "Home")
                Text("Retour à l'accueil", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ---------------------------------------------------------
// REWARDS SCREEN (Récompenses)
// ---------------------------------------------------------
@Composable
fun RewardsScreen(
    progress: UserProgress,
    onContinueLearnClick: () -> Unit,
    onSeeChallengesClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text("Mes Récompenses", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = EcoOnSurface)
        }

        // Current status card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Statut actuel", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                            Text("Niveau ${progress.level}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = EcoPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .background(EcoSecondaryContainer, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("1 250 XP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EcoOnSecondaryContainer)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(EcoOutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.83f)
                                .background(EcoPrimaryContainer, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }

        // Badges Section
        item {
            Text("Mes Badges", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoOnSurface)
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val badges = listOf(
                    Triple("Apprenti Éco", "Première leçon terminée", "♻️"),
                    Triple("Planteur d'Arbres", "Terminer 5 défis", "🌲"),
                    Triple("Guerrier des Déchets", "Finir le cours sur le plastique", "🗑️")
                )
                badges.forEach { badge ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(EcoCardLow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(badge.third, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(badge.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(badge.second, fontSize = 12.sp, color = EcoOnSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Mini Games Section
        item {
            Text("Mini-Jeux", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoOnSurface)
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Game 1
                Card(
                    colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎮", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Trier les Déchets", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Débloqué", fontSize = 12.sp, color = EcoPrimaryContainer)
                            }
                        }
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer)
                        ) {
                            Text("Jouer")
                        }
                    }
                }
            }
        }

        // Partner Rewards Store Section
        item {
            Text("Récompenses partenaires", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoOnSurface)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Partner reward 1
                Card(
                    modifier = Modifier.width(200.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EcoCardLow)
                        ) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&q=80&w=256",
                                contentDescription = "Gourde",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text("Gourde en Bambou", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                        Text("2 000 XP", color = EcoSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Échanger", fontSize = 12.sp)
                        }
                    }
                }

                // Partner reward 2
                Card(
                    modifier = Modifier.width(200.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EcoCardLow)
                        ) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1586075010923-2dd4570fb338?auto=format&fit=crop&q=80&w=256",
                                contentDescription = "Papeterie",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text("Set de Papeterie Recyclé", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                        Text("1 500 XP", color = EcoSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Échanger", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Prochain But Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EcoCardLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🎯 Prochain But", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoPrimary)
                    Text("Plus que 120 XP avant votre prochaine récompense.", fontSize = 14.sp, color = EcoOnSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onContinueLearnClick, colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer)) {
                            Text("Continuer d'apprendre")
                        }
                        Button(onClick = onSeeChallengesClick, colors = ButtonDefaults.buttonColors(containerColor = EcoPrimary)) {
                            Text("Voir les défis")
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// PROFIL SCREEN
// ---------------------------------------------------------
@Composable
fun ProfileScreen(
    progress: UserProgress,
    onTogglePartnerClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSeeRewardsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Mon Profil", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = EcoOnSurface)

        // Main User Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar Image
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=256",
                    contentDescription = "Avatar Amani",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(progress.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("Membre depuis : ${progress.memberSince}", fontSize = 12.sp, color = EcoOnSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.background(EcoPrimaryContainer.copy(alpha = 0.2f), CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("Niveau ${progress.level}", fontWeight = FontWeight.Bold, color = EcoPrimary, fontSize = 12.sp)
                    }
                    Box(modifier = Modifier.background(EcoSecondaryContainer, CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("${progress.xp} XP", fontWeight = FontWeight.Bold, color = EcoOnSecondaryContainer, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onEditProfileClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EcoPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("edit_profile_button")
                    ) {
                        Text("Modifier", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onSeeRewardsClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Récompenses", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Stats columns
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("12", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoPrimary)
                    Text("Leçons", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("5", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoPrimary)
                    Text("Défis", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("8", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoPrimary)
                    Text("Badges", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("2", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoPrimary)
                    Text("Jeux", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                }
            }
        }

        // Activité récente (Timeline)
        Text("Activité récente", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoOnSurface)
        Card(
            colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf(
                    Pair("Défi soumis", "Il y a 2h"),
                    Pair("Quiz réussi : Déchets plastiques", "Hier"),
                    Pair("Badge débloqué : Apprenti éco", "Il y a 3 jours")
                ).forEach { activity ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(EcoPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(activity.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(activity.second, fontSize = 12.sp, color = EcoOnSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Brand / Partner Toggle Option
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EcoCardLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🛠️ Vue Corporate RSE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = EcoPrimary
                )
                Text(
                    text = "Si vous êtes un partenaire RSE d'entreprise, vous pouvez basculer vers le tableau de bord de visibilité et de gestion de campagnes.",
                    fontSize = 12.sp,
                    color = EcoOnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onTogglePartnerClick,
                    colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer),
                    modifier = Modifier.testTag("toggle_partner_view_button")
                ) {
                    Text(if (progress.isPartnerView) "Retourner à l'Apprenant" else "Basculer vers RSE Partenaire")
                }
            }
        }
    }
}

// ---------------------------------------------------------
// CORPORATE RSE PARTNER DASHBOARD SCREEN (EcoPulse CSR)
// ---------------------------------------------------------
@Composable
fun CorporateDashboardScreen(
    progress: UserProgress,
    onCreateChallengeClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // RSE Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "RSE Logo",
                        tint = EcoPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        "EcoPulse CSR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = EcoPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&q=80&w=128",
                        contentDescription = "RSE Admin",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Bento Grid Metrics
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Votre Impact RSE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = EcoOnSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.background(EcoCardLow, RoundedCornerShape(12.dp)).padding(12.dp).fillMaxWidth()) {
                                Column {
                                    Text("Campagnes actives", fontSize = 11.sp, color = EcoOnSurfaceVariant)
                                    Text("5", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EcoPrimary)
                                }
                            }
                            Box(modifier = Modifier.background(EcoCardLow, RoundedCornerShape(12.dp)).padding(12.dp).fillMaxWidth()) {
                                Column {
                                    Text("Leçons terminées", fontSize = 11.sp, color = EcoOnSurfaceVariant)
                                    Text("8.2k", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EcoPrimary)
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.background(EcoCardLow, RoundedCornerShape(12.dp)).padding(12.dp).fillMaxWidth()) {
                                Column {
                                    Text("Jeunes touchés", fontSize = 11.sp, color = EcoOnSurfaceVariant)
                                    Text("12.5k", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EcoPrimary)
                                }
                            }
                            Box(modifier = Modifier.background(EcoCardLow, RoundedCornerShape(12.dp)).padding(12.dp).fillMaxWidth()) {
                                Column {
                                    Text("Engagement", fontSize = 11.sp, color = EcoOnSurfaceVariant)
                                    Text("78%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EcoPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Campaign manager actions
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(EcoPrimaryContainer.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("➕", fontSize = 22.sp)
                        }
                        Column {
                            Text("Créer une nouvelle campagne", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Lancer une nouvelle initiative RSE pour la communauté.", fontSize = 12.sp, color = EcoOnSurfaceVariant)
                        }
                    }

                    Button(
                        onClick = onCreateChallengeClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoPrimaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Commencer un brouillon", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sponsored rewards list
        item {
            Text("Récompenses RSE Sponsorisées", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoOnSurface)
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    Triple("Gourde éco-responsable", "150 / 200 réclamées", 0.75f),
                    Triple("Ensemble de papeterie en bambou", "85 / 100 réclamés", 0.85f)
                ).forEach { reward ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(EcoCardLow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎁", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(reward.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .background(EcoCardHigh, RoundedCornerShape(3.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(reward.third)
                                                .background(EcoPrimaryContainer, RoundedCornerShape(3.dp))
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(reward.second, fontSize = 11.sp, color = EcoOnSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Simple Impact chart
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EcoCardLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Aperçu de l'engagement mensuel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Column bar chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        listOf(0.4f, 0.55f, 0.7f, 0.9f).forEachIndexed { index, weight ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .fillMaxHeight(weight)
                                        .background(EcoPrimaryContainer.copy(alpha = if (index == 3) 1f else 0.4f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                )
                                Text(
                                    text = listOf("Jan", "Fév", "Mar", "Avr")[index],
                                    fontSize = 11.sp,
                                    color = EcoOnSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
