package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiAiService
import com.example.auth.AuthManager
import com.example.auth.AuthScreen
import com.example.data.model.Flashcard
import com.example.data.model.MockTestResult
import com.example.data.model.StudyMaterial
import com.example.data.model.StudyPlan
import com.example.data.model.UserProfile
import com.example.data.repository.StudyRepository
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AiTutorScreen
import com.example.ui.screens.ExamPrepHubScreen
import com.example.ui.screens.FlashcardsScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MockTestScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SnapStudyScannerScreen
import com.example.ui.screens.StudyGeneratorScreen
import com.example.ui.screens.StudyLibraryScreen
import com.example.ui.screens.StudyPlannerScreen
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanGlow
import com.example.ui.theme.StudyNovaTheme
import com.example.util.configureAppCheck
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Step 1: Configure App Check with launch intent before any UI or Firebase operations
    configureAppCheck(this, intent)

    enableEdgeToEdge()

    setContent {
      StudyNovaTheme {
        StudyNovaApp()
      }
    }
  }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
  data object Home : Screen("home", "Home", Icons.Default.Home)
  data object Generator : Screen("generator", "Generator", Icons.Default.AutoAwesome)
  data object Scanner : Screen("scanner", "Scanner", Icons.Default.CameraAlt)
  data object Tutor : Screen("tutor", "Tutor", Icons.Default.Psychology)
  data object MockTest : Screen("mock_test", "Mock Test", Icons.Default.Timer)
  data object ExamPrep : Screen("exam_prep", "Prep Hub", Icons.Default.School)
  data object Flashcards : Screen("flashcards", "Flashcards", Icons.Default.Style)
  data object Planner : Screen("planner", "Planner", Icons.Default.School)
  data object Library : Screen("library", "Library", Icons.Default.Bookmark)
  data object Analytics : Screen("analytics", "Analytics", Icons.Default.Insights)
  data object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyNovaApp() {
  val context = androidx.compose.ui.platform.LocalContext.current
  val scope = rememberCoroutineScope()

  val authManager = remember { AuthManager(context) }
  val currentUser by authManager.currentUser.collectAsState()

  val repository = remember { StudyRepository(context) }
  val aiService = remember { GeminiAiService() }

  var isGuestMode by remember { mutableStateOf(false) }
  var currentRoute by remember { mutableStateOf<String>("home") }
  var initialTopicForGenerator by remember { mutableStateOf("") }

  // Silent auto sign-in on start
  LaunchedEffect(Unit) {
    authManager.attemptAutoSignIn(scope)
  }

  // Reactive state from Firestore when authenticated
  var userProfile by remember { mutableStateOf<UserProfile?>(null) }
  val studyMaterials = remember { mutableStateListOf<StudyMaterial>() }
  val flashcards = remember { mutableStateListOf<Flashcard>() }
  val mockResults = remember { mutableStateListOf<MockTestResult>() }
  var studyPlan by remember { mutableStateOf<StudyPlan?>(null) }

  // Observe Firestore collections once signed in
  LaunchedEffect(currentUser) {
    val uid = currentUser?.uid
    if (uid != null) {
      launch {
        repository.observeUserProfile(uid).collect { profile ->
          userProfile = profile ?: UserProfile(userId = uid, displayName = currentUser?.displayName ?: "Scholar")
        }
      }
      launch {
        repository.observeStudyMaterials(uid).collect { list ->
          studyMaterials.clear()
          studyMaterials.addAll(list)
        }
      }
      launch {
        repository.observeFlashcards(uid).collect { list ->
          flashcards.clear()
          flashcards.addAll(list)
        }
      }
      launch {
        repository.observeMockResults(uid).collect { list ->
          mockResults.clear()
          mockResults.addAll(list)
        }
      }
      launch {
        repository.observeStudyPlan(uid).collect { plan ->
          studyPlan = plan
        }
      }
    }
  }

  // Handle system back navigation
  BackHandler(enabled = currentRoute != "home") {
    currentRoute = "home"
  }

  // Auth Gate: If not signed in and not in guest mode, show AuthScreen
  if (currentUser == null && !isGuestMode) {
    AuthScreen(
      authManager = authManager,
      onAuthSuccess = {
        isGuestMode = false
      },
      onContinueAsGuest = {
        isGuestMode = true
        userProfile = UserProfile(displayName = "Guest Scholar", selectedExam = "JEE Main & Advanced")
      }
    )
    return
  }

  val navItems = listOf(
    Screen.Home,
    Screen.Generator,
    Screen.Scanner,
    Screen.Tutor,
    Screen.MockTest,
    Screen.ExamPrep,
    Screen.Library,
    Screen.Analytics,
    Screen.Profile
  )

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      if (currentUser == null && isGuestMode) {
        // Guest mode banner reminder
        Surface(
          color = Color(0xFFFEF3C7),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Guest Mode (Local Only)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF92400E)
            )
            Text(
              text = "Sign In with Google",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NovaBlue,
              modifier = Modifier.clickable { isGuestMode = false }
            )
          }
        }
      }
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
      ) {
        navItems.take(5).forEach { screen ->
          val isSelected = currentRoute == screen.route
          NavigationBarItem(
            icon = { Icon(screen.icon, contentDescription = screen.title) },
            label = { Text(screen.title, fontSize = 10.sp) },
            selected = isSelected,
            onClick = { currentRoute = screen.route },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NovaBlue,
              selectedTextColor = NovaBlue,
              indicatorColor = NovaBlue.copy(alpha = 0.12f)
            ),
            modifier = Modifier.testTag("nav_item_${screen.route}")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentRoute) {
        "home" -> {
          HomeDashboardScreen(
            userProfile = userProfile,
            onNavigate = { route ->
              currentRoute = route
            },
            onQuickGenerate = { topic ->
              initialTopicForGenerator = topic
              currentRoute = "generator"
            }
          )
        }

        "generator" -> {
          StudyGeneratorScreen(
            initialTopic = initialTopicForGenerator,
            aiService = aiService,
            onSaveToLibrary = { material ->
              if (currentUser != null) {
                scope.launch { repository.saveStudyMaterial(material) }
              } else {
                studyMaterials.add(material)
              }
            }
          )
        }

        "scanner" -> {
          SnapStudyScannerScreen(
            aiService = aiService,
            onSaveToLibrary = { material ->
              if (currentUser != null) {
                scope.launch { repository.saveStudyMaterial(material) }
              } else {
                studyMaterials.add(material)
              }
            }
          )
        }

        "tutor" -> {
          AiTutorScreen(
            aiService = aiService,
            examContext = userProfile?.selectedExam ?: "JEE Main & Advanced",
            languageContext = userProfile?.targetLanguage ?: "English"
          )
        }

        "mock_test" -> {
          MockTestScreen(
            aiService = aiService,
            onSaveResult = { result ->
              if (currentUser != null) {
                scope.launch { repository.saveMockResult(result) }
              } else {
                mockResults.add(result)
              }
            }
          )
        }

        "exam_prep" -> {
          ExamPrepHubScreen(
            onSelectTopicForGenerator = { topic ->
              initialTopicForGenerator = topic
              currentRoute = "generator"
            },
            onStartMockTest = {
              currentRoute = "mock_test"
            }
          )
        }

        "flashcards" -> {
          FlashcardsScreen(
            flashcards = flashcards,
            onAddFlashcard = { fc ->
              if (currentUser != null) {
                scope.launch { repository.saveFlashcard(fc) }
              } else {
                flashcards.add(fc)
              }
            },
            onRateCard = { cardId, newLvl, revCount ->
              if (currentUser != null) {
                scope.launch { repository.updateFlashcardReview(cardId, newLvl, revCount) }
              }
            }
          )
        }

        "planner" -> {
          StudyPlannerScreen(
            existingPlan = studyPlan,
            aiService = aiService,
            onSavePlan = { plan ->
              if (currentUser != null) {
                scope.launch { repository.saveStudyPlan(plan) }
              } else {
                studyPlan = plan
              }
            }
          )
        }

        "daily_challenge" -> {
          // Direct entry to daily speed challenge via mock test engine
          MockTestScreen(
            aiService = aiService,
            onSaveResult = { result ->
              if (currentUser != null) {
                scope.launch { repository.saveMockResult(result) }
              } else {
                mockResults.add(result)
              }
            }
          )
        }

        "library" -> {
          StudyLibraryScreen(
            materials = studyMaterials,
            onToggleFavorite = { material ->
              if (currentUser != null) {
                scope.launch { repository.toggleFavoriteMaterial(material) }
              }
            },
            onDeleteMaterial = { materialId ->
              if (currentUser != null) {
                scope.launch { repository.deleteStudyMaterial(currentUser!!.uid, materialId) }
              } else {
                studyMaterials.removeAll { it.materialId == materialId }
              }
            }
          )
        }

        "analytics" -> {
          AnalyticsScreen(
            userProfile = userProfile,
            mockResults = mockResults,
            onTargetedPractice = { topic ->
              initialTopicForGenerator = topic
              currentRoute = "generator"
            }
          )
        }

        "profile" -> {
          ProfileScreen(
            currentUser = currentUser,
            userProfile = userProfile,
            authManager = authManager,
            onUpdateProfile = { updated ->
              userProfile = updated
              if (currentUser != null) {
                scope.launch { repository.saveUserProfile(updated) }
              }
            },
            onSignOut = {
              isGuestMode = false
              currentRoute = "home"
            },
            onSignInRequired = {
              isGuestMode = false
            }
          )
        }
      }
    }
  }
}
