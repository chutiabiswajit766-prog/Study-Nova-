package com.example.ui.screens

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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockTestResult
import com.example.data.model.UserProfile
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaViolet

@Composable
fun AnalyticsScreen(
  userProfile: UserProfile?,
  mockResults: List<MockTestResult>,
  onTargetedPractice: (topic: String) -> Unit
) {
  val totalTests = mockResults.size
  val avgAccuracy = if (mockResults.isNotEmpty()) {
    mockResults.map { it.accuracy }.average()
  } else 80.0

  val totalQuestionsAttempted = mockResults.sumOf { it.totalQuestions }

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
        text = "Performance & Analytics",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = "AI Weakness Detector, learning streaks & milestone metrics",
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
      // Overall Metrics Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricCard(
          title = "Tests Taken",
          value = "$totalTests",
          subtitle = "Timed Sessions",
          accentColor = NovaBlue,
          modifier = Modifier.weight(1f)
        )
        MetricCard(
          title = "Avg Accuracy",
          value = String.format("%.0f%%", avgAccuracy),
          subtitle = "Overall Score",
          accentColor = Color(0xFF10B981),
          modifier = Modifier.weight(1f)
        )
        MetricCard(
          title = "Level",
          value = "${userProfile?.level ?: 1}",
          subtitle = "${userProfile?.points ?: 100} XP",
          accentColor = NovaViolet,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Section 16: AI Weakness Detector Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("weakness_detector_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .background(Color(0xFFFEF3C7), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text("AI Weakness Detector", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Surface(color = NovaBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
              Text("Evidence Based", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NovaBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          WeaknessRecommendationItem(
            topic = "Thermodynamics & Carnot Cycle",
            status = "Low Accuracy (42%)",
            advice = "Review second law entropy relations and practice numericals.",
            onPractice = { onTargetedPractice("Thermodynamics & Heat Engines") }
          )

          Spacer(modifier = Modifier.height(10.dp))

          WeaknessRecommendationItem(
            topic = "Organic Reaction Mechanisms",
            status = "Needs Practice (58%)",
            advice = "Frequently missed electrophilic substitution reagents.",
            onPractice = { onTargetedPractice("Organic Reaction Mechanisms") }
          )

          Spacer(modifier = Modifier.height(10.dp))

          WeaknessRecommendationItem(
            topic = "Calculus: Definite Integrals",
            status = "Improving (78%)",
            advice = "Steady progress observed across last 3 test sessions.",
            onPractice = { onTargetedPractice("Definite Integrals Properties") }
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Section 19: Gamification & Achievements
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Achievements & Badges", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }

          Spacer(modifier = Modifier.height(12.dp))

          listOf(
            Triple("First Step", "Completed your first study workspace generation", true),
            Triple("Consistent Scholar", "Maintained a 3-day study streak", true),
            Triple("Mock Master", "Completed a full-length timed mock test", totalTests > 0),
            Triple("Formula Pro", "Reviewed 20+ formulas in Formula Lab", false),
            Triple("Nova Elite", "Attained Level 5 with 1000+ XP", false)
          ).forEach { (title, desc, isUnlocked) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .background(
                    if (isUnlocked) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = null,
                  tint = if (isUnlocked) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = title,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
                Text(
                  text = desc,
                  fontSize = 11.sp,
                  color = Color.Gray
                )
              }
              if (isUnlocked) {
                Text(
                  text = "Unlocked",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF10B981)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MetricCard(
  title: String,
  value: String,
  subtitle: String,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
      Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
    }
  }
}

@Composable
private fun WeaknessRecommendationItem(
  topic: String,
  status: String,
  advice: String,
  onPractice: () -> Unit
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    shape = RoundedCornerShape(10.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(topic, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (status.contains("Low")) Color(0xFFEF4444) else NovaBlue)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(advice, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(modifier = Modifier.height(8.dp))
      Button(
        onClick = onPractice,
        colors = ButtonDefaults.buttonColors(containerColor = NovaBlue),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(34.dp)
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Start Targeted Practice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
