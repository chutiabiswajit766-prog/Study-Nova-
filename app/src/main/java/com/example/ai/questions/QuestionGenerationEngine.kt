package com.example.ai.questions

import com.example.ai.context.TopicContextLock
import com.example.data.model.Question
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class QuestionType(val displayName: String) {
  MCQ("Multiple Choice (Single Correct)"),
  NUMERICAL("Numerical / Computational"),
  ASSERTION_REASON("Assertion and Reason"),
  CONCEPTUAL("Conceptual Inquiry"),
  SHORT_ANSWER("Short Answer"),
  LONG_ANSWER("Long Answer"),
  APPLICATION_BASED("Application & Problem Solving"),
  CASE_BASED("Case-Based / Comprehension")
}

data class QuestionDistribution(
  val easyCount: Int,
  val mediumCount: Int,
  val hardCount: Int
) {
  val total: Int get() = easyCount + mediumCount + hardCount

  companion object {
    fun balanced(total: Int): QuestionDistribution {
      val easy = (total * 0.25).toInt().coerceAtLeast(1)
      val hard = (total * 0.25).toInt().coerceAtLeast(1)
      val medium = (total - easy - hard).coerceAtLeast(0)
      return QuestionDistribution(easy, medium, hard)
    }
  }
}

data class StructuredSolution(
  val givenInformation: String,
  val requiredResult: String,
  val relevantConcept: String,
  val formulaOrPrinciple: String,
  val stepByStepWorking: List<String>,
  val finalAnswer: String,
  val unitVerification: String,
  val conceptualExplanation: String
)

object QuestionGenerationEngine {

  fun buildQuestionGenerationPrompt(
    contextLock: TopicContextLock,
    distribution: QuestionDistribution,
    allowedTypes: List<QuestionType> = listOf(QuestionType.MCQ)
  ): String {
    val typesStr = allowedTypes.joinToString(", ") { it.displayName }
    return """
Generate a strict set of ${distribution.total} academic questions for:
Subject: ${contextLock.subject.displayName}
Topic: ${contextLock.topic}
Subtopic: ${contextLock.subtopic}
Target Examination: ${contextLock.exam}
Difficulty Distribution: ${distribution.easyCount} Easy, ${distribution.mediumCount} Medium, ${distribution.hardCount} Hard
Permitted Question Formats: $typesStr

CRITICAL QUALITY INVARIANTS:
1. ZERO SUBJECT CONFUSION: Every single question must be 100% directly related to "${contextLock.topic}" in ${contextLock.subject.displayName}.
2. STRICT MCQ RULES:
   - Provide EXACTLY four distinct, meaningful options (Option A, B, C, D). No duplicate options.
   - Designate EXACTLY one clearly correct option (index 0, 1, 2, or 3).
   - The explanation MUST explicitly state why the chosen option is correct and why other options are incorrect.
   - The correctOptionIndex MUST strictly match the correct option text.
3. NUMERICAL & CONCEPTUAL PRECISION:
   - State all given variables with standard SI units.
   - Do not generate trick questions with ambiguous wording.
4. BRANDING & PROVENANCE:
   - Always label source as "StudyNova AI Practice Paper", NEVER claim it is an official past examination question.

Return raw valid JSON array ONLY (no code fences, no markdown):
[
  {
    "id": "q1",
    "questionType": "MCQ",
    "questionText": "Question statement?",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correctOptionIndex": 0,
    "correctAnswer": "Option A text",
    "difficulty": "Medium",
    "topic": "${contextLock.topic}",
    "subtopic": "${contextLock.subtopic}",
    "explanation": "Clear, verified step-by-step rationale showing why Option A is correct.",
    "structuredSolution": {
      "givenInformation": "Given quantities...",
      "requiredResult": "What to calculate...",
      "relevantConcept": "Core theorem...",
      "formulaOrPrinciple": "Formula...",
      "stepByStepWorking": ["Step 1", "Step 2"],
      "finalAnswer": "Value and unit",
      "unitVerification": "Dimensional check",
      "conceptualExplanation": "Underlying insight"
    }
  }
]
""".trimIndent()
  }

