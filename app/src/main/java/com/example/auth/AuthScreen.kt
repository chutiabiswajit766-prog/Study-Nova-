package com.example.auth

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanGlow
import com.example.ui.theme.NovaViolet

@Composable
fun AuthScreen(
  authManager: AuthManager,
  onAuthSuccess: () -> Unit,
  onContinueAsGuest: () -> Unit = {}
) {
  val context = LocalContext.current
  val activity = context as? Activity
  val scope = rememberCoroutineScope()
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            NavyDark,
            NavySurface,
            Color(0xFF0F172A)
          )
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 36.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Brand Logo Icon
      Box(
        modifier = Modifier
          .size(80.dp)
          .background(
            Brush.linearGradient(
              listOf(NovaBlue, NovaViolet)
            ),
            shape = CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "StudyNova AI Logo",
          tint = NovaCyanGlow,
          modifier = Modifier.size(44.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "StudyNova AI",
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        letterSpacing = 0.5.sp
      )

      Text(
        text = "Learn Smarter. Practice Better. Achieve More.",
        fontSize = 14.sp,
        color = NovaCyanGlow,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
      )

      // Value proposition feature list
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp),
        colors = CardDefaults.cardColors(
          containerColor = Color(0xFF1E293B).copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          FeatureRow(
            icon = Icons.Default.Psychology,
            title = "Personal AI Tutor & Generator",
            description = "Interactive explanations, step-by-step solutions & voice Q&A"
          )
          Spacer(modifier = Modifier.height(12.dp))
          FeatureRow(
            icon = Icons.Default.CameraAlt,
            title = "SnapStudy Smart Scanner",
            description = "Scan textbook pages, notes & diagrams for instant solutions"
          )
          Spacer(modifier = Modifier.height(12.dp))
          FeatureRow(
            icon = Icons.Default.Timer,
            title = "Timed Mock Exam Engine",
            description = "JEE, NEET, UPSC, Boards & SSC with accurate marking & analytics"
          )
          Spacer(modifier = Modifier.height(12.dp))
          FeatureRow(
            icon = Icons.Default.School,
            title = "Cloud Study Sync & Flashcards",
            description = "Spaced repetition revision synced seamlessly to your account"
          )
        }
      }

      if (errorMessage != null) {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
        ) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontSize = 13.sp,
            modifier = Modifier.padding(12.dp)
          )
        }
      }

      // Primary: Sign in with Google Button
      Button(
        onClick = {
          if (activity != null) {
            isLoading = true
            errorMessage = null
            authManager.signInWithGoogle(
              activity = activity,
              scope = scope,
              onSuccess = {
                isLoading = false
                onAuthSuccess()
              },
              onError = { err ->
                isLoading = false
                errorMessage = err
              },
              onCancelled = {
                isLoading = false
              }
            )
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("google_signin_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.White,
          contentColor = NavyDark
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = !isLoading
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = NovaBlue,
            strokeWidth = 2.5.dp
          )
        } else {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.School,
              contentDescription = null,
              tint = NovaBlue,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Sign in with Google",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = NavyDark
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Secondary: Continue as Guest
      OutlinedButton(
        onClick = onContinueAsGuest,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("guest_mode_button"),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = NovaCyanGlow
        ),
        border = ButtonDefaults.outlinedButtonBorder,
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(
          text = "Explore in Guest Mode",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Your data is secured with zero-trust cloud encryption.",
        fontSize = 12.sp,
        color = Color(0xFF94A3B8),
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun FeatureRow(
  icon: ImageVector,
  title: String,
  description: String
) {
  Row(verticalAlignment = Alignment.Top) {
    Surface(
      color = NovaBlue.copy(alpha = 0.3f),
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier.size(36.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = NovaCyan,
          modifier = Modifier.size(20.dp)
        )
      }
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
      )
      Text(
        text = description,
        color = Color(0xFF94A3B8),
        fontSize = 12.sp,
        lineHeight = 16.sp
      )
    }
  }
}
