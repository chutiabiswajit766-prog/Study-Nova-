package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "Student",
  val email: String = "",
  val selectedExam: String = "JEE Main",
  val targetLanguage: String = "English",
  val studyHoursPerDay: Long = 4L,
  val points: Long = 100L,
  val streakDays: Long = 1L,
  val level: Long = 1L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "userId" to userId,
    "displayName" to displayName,
    "email" to email,
    "selectedExam" to selectedExam,
    "targetLanguage" to targetLanguage,
    "studyHoursPerDay" to studyHoursPerDay,
    "points" to points,
    "streakDays" to streakDays,
    "level" to level,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
  ).filterValues { it != null }
}
