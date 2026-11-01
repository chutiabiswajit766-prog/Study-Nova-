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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiAiService
import com.example.data.model.ExamRepository
import com.example.data.model.MockTestResult
import com.example.data.model.Question
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TestStage {
  CONFIG,
  TESTING,
  RESULT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestScreen(
  aiService: GeminiAiService,
  onSaveResult: (MockTestResult) -> Unit
) {
  val scope = rememberCoroutineScope()

  var stage by remember { mutableStateOf(TestStage.CONFIG) }
  var selectedExam by remember { mutableStateOf("JEE Main & Advanced") }
  var selectedSubject by remember { mutableStateOf("Physics") }
  var selectedQuestionCount by remember { mutableIntStateOf(5) }
  var selectedDurationMinutes by remember { mutableIntStateOf(10) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var questions by remember { mutableStateOf<List<Question>>(emptyList()) }

  // Test state
  var currentQuestionIndex by remember { mutableIntStateOf(0) }
  val userAnswers = remember { mutableStateMapOf<Int, Int>() } // qIndex -> optionIndex
  val markedForReview = remember { mutableStateMapOf<Int, Boolean>() }
  var secondsRemaining by remember { mutableIntStateOf(600) }
  var showConfirmSubmit by remember { mutableStateOf(false) }

  // Results
  var testResult by remember { mutableStateOf<MockTestResult?>(null) }

  // Countdown timer when in TESTING stage
  LaunchedEffect(stage, secondsRemaining) {
    if (stage == TestStage.TESTING && secondsRemaining > 0) {
      delay(1000L)
      secondsRemaining -= 1
      if (secondsRemaining <= 0) {
        // Auto-submit when time expires
        stage = TestStage.RESULT
      }
    }
  }

  fun submitTest() {
    val total = questions.size
    var correct = 0L
    var incorrect = 0L
    var unattempted = 0L

    val examInfo = ExamRepository.supportedExams.find { it.name == selectedExam }
      ?: ExamRepository.supportedExams.first()

    for (i in 0 until total) {
      val ans = userAnswers[i]
      if (ans == null) {
        unattempted++
      } else if (ans == questions[i].correctOptionIndex) {
        correct++
      } else {
        incorrect++
      }
    }

    val score = (correct * examInfo.correctMark) - (incorrect * examInfo.negativeMark)
    val maxScore = total * examInfo.correctMark
    val percentage = if (maxScore > 0) (score / maxScore) * 100.0 else 0.0
    val attempted = correct + incorrect
    val accuracy = if (attempted > 0) (correct.toDouble() / attempted.toDouble()) * 100.0 else 0.0
    val timeTakenSeconds = (selectedDurationMinutes * 60 - secondsRemaining).toLong()

    val result = MockTestResult(
      resultId = "res_${System.currentTimeMillis()}",
      exam = selectedExam,
      subject = selectedSubject,
      totalQuestions = total.toLong(),
      correctAnswers = correct,
      incorrectAnswers = incorrect,
      unattempted = unattempted,
      score = score,
      maxScore = maxScore,
      percentage = percentage.coerceAtLeast(0.0),
      accuracy = accuracy,
      timeTakenSeconds = timeTakenSeconds
    )

    testResult = result
    onSaveResult(result)
    stage = TestStage.RESULT
  }

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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Timed Mock Test Engine", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
          Text(selectedExam, fontSize = 11.sp, color = Color(0xFFBAE6FD))
        }

        if (stage == TestStage.TESTING) {
          // Live Countdown Timer
          val mins = secondsRemaining / 60
          val secs = secondsRemaining % 60
          val timerColor = if (secondsRemaining < 120) Color(0xFFEF4444) else Color.White

          Surface(
            color = Color.Black.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Icon(Icons.Default.Timer, contentDescription = null, tint = timerColor, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = String.format("%02d:%02d", mins, secs),
                color = timerColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }
        }
      }
    }

    when (stage) {
      TestStage.CONFIG -> {
        // Configuration View
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
        ) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Text("Configure Your Mock Test", fontWeight = FontWeight.Bold, fontSize = 16.sp)
              Spacer(modifier = Modifier.height(14.dp))

              // Exam Selection
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
                  label = { Text("Target Examination") },
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
                        selectedSubject = exam.subjects.first()
                        examExp = false
                      }
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Subject Selection
              val examInfo = ExamRepository.supportedExams.find { it.name == selectedExam }
                ?: ExamRepository.supportedExams.first()

              var subjExp by remember { mutableStateOf(false) }
              ExposedDropdownMenuBox(
                expanded = subjExp,
                onExpandedChange = { subjExp = !subjExp },
                modifier = Modifier.fillMaxWidth()
              ) {
                OutlinedTextField(
                  value = selectedSubject,
                  onValueChange = {},
                  readOnly = true,
                  label = { Text("Subject") },
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjExp) },
                  modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                  expanded = subjExp,
                  onDismissRequest = { subjExp = false }
                ) {
                  examInfo.subjects.forEach { s ->
                    DropdownMenuItem(
                      text = { Text(s) },
                      onClick = {
                        selectedSubject = s
                        subjExp = false
                      }
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Marking Rules display
              Surface(
                color = NovaBlue.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text("Marking Scheme:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NovaBlue)
                  Text(examInfo.markingScheme, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Question count pills
              Text("Question Count:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              Spacer(modifier = Modifier.height(6.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15, 20).forEach { count ->
                  Surface(
                    onClick = {
                      selectedQuestionCount = count
                      selectedDurationMinutes = count * 2
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedQuestionCount == count) NovaBlue else Color(0xFFF1F5F9),
                    modifier = Modifier.height(36.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
                      Text(
                        text = "$count Qs",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedQuestionCount == count) Color.White else Color(0xFF334155),
                        fontSize = 13.sp
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              Button(
                onClick = {
                  isLoading = true
                  errorMessage = null
                  scope.launch {
                    val result = aiService.generateMockTestQuestions(
                      exam = selectedExam,
                      subject = selectedSubject,
                      questionCount = selectedQuestionCount
                    )
                    isLoading = false
                    result.onSuccess { generatedQuestions ->
                      questions = generatedQuestions
                      currentQuestionIndex = 0
                      userAnswers.clear()
                      markedForReview.clear()
                      secondsRemaining = selectedDurationMinutes * 60
                      stage = TestStage.TESTING
                    }.onFailure { err ->
                      // Fallback to sample PYQs if AI request faces rate limit
                      val fallbacks = ExamRepository.samplePyqs.take(selectedQuestionCount)
                      if (fallbacks.isNotEmpty()) {
                        questions = fallbacks
                        currentQuestionIndex = 0
                        userAnswers.clear()
                        markedForReview.clear()
                        secondsRemaining = selectedDurationMinutes * 60
                        stage = TestStage.TESTING
                      } else {
                        errorMessage = err.localizedMessage ?: "Failed to generate mock test."
                      }
                    }
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("start_mock_test_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NovaBlue),
                shape = RoundedCornerShape(10.dp),
                enabled = !isLoading
              ) {
                if (isLoading) {
                  CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Building Examination Paper...")
                } else {
                  Icon(Icons.Default.PlayArrow, contentDescription = null)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Start Timed Test", fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          if (errorMessage != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(12.dp),
                fontSize = 13.sp
              )
            }
          }
        }
      }

      TestStage.TESTING -> {
        // Active Timed Test Interface
        if (questions.isNotEmpty() && currentQuestionIndex < questions.size) {
          val currentQuestion = questions[currentQuestionIndex]
          val selectedOpt = userAnswers[currentQuestionIndex]
          val isMarked = markedForReview[currentQuestionIndex] == true

          Column(modifier = Modifier.fillMaxSize()) {
            // Question navigation strip
            LazyRow(
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              itemsIndexed(questions) { idx, _ ->
                val isAns = userAnswers.containsKey(idx)
                val isRev = markedForReview[idx] == true
                val isCurrent = idx == currentQuestionIndex

                val pillColor = when {
                  isCurrent -> NovaBlue
                  isRev -> NovaViolet
                  isAns -> Color(0xFF10B981) // Green
                  else -> Color(0xFF94A3B8)
                }

                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(pillColor)
                    .clickable { currentQuestionIndex = idx },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "${idx + 1}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                }
              }
            }

            // Question Card & Options
            Column(
              modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = NovaBlue
                )

                // Mark for review button
                OutlinedButton(
                  onClick = {
                    markedForReview[currentQuestionIndex] = !isMarked
                  },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.height(34.dp)
                ) {
                  Icon(
                    imageVector = if (isMarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isMarked) NovaViolet else Color.Gray,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (isMarked) "Marked" else "Review",
                    fontSize = 11.sp,
                    color = if (isMarked) NovaViolet else Color.Gray
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Text(
                    text = currentQuestion.questionText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                  )

                  Spacer(modifier = Modifier.height(16.dp))

                  currentQuestion.options.forEachIndexed { optIdx, optText ->
                    val isSelected = selectedOpt == optIdx
                    Surface(
                      color = if (isSelected) NovaBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                      shape = RoundedCornerShape(10.dp),
                      border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) NovaBlue else Color.Transparent
                      ),
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable { userAnswers[currentQuestionIndex] = optIdx }
                    ) {
                      Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Box(
                          modifier = Modifier
                            .size(26.dp)
                            .background(
                              if (isSelected) NovaBlue else Color(0xFFCBD5E1),
                              CircleShape
                            ),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(
                            text = "${('A'.code + optIdx).toChar()}",
                            color = if (isSelected) Color.White else Color(0xFF334155),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                          )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                          text = optText,
                          fontSize = 13.sp,
                          color = MaterialTheme.colorScheme.onSurface
                        )
                      }
                    }
                  }

                  if (selectedOpt != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                      text = "Clear Response",
                      color = Color(0xFFEF4444),
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier
                        .clickable { userAnswers.remove(currentQuestionIndex) }
                        .padding(4.dp)
                    )
                  }
                }
              }
            }

            // Bottom Navigation Footer
            Surface(
              color = MaterialTheme.colorScheme.surface,
              tonalElevation = 4.dp,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedButton(
                  onClick = {
                    if (currentQuestionIndex > 0) currentQuestionIndex -= 1
                  },
                  enabled = currentQuestionIndex > 0,
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Prev")
                }

                Button(
                  onClick = { showConfirmSubmit = true },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.testTag("submit_mock_test_button")
                ) {
                  Text("Submit Test", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                  onClick = {
                    if (currentQuestionIndex < questions.size - 1) {
                      currentQuestionIndex += 1
                    } else {
                      showConfirmSubmit = true
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = NovaBlue),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(if (currentQuestionIndex < questions.size - 1) "Next" else "Done")
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }
      }

      TestStage.RESULT -> {
        // Results & Performance Analysis
        testResult?.let { res ->
          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState())
              .padding(16.dp)
          ) {
            // Score Summary Card
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
                    .size(56.dp)
                    .background(Color(0xFFFEF3C7), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = "Test Completed!",
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                  text = "${res.exam} • ${res.subject}",
                  fontSize = 12.sp,
                  color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Score banner
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                  ScoreMetric(label = "Score", value = String.format("%.1f / %.1f", res.score, res.maxScore), color = NovaBlue)
                  ScoreMetric(label = "Accuracy", value = String.format("%.1f%%", res.accuracy), color = Color(0xFF10B981))
                  ScoreMetric(label = "Percentage", value = String.format("%.1f%%", res.percentage), color = NovaViolet)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceAround
                ) {
                  Text("Correct: ${res.correctAnswers}", color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  Text("Incorrect: ${res.incorrectAnswers}", color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  Text("Unattempted: ${res.unattempted}", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Question-by-Question Review", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(10.dp))

            questions.forEachIndexed { i, q ->
              val userAns = userAnswers[i]
              val isCorrect = userAns == q.correctOptionIndex

              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                      contentDescription = null,
                      tint = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Q${i + 1}. ${q.questionText}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  q.options.forEachIndexed { optIdx, optText ->
                    val isUserPicked = userAns == optIdx
                    val isRightAnswer = optIdx == q.correctOptionIndex

                    val rowColor = when {
                      isRightAnswer -> Color(0xFFDCFCE7)
                      isUserPicked -> Color(0xFFFEE2E2)
                      else -> Color(0xFFF8FAFC)
                    }

                    Surface(
                      color = rowColor,
                      shape = RoundedCornerShape(6.dp),
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                    ) {
                      Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = "${('A'.code + optIdx).toChar()}. $optText",
                          fontSize = 12.sp,
                          fontWeight = if (isRightAnswer) FontWeight.Bold else FontWeight.Normal,
                          color = if (isRightAnswer) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))
                  Surface(
                    color = NovaBlue.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                      Text("Solution & Explanation:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NovaBlue)
                      Text(q.explanation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = { stage = TestStage.CONFIG },
              colors = ButtonDefaults.buttonColors(containerColor = NovaBlue),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Take Another Mock Test", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // Confirmation Submit Modal
  if (showConfirmSubmit) {
    AlertDialog(
      onDismissRequest = { showConfirmSubmit = false },
      title = { Text("Submit Mock Test?") },
      text = {
        val answered = userAnswers.size
        val total = questions.size
        Text("You have answered $answered of $total questions. Are you ready to submit and calculate your final score?")
      },
      confirmButton = {
        Button(
          onClick = {
            showConfirmSubmit = false
            submitTest()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
          Text("Submit Now")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showConfirmSubmit = false }) {
          Text("Continue Test")
        }
      }
    )
  }
}

@Composable
private fun ScoreMetric(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
    Text(text = label, fontSize = 11.sp, color = Color.Gray)
  }
}
