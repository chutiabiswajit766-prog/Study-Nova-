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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiAiService
import com.example.ai.StudyWorkspaceResult
import com.example.data.model.StudyMaterial
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaViolet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyGeneratorScreen(
  initialTopic: String = "",
  aiService: GeminiAiService,
  onSaveToLibrary: (StudyMaterial) -> Unit
) {
  val scope = rememberCoroutineScope()
  val clipboardManager = LocalClipboardManager.current

  var topic by remember { mutableStateOf(initialTopic) }
  var selectedExam by remember { mutableStateOf("JEE Main & Advanced") }
  var selectedSubject by remember { mutableStateOf("Physics") }
  var selectedDifficulty by remember { mutableStateOf("Intermediate") }
  var selectedLanguage by remember { mutableStateOf("English") }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var workspaceResult by remember { mutableStateOf<StudyWorkspaceResult?>(null) }
  var selectedTab by remember { mutableIntStateOf(0) }
  var savedNotification by remember { mutableStateOf(false) }

  // MCQ answer states: questionIndex -> selectedOptionIndex
  val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }

  val exams = listOf("JEE Main & Advanced", "NEET UG", "UPSC Civil Services", "Class 12 Boards", "Class 10 Boards", "SSC & Banking", "Custom")
  val subjects = listOf("Physics", "Chemistry", "Mathematics", "Biology", "Polity & History", "General Studies", "Reasoning & Aptitude")
  val difficulties = listOf("Foundation (Basic)", "Intermediate (Moderate)", "Advanced (Competitive)")
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
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      Text(
        text = "AI Study Generator",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = "Generate structured summaries, MCQs, formulas & flashcards",
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
      // Input Configuration Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Enter Topic or Concept",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = topic,
            onValueChange = { topic = it },
            placeholder = { Text("e.g. Photoelectric Effect, Newton's Laws, Carbonyl Compounds...") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("topic_input_field"),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Filter row 1: Exam & Subject
          Row(modifier = Modifier.fillMaxWidth()) {
            // Exam Dropdown
            var examExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
              expanded = examExpanded,
              onExpandedChange = { examExpanded = !examExpanded },
              modifier = Modifier.weight(1f)
            ) {
              OutlinedTextField(
                value = selectedExam,
                onValueChange = {},
                readOnly = true,
                label = { Text("Exam", fontSize = 11.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                modifier = Modifier
                  .menuAnchor()
                  .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              )
              ExposedDropdownMenu(
                expanded = examExpanded,
                onDismissRequest = { examExpanded = false }
              ) {
                exams.forEach { exam ->
                  DropdownMenuItem(
                    text = { Text(exam, fontSize = 13.sp) },
                    onClick = {
                      selectedExam = exam
                      examExpanded = false
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Subject Dropdown
            var subjectExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
              expanded = subjectExpanded,
              onExpandedChange = { subjectExpanded = !subjectExpanded },
              modifier = Modifier.weight(1f)
            ) {
              OutlinedTextField(
                value = selectedSubject,
                onValueChange = {},
                readOnly = true,
                label = { Text("Subject", fontSize = 11.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                modifier = Modifier
                  .menuAnchor()
                  .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              )
              ExposedDropdownMenu(
                expanded = subjectExpanded,
                onDismissRequest = { subjectExpanded = false }
              ) {
                subjects.forEach { subj ->
                  DropdownMenuItem(
                    text = { Text(subj, fontSize = 13.sp) },
                    onClick = {
                      selectedSubject = subj
                      subjectExpanded = false
                    }
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Filter row 2: Difficulty & Language
          Row(modifier = Modifier.fillMaxWidth()) {
            var diffExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
              expanded = diffExpanded,
              onExpandedChange = { diffExpanded = !diffExpanded },
              modifier = Modifier.weight(1f)
            ) {
              OutlinedTextField(
                value = selectedDifficulty.substringBefore(" "),
                onValueChange = {},
                readOnly = true,
                label = { Text("Level", fontSize = 11.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffExpanded) },
                modifier = Modifier
                  .menuAnchor()
                  .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              )
              ExposedDropdownMenu(
                expanded = diffExpanded,
                onDismissRequest = { diffExpanded = false }
              ) {
                difficulties.forEach { diff ->
                  DropdownMenuItem(
                    text = { Text(diff, fontSize = 13.sp) },
                    onClick = {
                      selectedDifficulty = diff
                      diffExpanded = false
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            var langExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
              expanded = langExpanded,
              onExpandedChange = { langExpanded = !langExpanded },
              modifier = Modifier.weight(1f)
            ) {
              OutlinedTextField(
                value = selectedLanguage,
                onValueChange = {},
                readOnly = true,
                label = { Text("Language", fontSize = 11.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                modifier = Modifier
                  .menuAnchor()
                  .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              )
              ExposedDropdownMenu(
                expanded = langExpanded,
                onDismissRequest = { langExpanded = false }
              ) {
                languages.forEach { lang ->
                  DropdownMenuItem(
                    text = { Text(lang, fontSize = 13.sp) },
                    onClick = {
                      selectedLanguage = lang
                      langExpanded = false
                    }
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              if (topic.isNotBlank()) {
                isLoading = true
                errorMessage = null
                savedNotification = false
                selectedAnswers.clear()
                scope.launch {
                  val result = aiService.generateStudyWorkspace(
                    topicOrMaterial = topic,
                    exam = selectedExam,
                    subject = selectedSubject,
                    difficulty = selectedDifficulty,
                    language = selectedLanguage
                  )
                  isLoading = false
                  result.onSuccess {
                    workspaceResult = it
                  }.onFailure { err ->
                    errorMessage = err.localizedMessage ?: "Failed to generate study workspace."
                  }
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("generate_workspace_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NovaBlue),
            shape = RoundedCornerShape(10.dp),
            enabled = !isLoading && topic.isNotBlank()
          ) {
            if (isLoading) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Analyzing & Generating Workspace...")
            } else {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Generate Study Workspace", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(14.dp),
            fontSize = 13.sp
          )
        }
      }

      // Generated Workspace Display
      workspaceResult?.let { res ->
        Spacer(modifier = Modifier.height(16.dp))

        // Action bar (Save to Library & Copy)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Study Workspace: $topic",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground
          )

          Row {
            Button(
              onClick = {
                val fullSummary = "Short Summary:\n${res.shortSummary}\n\nDetailed:\n${res.detailedSummary}\n\nKey Formulas:\n${res.formulaSheet.joinToString("\n")}"
                clipboardManager.setText(AnnotatedString(fullSummary))
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(34.dp)
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Copy", fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
              onClick = {
                val material = StudyMaterial(
                  materialId = "mat_${System.currentTimeMillis()}",
                  title = topic,
                  topic = topic,
                  subject = selectedSubject,
                  exam = selectedExam,
                  type = "summary",
                  contentJson = res.detailedSummary,
                  source = "ai_generator"
                )
                onSaveToLibrary(material)
                savedNotification = true
              },
              colors = ButtonDefaults.buttonColors(containerColor = if (savedNotification) Color(0xFF10B981) else NovaBlue),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .height(34.dp)
                .testTag("save_workspace_button")
            ) {
              Icon(
                imageVector = if (savedNotification) Icons.Default.Check else Icons.Default.BookmarkAdd,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (savedNotification) "Saved!" else "Save Library", fontSize = 11.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Workspace Tabs
        val tabs = listOf("Summaries", "Formulas", "MCQ Practice", "Q&A", "Flashcards", "Mind Map")
        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          edgePadding = 0.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          tabs.forEachIndexed { index, tabTitle ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = { Text(tabTitle, fontSize = 13.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
          0 -> {
            // Summaries & Explanation
            WorkspaceSectionCard(title = "Executive Summary") {
              Text(text = res.shortSummary, fontSize = 14.sp, lineHeight = 20.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            WorkspaceSectionCard(title = "Simple Explanation (Intuition & Analogy)") {
              Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = res.simpleExplanation, fontSize = 14.sp, lineHeight = 20.sp)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            WorkspaceSectionCard(title = "Detailed In-Depth Breakdown") {
              Text(text = res.detailedSummary, fontSize = 14.sp, lineHeight = 20.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (res.importantDefinitions.isNotEmpty()) {
              WorkspaceSectionCard(title = "Important Definitions & Principles") {
                for (def in res.importantDefinitions) {
                  Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("• ", fontWeight = FontWeight.Bold, color = NovaBlue)
                    Text(def, fontSize = 13.sp, lineHeight = 18.sp)
                  }
                }
              }
            }
          }

          1 -> {
            // Formula Sheet
            WorkspaceSectionCard(title = "Essential Formulas & Equations") {
              if (res.formulaSheet.isEmpty()) {
                Text("No numerical formulas for this concept.", color = Color.Gray, fontSize = 13.sp)
              } else {
                for (f in res.formulaSheet) {
                  Surface(
                    color = NovaBlue.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp)
                  ) {
                    Text(
                      text = f,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 14.sp,
                      color = NovaBlue,
                      modifier = Modifier.padding(12.dp)
                    )
                  }
                }
              }
            }
          }

          2 -> {
            // Practice MCQs with interactive answer checking
            Text(
              text = "Practice Multiple Choice Questions",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              modifier = Modifier.padding(bottom = 8.dp)
            )

            for ((qIdx, mcq) in res.mcqs.withIndex()) {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Text(
                    text = "Q${qIdx + 1}. ${mcq.questionText}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  Spacer(modifier = Modifier.height(8.dp))

                  for ((optIdx, optText) in mcq.options.withIndex()) {
                    val isSelected = selectedAnswers[qIdx] == optIdx
                    val isAnswered = selectedAnswers.containsKey(qIdx)
                    val isCorrect = optIdx == mcq.correctOptionIndex

                    val bgColor = when {
                      !isAnswered -> if (isSelected) NovaBlue.copy(alpha = 0.1f) else Color(0xFFF8FAFC)
                      isCorrect -> Color(0xFFDCFCE7) // green
                      isSelected -> Color(0xFFFEE2E2) // red
                      else -> Color(0xFFF8FAFC)
                    }

                    val borderColor = when {
                      !isAnswered -> if (isSelected) NovaBlue else Color(0xFFE2E8F0)
                      isCorrect -> Color(0xFF10B981)
                      isSelected -> Color(0xFFEF4444)
                      else -> Color(0xFFE2E8F0)
                    }

                    Surface(
                      color = bgColor,
                      shape = RoundedCornerShape(8.dp),
                      border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                          if (!isAnswered) {
                            selectedAnswers[qIdx] = optIdx
                          }
                        }
                    ) {
                      Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = "${('A'.code + optIdx).toChar()}.",
                          fontWeight = FontWeight.Bold,
                          fontSize = 13.sp,
                          color = if (isAnswered && isCorrect) Color(0xFF15803D) else Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = optText,
                          fontSize = 13.sp,
                          modifier = Modifier.weight(1f)
                        )
                        if (isAnswered && isCorrect) {
                          Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        } else if (isAnswered && isSelected) {
                          Icon(Icons.Default.Close, contentDescription = "Incorrect", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                      }
                    }
                  }

                  // Reveal explanation once answered
                  if (selectedAnswers.containsKey(qIdx)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                      color = NovaBlue.copy(alpha = 0.08f),
                      shape = RoundedCornerShape(8.dp),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Column(modifier = Modifier.padding(10.dp)) {
                        Text("Explanation:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NovaBlue)
                        Text(mcq.explanation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                      }
                    }
                  }
                }
              }
            }
          }

          3 -> {
            // Q&A
            WorkspaceSectionCard(title = "Core Questions & Model Answers") {
              for ((i, qa) in res.questionsAndAnswers.withIndex()) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                  Text("Q${i + 1}: ${qa.first}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NovaBlue)
                  Text("Ans: ${qa.second}", fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp), lineHeight = 18.sp)
                }
              }
            }
          }

          4 -> {
            // Flashcards
            Text(
              text = "Topic Flashcards (${res.flashcards.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              modifier = Modifier.padding(bottom = 8.dp)
            )

            for (fc in res.flashcards) {
              StudyFlashcardWorkspaceItem(fc)
            }
          }

          5 -> {
            // Mind Map Outline
            WorkspaceSectionCard(title = "Hierarchical Mind Map Outline") {
              for (node in res.mindMapOutline) {
                Text(
                  text = node,
                  fontSize = 13.sp,
                  fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                  modifier = Modifier.padding(vertical = 2.dp),
                  color = if (node.startsWith("  ")) Color(0xFF334155) else NovaBlue,
                  fontWeight = if (node.startsWith("  ")) FontWeight.Normal else FontWeight.Bold
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
private fun StudyFlashcardWorkspaceItem(fc: Pair<String, String>) {
  var isFlipped by remember { mutableStateOf(false) }
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
      .clickable { isFlipped = !isFlipped },
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isFlipped) NovaViolet.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = if (isFlipped) "BACK: ANSWER" else "FRONT: QUESTION",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = if (isFlipped) NovaViolet else NovaBlue
        )
        Text(
          text = "Tap to flip",
          fontSize = 11.sp,
          color = Color.Gray
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = if (isFlipped) fc.second else fc.first,
        fontSize = 14.sp,
        fontWeight = if (isFlipped) FontWeight.Normal else FontWeight.SemiBold
      )
    }
  }
}

@Composable
private fun WorkspaceSectionCard(
  title: String,
  content: @Composable () -> Unit
) {
  var isExpanded by remember { mutableStateOf(true) }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isExpanded = !isExpanded },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NovaBlue)
        Icon(
          imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = null,
          tint = NovaBlue
        )
      }

      if (isExpanded) {
        Spacer(modifier = Modifier.height(8.dp))
        content()
      }
    }
  }
}
