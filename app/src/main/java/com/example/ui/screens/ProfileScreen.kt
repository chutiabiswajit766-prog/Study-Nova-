package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.data.model.ExamRepository
import com.example.data.model.UserProfile
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaViolet
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  currentUser: FirebaseUser?,
  userProfile: UserProfile?,
  authManager: AuthManager,
  onUpdateProfile: (UserProfile) -> Unit,
  onSignOut: () -> Unit,
  onSignInRequired: () -> Unit
) {
  val context = LocalContext.current
  val activity = context as? Activity
  val scope = rememberCoroutineScope()

  var selectedExam by remember { mutableStateOf(userProfile?.selectedExam ?: "JEE Main & Advanced") }
  var selectedLanguage by remember { mutableStateOf(userProfile?.targetLanguage ?: "English") }
  var isSavedNotice by remember { mutableStateOf(false) }

  val languages = listOf("English", "Hindi", "Hinglish", "Assamese")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(NovaBlue)
        .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
      Text(
        text = "Profile & Settings",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = "Personalization, language & account management",
        fontSize = 12.sp,
        color = Color(0xFFBAE6FD)
      )
    }

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
      // User Account Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .background(NovaBlue.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AccountCircle,
              contentDescription = null,
              tint = NovaBlue,
              modifier = Modifier.size(42.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = currentUser?.displayName ?: userProfile?.displayName ?: "Guest Student",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          Text(
            text = currentUser?.email ?: "Guest Mode (Local Only)",
            fontSize = 12.sp,
            color = Color.Gray
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Stats badge row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Total Points", fontSize = 11.sp, color = Color.Gray)
              Text("${userProfile?.points ?: 100} XP", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NovaBlue)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Study Streak", fontSize = 11.sp, color = Color.Gray)
              Text("${userProfile?.streakDays ?: 1} Days", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFF97316))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Rank Level", fontSize = 11.sp, color = Color.Gray)
              Text("Level ${userProfile?.level ?: 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NovaViolet)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          if (currentUser != null) {
            OutlinedButton(
              onClick = {
                authManager.signOut(scope) {
                  onSignOut()
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("sign_out_button"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Sign Out")
            }
          } else {
            Button(
              onClick = onSignInRequired,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("login_from_profile_button"),
              colors = ButtonDefaults.buttonColors(containerColor = NovaBlue),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Sign in with Google")
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Preferences Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Learning Preferences", fontWeight = FontWeight.Bold, fontSize = 15.sp)

          Spacer(modifier = Modifier.height(14.dp))

          // Exam Dropdown
          var examExp by remember { mutableStateOf(false) }
          ExposedDropdownMenuBox(
            expanded = examExp,
            onExpandedChange = { examExp = !examExp },
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = selectedExam,
              onValueChange = {},
              readOnly = true,
              label = { Text("Active Exam Target") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExp) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(
              expanded = examExp,
              onDismissRequest = { examExp = false }
            ) {
              ExamRepository.supportedExams.forEach { exam ->
                DropdownMenuItem(
                  text = { Text(exam.name) },
                  onClick = {
                    selectedExam = exam.name
                    examExp = false
                  }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Language Dropdown
          var langExp by remember { mutableStateOf(false) }
          ExposedDropdownMenuBox(
            expanded = langExp,
            onExpandedChange = { langExp = !langExp },
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = selectedLanguage,
              onValueChange = {},
              readOnly = true,
              label = { Text("Study & Explanation Language") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExp) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(
              expanded = langExp,
              onDismissRequest = { langExp = false }
            ) {
              languages.forEach { lang ->
                DropdownMenuItem(
                  text = { Text(lang) },
                  onClick = {
                    selectedLanguage = lang
                    langExp = false
                  }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              val updated = (userProfile ?: UserProfile()).copy(
                selectedExam = selectedExam,
                targetLanguage = selectedLanguage
              )
              onUpdateProfile(updated)
              isSavedNotice = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (isSavedNotice) Color(0xFF10B981) else NovaBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(if (isSavedNotice) Icons.Default.Check else Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isSavedNotice) "Preferences Saved!" else "Save Preferences")
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Section 28: Freemium Tiers Architecture Preview
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Subscription Plan Tiers", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }

          Spacer(modifier = Modifier.height(10.dp))

          Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text("Current Tier: FREE", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NovaBlue)
              Text("• Full access to AI Study Generator & SnapStudy Scanner", fontSize = 12.sp, color = Color.Gray)
              Text("• Timed Mock Tests & Spaced Repetition Flashcards", fontSize = 12.sp, color = Color.Gray)
              Text("• Cloud Study Library and Voice Tutor", fontSize = 12.sp, color = Color.Gray)
              Spacer(modifier = Modifier.height(6.dp))
              Text("Note: Subscriptions are product tiers. Payment gateways will be enabled once billing integration is configured.", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
          }
        }
      }
    }
  }
}
