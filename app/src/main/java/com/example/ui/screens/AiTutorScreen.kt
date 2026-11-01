package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiAiService
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaViolet
import kotlinx.coroutines.launch
import java.util.Locale

data class ChatMessage(
  val sender: String, // "user" or "tutor"
  val text: String,
  val isStreaming: Boolean = false
)

@Composable
fun AiTutorScreen(
  aiService: GeminiAiService,
  examContext: String = "JEE Main & Advanced",
  languageContext: String = "English"
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  var inputText by remember { mutableStateOf("") }
  var isGenerating by remember { mutableStateOf(false) }
  var isSpeaking by remember { mutableStateOf(false) }

  val messages = remember {
    mutableStateListOf(
      ChatMessage(
        sender = "tutor",
        text = "Hello! I am your StudyNova AI Personal Teacher for $examContext. Ask me any question, ask for step-by-step math breakdowns, or tap one of the quick actions below to begin!"
      )
    )
  }

  // Android Text-To-Speech engine
  var tts by remember { mutableStateOf<TextToSpeech?>(null) }
  DisposableEffect(context) {
    var ttsInstance: TextToSpeech? = null
    ttsInstance = TextToSpeech(context) { status ->
      if (status == TextToSpeech.SUCCESS) {
        ttsInstance?.language = Locale.ENGLISH
      }
    }
    tts = ttsInstance
    onDispose {
      ttsInstance?.stop()
      ttsInstance?.shutdown()
    }
  }

  // Speech to text launcher
  val speechLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      val data = result.data
      val spokenList = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
      val spokenText = spokenList?.firstOrNull()
      if (!spokenText.isNullOrBlank()) {
        inputText = spokenText
      }
    }
  }

  fun sendMessage(prompt: String) {
    if (prompt.isBlank() || isGenerating) return
    val userMsg = ChatMessage("user", prompt)
    messages.add(userMsg)
    inputText = ""
    isGenerating = true

    // Placeholder message for streaming
    val tutorMsgIndex = messages.size
    messages.add(ChatMessage("tutor", "", isStreaming = true))

    scope.launch {
      val history = messages.filter { !it.isStreaming }.map { Pair(it.sender, it.text) }
      val streamFlow = aiService.streamTutorResponse(
        userPrompt = prompt,
        conversationHistory = history,
        examContext = examContext,
        language = languageContext
      )

      val accumulated = StringBuilder()
      streamFlow.collect { chunk ->
        accumulated.append(chunk)
        if (tutorMsgIndex < messages.size) {
          messages[tutorMsgIndex] = ChatMessage("tutor", accumulated.toString(), isStreaming = true)
        }
      }

      if (tutorMsgIndex < messages.size) {
        messages[tutorMsgIndex] = ChatMessage("tutor", accumulated.toString(), isStreaming = false)
      }
      isGenerating = false
    }
  }

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val quickActions = listOf(
    "Explain Simply",
    "Show an Example",
    "Explain Step by Step",
    "Give Me a Hint",
    "Ask Me a Question",
    "Test My Understanding"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Top Bar
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Psychology, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("AI Personal Teacher", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            Text(examContext, fontSize = 11.sp, color = Color(0xFFBAE6FD))
          }
        }

        if (isSpeaking) {
          IconButton(
            onClick = {
              tts?.stop()
              isSpeaking = false
            }
          ) {
            Icon(Icons.Default.Stop, contentDescription = "Stop Speech", tint = Color(0xFFEF4444))
          }
        }
      }
    }

    // Quick Action Chips Row
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(quickActions) { action ->
        Surface(
          onClick = { sendMessage(action) },
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          border = androidx.compose.foundation.BorderStroke(1.dp, NovaBlue.copy(alpha = 0.3f))
        ) {
          Text(
            text = action,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = NovaBlue,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }
    }

    // Chat Messages List
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      state = listState,
      contentPadding = PaddingValues(vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(messages) { msg ->
        val isUser = msg.sender == "user"
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
          if (!isUser) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .background(NovaViolet.copy(alpha = 0.2f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Psychology, contentDescription = null, tint = NovaViolet, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
          }

          Card(
            shape = RoundedCornerShape(
              topStart = 16.dp,
              topEnd = 16.dp,
              bottomStart = if (isUser) 16.dp else 2.dp,
              bottomEnd = if (isUser) 2.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
              containerColor = if (isUser) NovaBlue else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(max = 280.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = msg.text.ifEmpty { "Thinking..." },
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 20.sp
              )

              if (!isUser && msg.text.isNotBlank() && !msg.isStreaming) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End
                ) {
                  Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Listen to answer",
                    tint = NovaBlue,
                    modifier = Modifier
                      .size(18.dp)
                      .clickable {
                        tts?.speak(msg.text, TextToSpeech.QUEUE_FLUSH, null, "tutor_speech")
                        isSpeaking = true
                      }
                  )
                }
              }
            }
          }
        }
      }
    }

    // Input Bar
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 4.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Voice STT button
        IconButton(
          onClick = {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
              putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
              putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your question to StudyNova Tutor")
            }
            try {
              speechLauncher.launch(intent)
            } catch (e: Exception) {
              // speech recognizer not available
            }
          },
          modifier = Modifier
            .size(42.dp)
            .testTag("voice_input_button")
        ) {
          Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = NovaBlue)
        }

        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = { Text("Ask your personal tutor...", fontSize = 13.sp) },
          modifier = Modifier
            .weight(1f)
            .testTag("tutor_message_input"),
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NovaBlue,
            unfocusedBorderColor = Color(0xFFCBD5E1)
          ),
          maxLines = 3
        )

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
          onClick = { sendMessage(inputText) },
          enabled = inputText.isNotBlank() && !isGenerating,
          modifier = Modifier
            .size(42.dp)
            .background(if (inputText.isNotBlank()) NovaBlue else Color(0xFFE2E8F0), CircleShape)
            .testTag("send_tutor_message_button")
        ) {
          if (isGenerating) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
          } else {
            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}
