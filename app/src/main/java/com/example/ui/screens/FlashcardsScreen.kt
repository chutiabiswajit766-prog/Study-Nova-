package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Flashcard
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaViolet

@Composable
fun FlashcardsScreen(
  flashcards: List<Flashcard>,
  onAddFlashcard: (Flashcard) -> Unit,
  onRateCard: (cardId: String, newLevel: Long, reviewCount: Long) -> Unit
) {
  // Local default flashcards if collection is initially empty
  val activeCards = remember(flashcards) {
    if (flashcards.isNotEmpty()) {
      flashcards
    } else {
      listOf(
        Flashcard(
          cardId = "fc_def_1",
          deckName = "Physics Mechanics",
          subject = "Physics",
          front = "State Newton's Second Law of Motion in vector form.",
          back = "F_net = dp/dt = m * a (for constant mass). The net external force on a body is equal to the rate of change of its linear momentum.",
          difficulty = "EASY"
        ),
        Flashcard(
          cardId = "fc_def_2",
          deckName = "Chemistry Equilibrium",
          subject = "Chemistry",
          front = "What is Le Chatelier's Principle?",
          back = "If a chemical system at dynamic equilibrium experiences a change in concentration, temperature, or pressure, the equilibrium shifts in the direction that counteracts the imposed change.",
          difficulty = "MEDIUM"
        ),
        Flashcard(
          cardId = "fc_def_3",
          deckName = "Indian Polity",
          subject = "Polity",
          front = "Which Constitutional Amendment introduced the Right to Education under Article 21A?",
          back = "The 86th Constitutional Amendment Act, 2002, made free and compulsory education a fundamental right for children aged 6 to 14 years.",
          difficulty = "HARD"
        )
      )
    }
  }

  var currentIndex by remember { mutableIntStateOf(0) }
  var isFlipped by remember { mutableStateOf(false) }
  var showAddDialog by remember { mutableStateOf(false) }

  val card = if (activeCards.isNotEmpty() && currentIndex < activeCards.size) activeCards[currentIndex] else null

  val rotation by animateFloatAsState(
    targetValue = if (isFlipped) 180f else 0f,
    animationSpec = tween(durationMillis = 350),
    label = "cardFlip"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Header
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(NovaBlue)
          .padding(horizontal = 20.dp, vertical = 14.dp)
      ) {
        Text(
          text = "Spaced Repetition Flashcards",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "Review cards with Leitner algorithm intervals",
          fontSize = 12.sp,
          color = Color(0xFFBAE6FD)
        )
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        if (card != null) {
          // Card indicator & Deck name
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(color = NovaBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
              Text(
                text = card.deckName,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = NovaBlue,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }

            Text(
              text = "Card ${currentIndex + 1} of ${activeCards.size}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Gray
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 3D Flip Flashcard
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .height(280.dp)
              .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
              }
              .clickable { isFlipped = !isFlipped }
              .testTag("flip_flashcard"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (rotation > 90f) NovaViolet.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .graphicsLayer {
                  if (rotation > 90f) rotationY = 180f
                },
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = if (rotation > 90f) "ANSWER" else "QUESTION",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = if (rotation > 90f) NovaViolet else NovaBlue,
                  letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                  text = if (rotation > 90f) card.back else card.front,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.SemiBold,
                  textAlign = TextAlign.Center,
                  lineHeight = 24.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                  text = "Tap card to flip",
                  fontSize = 11.sp,
                  color = Color.Gray
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Spaced Repetition Rating Buttons
          if (isFlipped) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  onRateCard(card.cardId, (card.repetitionLevel - 1).coerceAtLeast(0), card.reviewCount + 1)
                  isFlipped = false
                  if (currentIndex < activeCards.size - 1) currentIndex += 1
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Difficult", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = {
                  onRateCard(card.cardId, card.repetitionLevel, card.reviewCount + 1)
                  isFlipped = false
                  if (currentIndex < activeCards.size - 1) currentIndex += 1
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Medium", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = {
                  onRateCard(card.cardId, card.repetitionLevel + 1, card.reviewCount + 1)
                  isFlipped = false
                  if (currentIndex < activeCards.size - 1) currentIndex += 1
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Easy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          } else {
            Button(
              onClick = { isFlipped = true },
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
              colors = ButtonDefaults.buttonColors(containerColor = NovaBlue),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Reveal Answer", fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Navigation Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = {
                if (currentIndex > 0) {
                  currentIndex -= 1
                  isFlipped = false
                }
              },
              enabled = currentIndex > 0,
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Prev")
            }

            IconButton(
              onClick = {
                currentIndex = 0
                isFlipped = false
              }
            ) {
              Icon(Icons.Default.Refresh, contentDescription = "Restart Deck", tint = NovaBlue)
            }

            OutlinedButton(
              onClick = {
                if (currentIndex < activeCards.size - 1) {
                  currentIndex += 1
                  isFlipped = false
                }
              },
              enabled = currentIndex < activeCards.size - 1,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Next")
              Spacer(modifier = Modifier.width(4.dp))
              Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }

    // Floating action button to add custom flashcard
    FloatingActionButton(
      onClick = { showAddDialog = true },
      containerColor = NovaBlue,
      contentColor = Color.White,
      shape = CircleShape,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 80.dp, end = 20.dp)
        .testTag("add_flashcard_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add Flashcard")
    }
  }

  // Add Flashcard Modal Dialog
  if (showAddDialog) {
    var newFront by remember { mutableStateOf("") }
    var newBack by remember { mutableStateOf("") }
    var newDeck by remember { mutableStateOf("Custom") }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Create Flashcard") },
      text = {
        Column {
          OutlinedTextField(
            value = newDeck,
            onValueChange = { newDeck = it },
            label = { Text("Deck Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = newFront,
            onValueChange = { newFront = it },
            label = { Text("Front (Question or Term)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = newBack,
            onValueChange = { newBack = it },
            label = { Text("Back (Answer or Definition)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            minLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newFront.isNotBlank() && newBack.isNotBlank()) {
              onAddFlashcard(
                Flashcard(
                  cardId = "card_${System.currentTimeMillis()}",
                  deckName = newDeck,
                  front = newFront,
                  back = newBack,
                  difficulty = "MEDIUM"
                )
              )
              showAddDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = NovaBlue)
        ) {
          Text("Save Card")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showAddDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
