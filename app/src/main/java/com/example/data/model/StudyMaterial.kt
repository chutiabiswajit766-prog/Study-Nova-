package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class StudyMaterial(
  @DocumentId val id: String = "",
  val materialId: String = "",
  val userId: String = "",
  val title: String = "",
  val topic: String = "",
  val subject: String = "",
  val exam: String = "",
  val type: String = "summary", // summary, notes, formula_sheet, worksheet, mind_map
  val contentJson: String = "",
  val source: String = "ai_generator", // ai_generator, scanner, tutor, manual
  val isFavorite: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "materialId" to materialId,
    "userId" to userId,
    "title" to title,
    "topic" to topic,
    "subject" to subject,
    "exam" to exam,
    "type" to type,
    "contentJson" to contentJson,
    "source" to source,
    "isFavorite" to isFavorite,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
  ).filterValues { it != null }
}
