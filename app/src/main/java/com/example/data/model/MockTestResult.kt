package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class MockTestResult(
  @DocumentId val id: String = "",
  val resultId: String = "",
  val userId: String = "",
  val exam: String = "",
  val subject: String = "",
  val totalQuestions: Long = 0L,
  val correctAnswers: Long = 0L,
  val incorrectAnswers: Long = 0L,
  val unattempted: Long = 0L,
  val score: Double = 0.0,
  val maxScore: Double = 0.0,
  val percentage: Double = 0.0,
  val accuracy: Double = 0.0,
  val timeTakenSeconds: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "resultId" to resultId,
    "userId" to userId,
    "exam" to exam,
    "subject" to subject,
    "totalQuestions" to totalQuestions,
    "correctAnswers" to correctAnswers,
    "incorrectAnswers" to incorrectAnswers,
    "unattempted" to unattempted,
    "score" to score,
    "maxScore" to maxScore,
    "percentage" to percentage,
    "accuracy" to accuracy,
    "timeTakenSeconds" to timeTakenSeconds,
    "createdAt" to createdAt
  ).filterValues { it != null }
}