  fun parseAndValidateQuestions(
    rawJson: String,
    contextLock: TopicContextLock
  ): List<Question> {
    val clean = cleanJson(rawJson)
    val questions = mutableListOf<Question>()

    try {
      val jsonArray = if (clean.startsWith("[")) {
        JSONArray(clean)
      } else if (clean.startsWith("{")) {
        val obj = JSONObject(clean)
        obj.optJSONArray("questions") ?: JSONArray()
      } else {
        JSONArray()
      }

      for (i in 0 until jsonArray.length()) {
        val item = jsonArray.getJSONObject(i)
        val optsArr = item.optJSONArray("options") ?: JSONArray()
        val opts = (0 until optsArr.length()).map { optsArr.getString(it).trim() }

        val rawCorrectIndex = item.optInt("correctOptionIndex", 0)
        val explanation = item.optString("explanation", "Verified answer based on ${contextLock.topic} principles.")
        val questionText = item.optString("questionText", "Question ${i + 1}")

        // Validate 4 options
        val finalOptions = if (opts.size >= 4) {
          opts.take(4)
        } else {
          listOf("Option A", "Option B", "Option C", "Option D")
        }

        // Check alignment between explanation and correct option
        var validatedIndex = rawCorrectIndex.coerceIn(0, 3)
        for ((idx, opt) in finalOptions.withIndex()) {
          if (explanation.contains(opt, ignoreCase = true) && !explanation.contains(finalOptions[validatedIndex], ignoreCase = true)) {
            validatedIndex = idx
            break
          }
        }

        val q = Question(
          id = item.optString("id", "sn_q_${UUID.randomUUID().toString().take(8)}"),
          exam = contextLock.exam,
          subject = contextLock.subject.displayName,
          topic = item.optString("topic", contextLock.topic),
          questionText = questionText,
          options = finalOptions,
          correctOptionIndex = validatedIndex,
          explanation = explanation,
          difficulty = item.optString("difficulty", contextLock.difficulty),
          source = "StudyNova Practice Question (${contextLock.subject.displayName})"
        )
        questions.add(q)
      }
    } catch (e: Throwable) {
      // Fallback parser for JVM environments where Android's org.json is stubbed
      val blocks = clean.split(Regex("\\{[\\s\\r\\n]*\"id\""))
      for ((idx, block) in blocks.withIndex()) {
        if (idx == 0 && !block.contains("questionText")) continue
        val qText = Regex("\"questionText\"\\s*:\\s*\"([^\"]+)\"").find(block)?.groupValues?.get(1) ?: "Question $idx"
        val explanation = Regex("\"explanation\"\\s*:\\s*\"([^\"]+)\"").find(block)?.groupValues?.get(1) ?: "Verified answer based on ${contextLock.topic}."
        val correctIdx = Regex("\"correctOptionIndex\"\\s*:\\s*([0-9]+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        questions.add(
          Question(
            id = "q$idx",
            exam = contextLock.exam,
            subject = contextLock.subject.displayName,
            topic = contextLock.topic,
            questionText = qText,
            options = listOf("Option A", "Option B", "Option C", "Option D"),
            correctOptionIndex = correctIdx.coerceIn(0, 3),
            explanation = explanation,
            difficulty = contextLock.difficulty,
            source = "StudyNova Practice Question (${contextLock.subject.displayName})"
          )
        )
      }
    }

    return questions
  }

  private fun cleanJson(raw: String): String {
    var s = raw.trim()
    if (s.startsWith("```json")) s = s.substring(7)
    if (s.startsWith("```")) s = s.substring(3)
    if (s.endsWith("```")) s = s.substring(0, s.length - 3)
    return s.trim()
  }
}
