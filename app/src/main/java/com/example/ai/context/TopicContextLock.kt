package com.example.ai.context

import com.example.ai.subject.AcademicSubject
import com.example.ai.subject.SubjectClassifier
import java.util.UUID

/**
 * Strict context lock representing the verified state for every educational request.
 * Every generation request receives a newly minted requestId to guarantee zero
 * cross-request leakage or stale conversation bleed.
 */
data class TopicContextLock(
  val requestId: String = UUID.randomUUID().toString(),
  val subject: AcademicSubject,
  val topic: String,
  val subtopic: String,
  val exam: String,
  val educationLevel: String,
  val language: String,
  val difficulty: String,
  val task: String,
  val sourceMode: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isAmbiguous: Boolean = false,
  val ambiguityClarification: String? = null,
  val clarificationOptions: List<String> = emptyList()
) {

  fun toPromptContext(): String {
    return """
      [TOPIC CONTEXT LOCK: REQUEST #$requestId]
      - Primary Academic Subject: ${subject.displayName}
      - Canonical Topic: $topic
      - Active Subtopic / Domain: $subtopic
      - Target Examination: $exam
      - Education Level: $educationLevel
      - Pedagogical Difficulty: $difficulty
      - Target Language: $language
      - Task Mode: $task
      - Source Mode: $sourceMode
      --------------------------------------------------
      CRITICAL SYSTEM INVARIANT:
      You MUST confine 100% of your response to the Academic Subject (${subject.displayName}) and Topic ($topic).
      Under NO circumstances may you mention, substitute, or cross-bleed concepts from another subject.
    """.trimIndent()
  }

  companion object {
    fun create(
      rawTopicOrMaterial: String,
      userSelectedSubjectStr: String?,
      exam: String = "General",
      difficulty: String = "Medium",
      language: String = "English",
      task: String = "Study Workspace Generation",
      sourceMode: String = "Topic-based"
    ): TopicContextLock {
      val parsedSubject = AcademicSubject.fromString(userSelectedSubjectStr)
      val classification = SubjectClassifier.classifyTopic(rawTopicOrMaterial, parsedSubject)

      if (classification.isAmbiguous) {
        return TopicContextLock(
          subject = AcademicSubject.OTHER,
          topic = rawTopicOrMaterial.trim(),
          subtopic = "Ambiguous Concept",
          exam = exam,
          educationLevel = "Multi-Level",
          language = language,
          difficulty = difficulty,
          task = task,
          sourceMode = sourceMode,
          isAmbiguous = true,
          ambiguityClarification = classification.clarificationPrompt,
          clarificationOptions = classification.ambiguityOptions
        )
      }

      // Check for direct mismatch: if user picked Chemistry for "Newton's laws", lock to Physics!
      val mismatch = SubjectClassifier.checkSubjectMismatch(rawTopicOrMaterial, parsedSubject)
      val finalSubject = if (mismatch.isMismatch) mismatch.detectedSubject else classification.subject

      return TopicContextLock(
        subject = finalSubject,
        topic = classification.topic,
        subtopic = classification.subtopics.firstOrNull() ?: classification.topic,
        exam = if (exam.isBlank() || exam == "General") classification.relevantExams.firstOrNull() ?: "General" else exam,
        educationLevel = classification.educationLevel,
        language = language,
        difficulty = difficulty,
        task = task,
        sourceMode = sourceMode
      )
    }
  }
}
