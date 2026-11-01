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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamInfo
import com.example.data.model.ExamRepository
import com.example.data.model.Question
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaViolet

@Composable
fun ExamPrepHubScreen(
  onSelectTopicForGenerator: (String) -> Unit,
  onStartMockTest: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var selectedExamIndex by remember { mutableIntStateOf(0) }
  val currentExam = ExamRepository.supportedExams[selectedExamIndex]

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
        text = "Exam Preparation Hub",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = "Official syllabi, verified PYQs, and topic-wise strategy",
        fontSize = 12.sp,
        color = Color(0xFFBAE6FD)
      )
    }

    // Exam Selector horizontal carousel
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF0F172A))
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(ExamRepository.supportedExams.size) { index ->
        val exam = ExamRepository.supportedExams[index]
        val isSelected = selectedExamIndex == index
        Surface(
          onClick = { selectedExamIndex = index },
          shape = RoundedCornerShape(20.dp),
          color = if (isSelected) NovaCyan else Color(0xFF1E293B)
        ) {
          Text(
            text = exam.name,
            color = if (isSelected) Color(0xFF0F172A) else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
          )
        }
      }
    }

    // Tabs: Overview & Syllabus, Verified PYQs, High-Yield Concepts
    val tabs = listOf("Syllabus & Strategy", "Verified PYQ Library", "Formula Lab")
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      edgePadding = 0.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      tabs.forEachIndexed { idx, title ->
        Tab(
          selected = selectedTab == idx,
          onClick = { selectedTab = idx },
          text = { Text(title, fontSize = 13.sp, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal) }
        )
      }
    }

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      when (selectedTab) {
        0 -> {
          // Exam Details
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(currentExam.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NovaBlue)
                Text(currentExam.subtitle, fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceAround
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Duration", fontSize = 11.sp, color = Color.Gray)
                    Text("${currentExam.durationMinutes} mins", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Marking", fontSize = 11.sp, color = Color.Gray)
                    Text("+${currentExam.correctMark.toInt()} / -${currentExam.negativeMark}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Subjects", fontSize = 11.sp, color = Color.Gray)
                    Text("${currentExam.subjects.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Syllabus Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                  currentExam.syllabusOverview,
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(top = 4.dp),
                  lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                  onClick = onStartMockTest,
                  modifier = Modifier.fillMaxWidth(),
                  colors = ButtonDefaults.buttonColors(containerColor = NovaBlue),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Take ${currentExam.name} Mock Test")
                }
              }
            }
          }

          item {
            Text("Subjects in this Examination", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }

          items(currentExam.subjects) { subject ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectTopicForGenerator("${currentExam.name} - $subject") },
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .background(NovaBlue.copy(alpha = 0.1f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.MenuBook, contentDescription = null, tint = NovaBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(subject, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                  Text("Generate high-yield chapter notes & practice", fontSize = 11.sp, color = Color.Gray)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
              }
            }
          }
        }

        1 -> {
          // Verified PYQ Repository
          item {
            Surface(
              color = Color(0xFFDCFCE7),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Verified Previous Year Questions (PYQs) with step-by-step verified solutions.",
                  fontSize = 12.sp,
                  color = Color(0xFF14532D)
                )
              }
            }
          }

          items(ExamRepository.samplePyqs) { pyq ->
            var showSolution by remember { mutableStateOf(false) }

            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Surface(color = NovaBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                    Text(pyq.source, fontSize = 11.sp, color = NovaBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                  }
                  Text(pyq.difficulty, fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(pyq.questionText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)

                Spacer(modifier = Modifier.height(10.dp))
                pyq.options.forEachIndexed { idx, opt ->
                  Text(
                    text = "${('A'.code + idx).toChar()}. $opt",
                    fontSize = 13.sp,
                    color = if (showSolution && idx == pyq.correctOptionIndex) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (showSolution && idx == pyq.correctOptionIndex) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 2.dp)
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Button(
                    onClick = { showSolution = !showSolution },
                    colors = ButtonDefaults.buttonColors(containerColor = if (showSolution) Color(0xFF475569) else NovaBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                  ) {
                    Text(if (showSolution) "Hide Solution" else "View Solution", fontSize = 11.sp)
                  }
                }

                if (showSolution) {
                  Spacer(modifier = Modifier.height(8.dp))
                  Surface(
                    color = NovaBlue.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                      Text("Correct Answer: ${('A'.code + pyq.correctOptionIndex).toChar()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NovaBlue)
                      Text(pyq.explanation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp))
                    }
                  }
                }
              }
            }
          }
        }

        2 -> {
          // Formula Lab
          item {
            Text("Formula Lab - Verified Scientific Formulations", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }

          items(ExamRepository.coreFormulas) { item ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NovaBlue)
                  Text(item.subject, fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                  color = NovaBlue.copy(alpha = 0.08f),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = item.formula,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = NovaBlue,
                    modifier = Modifier.padding(12.dp)
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Variables: ${item.variables}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Condition: ${item.condition}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Units: ${item.unit}", fontSize = 12.sp, color = NovaViolet, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }
    }
  }
}
