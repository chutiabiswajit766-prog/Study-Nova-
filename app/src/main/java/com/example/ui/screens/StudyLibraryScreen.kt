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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyMaterial
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaViolet

@Composable
fun StudyLibraryScreen(
  materials: List<StudyMaterial>,
  onToggleFavorite: (StudyMaterial) -> Unit,
  onDeleteMaterial: (materialId: String) -> Unit
) {
  val clipboardManager = LocalClipboardManager.current

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategoryIndex by remember { mutableIntStateOf(0) }
  var viewingMaterial by remember { mutableStateOf<StudyMaterial?>(null) }
  var materialToDelete by remember { mutableStateOf<StudyMaterial?>(null) }

  val categories = listOf("All Items", "Summaries", "Scanner Notes", "Formula Sheets", "Favorites")

  val filteredMaterials = materials.filter { item ->
    val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
      item.topic.contains(searchQuery, ignoreCase = true) ||
      item.subject.contains(searchQuery, ignoreCase = true)

    val matchesCategory = when (selectedCategoryIndex) {
      0 -> true
      1 -> item.type == "summary"
      2 -> item.source == "scanner"
      3 -> item.type == "formula_sheet"
      4 -> item.isFavorite
      else -> true
    }

    matchesSearch && matchesCategory
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
      Text(
        text = "Notes & Study Library",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = "${materials.size} saved study materials in cloud storage",
        fontSize = 12.sp,
        color = Color(0xFFBAE6FD)
      )
    }

    // Search Bar
    Box(modifier = Modifier.padding(16.dp)) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search your study library...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NovaBlue) },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Close, contentDescription = "Clear")
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("library_search_field"),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = NovaBlue,
          unfocusedBorderColor = Color(0xFFCBD5E1)
        ),
        singleLine = true
      )
    }

    // Category Filter Chips
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(categories.size) { idx ->
        val isSelected = selectedCategoryIndex == idx
        Surface(
          onClick = { selectedCategoryIndex = idx },
          shape = RoundedCornerShape(20.dp),
          color = if (isSelected) NovaBlue else MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = categories[idx],
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Items list
    if (filteredMaterials.isEmpty()) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Bookmark,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (materials.isEmpty()) "Your Study Library is empty.\nGenerate summaries or scan pages to save them!" else "No materials match your search.",
            color = Color(0xFF64748B),
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredMaterials) { item ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewingMaterial = item }
              .testTag("material_item_${item.materialId}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(
                    if (item.source == "scanner") NovaViolet.copy(alpha = 0.15f) else NovaBlue.copy(alpha = 0.15f),
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (item.source == "scanner") Icons.Default.CameraAlt else Icons.Default.Description,
                  contentDescription = null,
                  tint = if (item.source == "scanner") NovaViolet else NovaBlue,
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.title,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1
                )
                Text(
                  text = "${item.subject.ifEmpty { "General" }} • ${item.type.replace('_', ' ')}",
                  fontSize = 12.sp,
                  color = Color.Gray
                )
              }

              // Favorite toggle
              IconButton(onClick = { onToggleFavorite(item) }) {
                Icon(
                  imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = "Favorite",
                  tint = if (item.isFavorite) Color(0xFFEF4444) else Color.Gray,
                  modifier = Modifier.size(20.dp)
                )
              }

              // Delete button
              IconButton(onClick = { materialToDelete = item }) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Delete",
                  tint = Color.Gray,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }

  // Material Viewer Modal Dialog
  viewingMaterial?.let { item ->
    AlertDialog(
      onDismissRequest = { viewingMaterial = null },
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
          IconButton(onClick = { viewingMaterial = null }) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .verticalScroll(rememberScrollState())
        ) {
          Surface(
            color = NovaBlue.copy(alpha = 0.08f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "${item.subject} • ${item.exam} • Source: ${item.source}",
              fontSize = 11.sp,
              color = NovaBlue,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = item.contentJson,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            clipboardManager.setText(AnnotatedString(item.contentJson))
            viewingMaterial = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = NovaBlue)
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Copy Content")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { viewingMaterial = null }) {
          Text("Done")
        }
      }
    )
  }

  // Delete Confirmation Dialog
  materialToDelete?.let { item ->
    AlertDialog(
      onDismissRequest = { materialToDelete = null },
      title = { Text("Delete Material?") },
      text = { Text("Are you sure you want to delete '${item.title}' from your study library?") },
      confirmButton = {
        Button(
          onClick = {
            onDeleteMaterial(item.materialId)
            materialToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { materialToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}
