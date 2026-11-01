package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaBlueAccent
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanGlow
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.NovaVioletLight

@Composable
fun HomeDashboardScreen(
  userProfile: UserProfile?,
  onNavigate: (String) -> Unit,
  onQuickGenerate: (String) -> Unit
) {
  var globalSearchQuery by remember { mutableStateOf("") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    // Header Hero Banner
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              listOf(NovaBlue, Color(0xFF0F172A))
            )
          )
          .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Welcome back,",
                fontSize = 13.sp,
                color = NovaCyanGlow,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = userProfile?.displayName ?: "Scholar",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }

            // Streak & XP pill
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak",
                tint = Color(0xFFF97316),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${userProfile?.streakDays ?: 1}d",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Points",
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${userProfile?.points ?: 100} XP",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Target Exam Badge
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Target:",
              fontSize = 12.sp,
              color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              color = NovaViolet.copy(alpha = 0.4f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = userProfile?.selectedExam ?: "JEE Main & Advanced",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Prominent Global Study Input: "What do you want to learn today?"
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "What do you want to learn today?",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = globalSearchQuery,
                onValueChange = { globalSearchQuery = it },
                placeholder = { Text("e.g., Thermodynamics, Organic Reactions, Calculus, Polity Article 21...") },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("global_study_input"),
                leadingIcon = {
                  Icon(Icons.Default.Search, contentDescription = null, tint = NovaBlue)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = NovaBlue,
                  unfocusedBorderColor = Color(0xFFCBD5E1)
                )
              )
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row {
                  QuickActionChip(
                    label = "Scan",
                    icon = Icons.Default.CameraAlt,
                    onClick = { onNavigate("scanner") }
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  QuickActionChip(
                    label = "Ask Tutor",
                    icon = Icons.Default.Psychology,
                    onClick = { onNavigate("tutor") }
                  )
                }

                Button(
                  onClick = {
                    if (globalSearchQuery.isNotBlank()) {
                      onQuickGenerate(globalSearchQuery)
                    } else {
                      onNavigate("generator")
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = NovaBlue),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("generate_study_button")
                ) {
                  Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Generate", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    // Daily Challenge Section
    item {
      Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Daily Challenge",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "+50 XP Bonus",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD97706)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate("daily_challenge") }
            .testTag("daily_challenge_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .background(NovaViolet.copy(alpha = 0.2f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = NovaVioletLight,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Today's 5-Question Speed Drill",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Mixed physics & chemistry concepts with instant explanations",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Core Platform Modules Grid
    item {
      Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
          text = "Study Modules",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
          ModuleCard(
            title = "AI Study Generator",
            subtitle = "Summaries, MCQs & Worksheets",
            icon = Icons.Default.AutoAwesome,
            accentColor = NovaBlueAccent,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("generator") },
            testTag = "module_study_generator"
          )
          Spacer(modifier = Modifier.width(12.dp))
          ModuleCard(
            title = "SnapStudy Scanner",
            subtitle = "Smart Document & Image OCR",
            icon = Icons.Default.CameraAlt,
            accentColor = NovaCyan,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("scanner") },
            testTag = "module_snapstudy"
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
          ModuleCard(
            title = "AI Personal Tutor",
            subtitle = "Interactive Voice & Chat Teacher",
            icon = Icons.Default.Psychology,
            accentColor = NovaVioletLight,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("tutor") },
            testTag = "module_ai_tutor"
          )
          Spacer(modifier = Modifier.width(12.dp))
          ModuleCard(
            title = "Timed Mock Tests",
            subtitle = "Real Exam Engine with Scoring",
            icon = Icons.Default.Timer,
            accentColor = Color(0xFFF59E0B),
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("mock_test") },
            testTag = "module_mock_tests"
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
          ModuleCard(
            title = "Exam Prep Hub",
            subtitle = "JEE, NEET, UPSC & Boards",
            icon = Icons.Default.School,
            accentColor = Color(0xFF10B981),
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("exam_prep") },
            testTag = "module_exam_prep"
          )
          Spacer(modifier = Modifier.width(12.dp))
          ModuleCard(
            title = "Flashcards & Formulas",
            subtitle = "Spaced Repetition & Formula Lab",
            icon = Icons.Default.Style,
            accentColor = Color(0xFFEC4899),
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("flashcards") },
            testTag = "module_flashcards"
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
          ModuleCard(
            title = "AI Study Planner",
            subtitle = "Custom Timetable & Target Date",
            icon = Icons.Default.DateRange,
            accentColor = Color(0xFF6366F1),
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("planner") },
            testTag = "module_study_planner"
          )
          Spacer(modifier = Modifier.width(12.dp))
          ModuleCard(
            title = "Notes & Library",
            subtitle = "Saved Workspaces & Favorites",
            icon = Icons.Default.Bookmark,
            accentColor = Color(0xFF8B5CF6),
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("library") },
            testTag = "module_library"
          )
        }
      }
    }

    // Quick Concept Tip / Formula of the Day
    item {
      Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = NovaBlue.copy(alpha = 0.08f)
          )
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Functions,
                contentDescription = null,
                tint = NovaBlue,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Formula of the Day",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NovaBlue
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Work-Energy Theorem: W_net = ΔK = (1/2)m(v_f^2 - v_i^2)",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Net work done by all forces equals the change in kinetic energy of the body.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun QuickActionChip(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(8.dp),
    color = Color(0xFFF1F5F9),
    modifier = Modifier.height(34.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 10.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = NovaBlue, modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
    }
  }
}

@Composable
private fun ModuleCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  testTag: String
) {
  Card(
    modifier = modifier
      .height(115.dp)
      .clickable(onClick = onClick)
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(20.dp)
        )
      }

      Column {
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1
        )
        Text(
          text = subtitle,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          lineHeight = 14.sp
        )
      }
    }
  }
}
