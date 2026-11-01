package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Flashcard(
  @DocumentId val id: String = "",
  val cardId: String = "",
  val userId: String = "",
  val deckName: String = "General",
  val subject: String = "",
  val front: String = "",
  val back: String = "",
  val difficulty: String = "MEDIUM", // EASY, MEDIUM, HARD
  val repetitionLevel: Long = 0L,
  val reviewCount: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "cardId" to cardId,
    "userId" to userId,
    "deckName" to deckName,
    "subject" to subject,
    "front" to front,
    "back" to back,
    "difficulty" to difficulty,
    "repetitionLevel" to repetitionLevel,
    "reviewCount" to reviewCount,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
  ).filterValues { it != null }
}
