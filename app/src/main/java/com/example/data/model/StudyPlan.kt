package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class StudyPlan(
  @DocumentId val id: String = "",
  val planId: String = "",
  val userId: String = "",
  val exam: String = "",
  val targetDate: String = "",
  val hoursPerDay: Long = 4L,
  val dailyTasksJson: String = "",
  val progressPercentage: Double = 0.0,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "planId" to planId,
    "userId" to userId,
    "exam" to exam,
    "targetDate" to targetDate,
    "hoursPerDay" to hoursPerDay,
    "dailyTasksJson" to dailyTasksJson,
    "progressPercentage" to progressPercentage,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
  ).filterValues { it != null }
}
