package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiAiService
import com.example.data.model.StudyPlan
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaViolet
import kotlinx.coroutines.launch
import org.json.JSONObject

data class PlannerTask(
  val id: String,
  val time: String,
  val task: String,
  val focus: String
)

@Composable
fun StudyPlannerScreen(
  existingPlan: StudyPlan?,
  aiService: GeminiAiService,
  onSavePlan: (StudyPlan) -> Unit
) {
  val scope = rememberCoroutineScope()

  var exam by remember { mutableStateOf(existingPlan?.exam ?: "JEE Main & Advanced") }
  var targetDate by remember { mutableStateOf(existingPlan?.targetDate ?: "May 2027") }
  var dailyHours by remember { mutableIntStateOf(existingPlan?.hoursPerDay?.toInt() ?: 4) }
  var isLoading by remember { mutableStateOf(false) }

  // Default initial tasks
  val tasks = remember {
    mutableStateMapOf<String, Boolean>().apply {
      put("08:00 - 10:00: Deep Theory & Derivations", false)
      put("14:00 - 15:30: High-Yield MCQ Practice", false)
      put("19:00 - 20:00: Formula Revision & Spaced Repetition", false)
      put("21:00 - 22:00: Previous Year Questions & Analysis", false)
    }
  }

  val completedCount = tasks.count { it.value }
  val progress = if (tasks.isNotEmpty()) (completedCount.toFloat() / tasks.size.toFloat()) else 0f

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
        text = "AI Study Planner",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = "Personalized daily schedule, milestones & progress tracker",
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
      // Plan Configuration & Generator Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Target Schedule Parameters", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(10.dp))

          Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = exam,
              onValueChange = { exam = it },
              label = { Text("Exam") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
              value = targetDate,
              onValueChange = { targetDate = it },
              label = { Text("Target Date") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Study Hours Selector
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Daily Hours:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf(2, 4, 6, 8).forEach { hrs ->
                Surface(
                  onClick = { dailyHours = hrs },
                  shape = RoundedCornerShape(8.dp),
                  color = if (dailyHours == hrs) NovaBlue else Color(0xFFF1F5F9),
                  modifier = Modifier.height(32.dp)
                ) {
                  Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                    Text(
                      text = "${hrs}h",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (dailyHours == hrs) Color.White else Color(0xFF334155)
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              isLoading = true
              scope.launch {
                val result = aiService.generateStudyPlan(
                  exam = exam,
                  targetDate = targetDate,
                  dailyHours = dailyHours,
                  subjects = listOf("Physics", "Chemistry", "Mathematics")
                )
                isLoading = false
                result.onSuccess { jsonStr ->
                  val plan = StudyPlan(
                    planId = "plan_${System.currentTimeMillis()}",
                    exam = exam,
                    targetDate = targetDate,
                    hoursPerDay = dailyHours.toLong(),
                    dailyTasksJson = jsonStr,
                    progressPercentage = (progress * 100).toDouble()
                  )
                  onSavePlan(plan)
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
              .testTag("generate_schedule_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NovaBlue),
            shape = RoundedCornerShape(10.dp),
            enabled = !isLoading
          ) {
            if (isLoading) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Calculating Optimal Timetable...")
            } else {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Generate AI Schedule", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Today's Routine & Checklist
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Schedule, contentDescription = null, tint = NovaBlue, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Today's Timetable", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Text("${(progress * 100).toInt()}% Done", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NovaBlue)
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Progress bar
          Surface(
            color = Color(0xFFE2E8F0),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(fraction = progress)
                .background(NovaBlue)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          tasks.keys.forEach { taskKey ->
            val isChecked = tasks[taskKey] == true
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { tasks[taskKey] = !isChecked }
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isChecked) Color(0xFF10B981) else Color.Gray,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = taskKey,
                fontSize = 13.sp,
                fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Medium,
                color = if (isChecked) Color.Gray else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Weekly Milestones Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = NovaViolet, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Weekly Milestones", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }

          Spacer(modifier = Modifier.height(12.dp))

          listOf(
            "Week 1: Core Mechanics, Electrostatics & Thermodynamics foundations",
            "Week 2: Coordination Compounds, Chemical Kinetics & Integrals",
            "Week 3: Full chapter revision drills & 1 timed mock test",
            "Week 4: Error analysis, formula sheet memorization & 2 mock tests"
          ).forEachIndexed { idx, milestone ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
              Box(
                modifier = Modifier
                  .size(20.dp)
                  .background(NovaViolet.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text("${idx + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NovaViolet)
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(milestone, fontSize = 13.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }
          }
        }
      }
    }
  }
}
