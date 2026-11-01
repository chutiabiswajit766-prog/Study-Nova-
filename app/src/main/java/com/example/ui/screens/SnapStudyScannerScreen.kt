package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiAiService
import com.example.data.model.StudyMaterial
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapStudyScannerScreen(
  aiService: GeminiAiService,
  onSaveToLibrary: (StudyMaterial) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var selectedAction by remember { mutableStateOf("Explain this page") }
  var selectedExam by remember { mutableStateOf("General") }

  var isLoading by remember { mutableStateOf(false) }
  var analysisResult by remember { mutableStateOf<String?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isSaved by remember { mutableStateOf(false) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedImageUri = uri
      scope.launch(Dispatchers.IO) {
        try {
          val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
          } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
          }
          withContext(Dispatchers.Main) {
            selectedBitmap = bitmap
            analysisResult = null
            errorMessage = null
            isSaved = false
          }
        } catch (e: Exception) {
          withContext(Dispatchers.Main) {
            errorMessage = "Failed to load image: ${e.message}"
          }
        }
      }
    }
  }

  val actions = listOf(
    "Explain this page",
    "Summarize this page",
    "Solve the questions",
    "Generate MCQs",
    "Extract formulas",
    "Make revision notes"
  )

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
        text = "SnapStudy AI Scanner",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = "Smart textbook, notes, and problem sheet document analysis",
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
      // Image Upload & Preview Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          if (selectedBitmap != null) {
            Image(
              bitmap = selectedBitmap!!.asImageBitmap(),
              contentDescription = "Uploaded document preview",
              modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp)),
              contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
              onClick = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Select Different Image", fontSize = 13.sp)
            }
          } else {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF1F5F9)),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  imageVector = Icons.Default.CameraAlt,
                  contentDescription = null,
                  tint = NovaBlue,
                  modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Upload textbook page, worksheet, or handwritten notes",
                  fontSize = 13.sp,
                  color = Color(0xFF64748B)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
              onClick = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = NovaBlue),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_image_button")
            ) {
              Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Select Document / Image", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action configuration
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Select Analysis Action",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          var actionExpanded by remember { mutableStateOf(false) }
          ExposedDropdownMenuBox(
            expanded = actionExpanded,
            onExpandedChange = { actionExpanded = !actionExpanded },
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = selectedAction,
              onValueChange = {},
              readOnly = true,
              label = { Text("What should SnapStudy do?") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionExpanded) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(
              expanded = actionExpanded,
              onDismissRequest = { actionExpanded = false }
            ) {
              actions.forEach { act ->
                DropdownMenuItem(
                  text = { Text(act) },
                  onClick = {
                    selectedAction = act
                    actionExpanded = false
                  }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              if (selectedBitmap != null) {
                isLoading = true
                errorMessage = null
                isSaved = false
                scope.launch {
                  val result = aiService.analyzeDocumentImage(
                    bitmap = selectedBitmap!!,
                    action = selectedAction,
                    exam = selectedExam
                  )
                  isLoading = false
                  result.onSuccess {
                    analysisResult = it
                  }.onFailure { err ->
                    errorMessage = err.localizedMessage ?: "Failed to analyze document."
                  }
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("analyze_document_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
            shape = RoundedCornerShape(10.dp),
            enabled = !isLoading && selectedBitmap != null
          ) {
            if (isLoading) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Scanning & Processing...")
            } else {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Run SnapStudy Analysis", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(14.dp))
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

      analysisResult?.let { resultText ->
        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Analysis Result",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )

          Button(
            onClick = {
              val material = StudyMaterial(
                materialId = "scan_${System.currentTimeMillis()}",
                title = "SnapStudy: $selectedAction",
                topic = selectedAction,
                subject = "Document Scan",
                type = "notes",
                contentJson = resultText,
                source = "scanner"
              )
              onSaveToLibrary(material)
              isSaved = true
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isSaved) Color(0xFF10B981) else NovaBlue
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .height(36.dp)
              .testTag("save_scan_button")
          ) {
            Icon(
              imageVector = if (isSaved) Icons.Default.Check else Icons.Default.BookmarkAdd,
              contentDescription = null,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isSaved) "Saved to Library!" else "Save Notes", fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Text(
            text = resultText,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    }
  }
}
