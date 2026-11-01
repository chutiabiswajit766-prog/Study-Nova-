package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.Question
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "GeminiAiService"

data class StudyWorkspaceResult(
  val shortSummary: String,
  val detailedSummary: String,
  val simpleExplanation: String,
  val importantDefinitions: List<String>,
  val formulaSheet: List<String>,
  val mcqs: List<Question>,
  val questionsAndAnswers: List<Pair<String, String>>,
  val flashcards: List<Pair<String, String>>,
  val mindMapOutline: List<String>
)

data class EvaluationResult(
  val score: Int,
  val maxScore: Int,
  val conceptualAccuracy: String,
  val keyPointsCovered: List<String>,
  val missingPoints: List<String>,
  val suggestions: String,
  val sampleAnswer: String
)

class GeminiAiService {

  // Mandated: Lazy evaluation of generative model
  val generalModel by lazy {
    Firebase.ai.generativeModel(modelName = "gemini-3.8-flash")
  }

  // 1. Generate full structured study workspace
  suspend fun generateStudyWorkspace(
    topicOrMaterial: String,
    exam: String = "General",
    subject: String = "General Science",
    difficulty: String = "Intermediate",
    language: String = "English",
    bitmap: Bitmap? = null
  ): Result<StudyWorkspaceResult> {
    return try {
      val prompt = """
        You are StudyNova AI, an expert educational architect and master teacher.
        Generate a comprehensive, high-quality, structured study workspace for the following input:
        Topic/Material: $topicOrMaterial
        Target Examination: $exam
        Subject: $subject
        Difficulty Level: $difficulty
        Target Language: $language

        Return your output as a clean JSON object with EXACTLY this structure (no markdown wrappers):
        {
          "shortSummary": "A concise 2-3 sentence executive summary",
          "detailedSummary": "A structured in-depth breakdown of the topic with core concepts",
          "simpleExplanation": "An intuitive, real-world analogy explanation suitable for quick understanding",
          "importantDefinitions": ["Definition 1 with term and meaning", "Definition 2"],
          "formulaSheet": ["Equation 1 with units and conditions", "Equation 2"],
          "mcqs": [
            {
              "questionText": "Question text here?",
              "options": ["Option A", "Option B", "Option C", "Option D"],
              "correctOptionIndex": 0,
              "explanation": "Detailed rationale why Option A is correct",
              "topic": "$subject"
            }
          ],
          "questionsAndAnswers": [
            {"question": "Core question?", "answer": "Clear comprehensive answer"}
          ],
          "flashcards": [
            {"front": "Key Term or Question", "back": "Precise Definition or Answer"}
          ],
          "mindMapOutline": [
            "Root Concept",
            "  -> Subtopic A",
            "     * Principle 1",
            "  -> Subtopic B",
            "     * Principle 2"
          ]
        }
      """.trimIndent()

      val responseText = withTimeout(60_000L) {
        if (bitmap != null) {
          val response = generalModel.generateContent(
            content {
              image(bitmap)
              text(prompt)
            }
          )
          response.text
        } else {
          val response = generalModel.generateContent(prompt)
          response.text
        }
      } ?: throw IllegalStateException("Model returned empty response.")

      val cleanJson = cleanJsonString(responseText)
      val json = JSONObject(cleanJson)

      val shortSummary = json.optString("shortSummary", "Summary generated.")
      val detailedSummary = json.optString("detailedSummary", "Detailed summary generated.")
      val simpleExplanation = json.optString("simpleExplanation", "Concept explanation.")

      val defsArray = json.optJSONArray("importantDefinitions") ?: JSONArray()
      val definitions = (0 until defsArray.length()).map { defsArray.getString(it) }

      val formulaArray = json.optJSONArray("formulaSheet") ?: JSONArray()
      val formulas = (0 until formulaArray.length()).map { formulaArray.getString(it) }

      val mcqsList = mutableListOf<Question>()
      val mcqsArray = json.optJSONArray("mcqs") ?: JSONArray()
      for (i in 0 until mcqsArray.length()) {
        val qObj = mcqsArray.getJSONObject(i)
        val optsArr = qObj.optJSONArray("options") ?: JSONArray()
        val opts = (0 until optsArr.length()).map { optsArr.getString(it) }
        mcqsList.add(
          Question(
            id = "mcq_$i",
            exam = exam,
            subject = subject,
            topic = qObj.optString("topic", subject),
            questionText = qObj.optString("questionText", "Question $i"),
            options = if (opts.size >= 4) opts else listOf("Option A", "Option B", "Option C", "Option D"),
            correctOptionIndex = qObj.optInt("correctOptionIndex", 0).coerceIn(0, 3),
            explanation = qObj.optString("explanation", "Correct based on core principles."),
            difficulty = difficulty,
            source = "StudyNova AI Generated"
          )
        )
      }

      val qaList = mutableListOf<Pair<String, String>>()
      val qaArray = json.optJSONArray("questionsAndAnswers") ?: JSONArray()
      for (i in 0 until qaArray.length()) {
        val obj = qaArray.getJSONObject(i)
        qaList.add(Pair(obj.optString("question", ""), obj.optString("answer", "")))
      }

      val fcList = mutableListOf<Pair<String, String>>()
      val fcArray = json.optJSONArray("flashcards") ?: JSONArray()
      for (i in 0 until fcArray.length()) {
        val obj = fcArray.getJSONObject(i)
        fcList.add(Pair(obj.optString("front", ""), obj.optString("back", "")))
      }

      val mmArray = json.optJSONArray("mindMapOutline") ?: JSONArray()
      val mindMap = (0 until mmArray.length()).map { mmArray.getString(it) }

      Result.success(
        StudyWorkspaceResult(
          shortSummary = shortSummary,
          detailedSummary = detailedSummary,
          simpleExplanation = simpleExplanation,
          importantDefinitions = definitions,
          formulaSheet = formulas,
          mcqs = mcqsList,
          questionsAndAnswers = qaList,
          flashcards = fcList,
          mindMapOutline = mindMap
        )
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Study workspace generation failed", e)
      Result.failure(e)
    }
  }

  // 2. SnapStudy Scanner: Analyze page / image with selected action
  suspend fun analyzeDocumentImage(
    bitmap: Bitmap,
    action: String,
    exam: String = "General",
    language: String = "English"
  ): Result<String> {
    return try {
      val actionPrompt = when (action) {
        "Explain this page" -> "Explain all key concepts, equations, and diagrams visible on this page clearly and thoroughly."
        "Summarize this page" -> "Provide an executive summary and high-yield bulleted summary of this page."
        "Solve the questions" -> "Identify all questions, exercises, or problems on this page. Provide complete, step-by-step verified solutions with equations."
        "Generate MCQs" -> "Extract the core concepts from this page and generate 5 exam-style MCQs with 4 options each and explanations."
        "Extract formulas" -> "Extract all mathematical and scientific formulas from this page with variable definitions and units."
        "Make revision notes" -> "Convert this page into high-yield revision notes with bolded key terms and takeaways."
        else -> "Analyze and explain the educational contents of this page."
      }

      val prompt = """
        You are SnapStudy AI in StudyNova.
        User selected action: $actionPrompt
        Target Exam Context: $exam
        Language: $language

        Preserve all mathematical formulas, chemical structures, symbols, and technical terms.
        If any portion is handwritten and ambiguous, note the uncertainty explicitly.
        Provide a structured, beautifully formatted response in clean Markdown with clear headings.
      """.trimIndent()

      val responseText = withTimeout(60_000L) {
        val response = generalModel.generateContent(
          content {
            image(bitmap)
            text(prompt)
          }
        )
        response.text
      } ?: throw IllegalStateException("Model returned empty response.")

      Result.success(responseText)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "SnapStudy document analysis failed", e)
      Result.failure(e)
    }
  }

  // 3. AI Tutor streaming chat response
  fun streamTutorResponse(
    userPrompt: String,
    conversationHistory: List<Pair<String, String>> = emptyList(),
    examContext: String = "General",
    language: String = "English"
  ): Flow<String> = flow {
    val systemContext = """
      You are the StudyNova AI Personal Tutor, an encouraging, pedagogically skilled teacher.
      The student is preparing for: $examContext.
      Preferred Language: $language.
      Explain concepts clearly from fundamentals to advanced nuance, break difficult math into small steps, and test comprehension when appropriate.
    """.trimIndent()

    val promptWithHistory = buildString {
      append(systemContext)
      append("\n\nRecent Conversation:\n")
      conversationHistory.takeLast(6).forEach { (role, msg) ->
        append("$role: $msg\n")
      }
      append("\nUser: $userPrompt\nTutor:")
    }

    try {
      withTimeout(60_000L) {
        generalModel.generateContentStream(promptWithHistory).collect { chunk ->
          val chunkText = chunk.text.orEmpty()
          if (chunkText.isNotEmpty()) {
            emit(chunkText)
          }
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Tutor streaming failed", e)
      emit("\n[Error: ${e.localizedMessage ?: "Failed to receive response"}]")
    }
  }

  // 4. Generate Timed Mock Test Questions
  suspend fun generateMockTestQuestions(
    exam: String,
    subject: String,
    questionCount: Int = 10,
    difficulty: String = "Medium",
    language: String = "English"
  ): Result<List<Question>> {
    return try {
      val prompt = """
        You are StudyNova AI's examination paper generator.
        Create a genuine, high-quality, timed practice test for:
        Exam: $exam
        Subject: $subject
        Number of questions: $questionCount
        Difficulty: $difficulty
        Language: $language

        Adhere strictly to the actual question style, standard syllabus, and difficulty of $exam.
        Return raw valid JSON array ONLY (no markdown code fence):
        [
          {
            "id": "q1",
            "questionText": "Question statement?",
            "options": ["Option A", "Option B", "Option C", "Option D"],
            "correctOptionIndex": 0,
            "explanation": "Clear step-by-step reason why option 0 is correct",
            "topic": "Topic Name",
            "difficulty": "$difficulty"
          }
        ]
      """.trimIndent()

      val responseText = withTimeout(60_000L) {
        generalModel.generateContent(prompt).text
      } ?: throw IllegalStateException("Model returned empty text.")

      val cleanJson = cleanJsonString(responseText)
      val jsonArray = JSONArray(cleanJson)
      val questions = mutableListOf<Question>()

      for (i in 0 until jsonArray.length()) {
        val qObj = jsonArray.getJSONObject(i)
        val optsArr = qObj.optJSONArray("options") ?: JSONArray()
        val opts = (0 until optsArr.length()).map { optsArr.getString(it) }
        questions.add(
          Question(
            id = "mock_q_${System.currentTimeMillis()}_$i",
            exam = exam,
            subject = subject,
            topic = qObj.optString("topic", subject),
            questionText = qObj.optString("questionText", "Question ${i + 1}"),
            options = if (opts.size >= 4) opts else listOf("Option A", "Option B", "Option C", "Option D"),
            correctOptionIndex = qObj.optInt("correctOptionIndex", 0).coerceIn(0, 3),
            explanation = qObj.optString("explanation", "Verified conceptual solution."),
            difficulty = qObj.optString("difficulty", difficulty),
            source = "$exam Mock Engine"
          )
        )
      }

      Result.success(questions)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Mock test generation failed", e)
      Result.failure(e)
    }
  }

  // 5. AI Answer Evaluator
  suspend fun evaluateAnswer(
    question: String,
    studentAnswer: String,
    exam: String = "UPSC",
    subject: String = "General Studies"
  ): Result<EvaluationResult> {
    return try {
      val prompt = """
        You are an expert exam evaluator for $exam ($subject).
        Evaluate this student's written response against an official assessment rubric:
        Question: $question
        Student's Answer: $studentAnswer

        Return a raw valid JSON object ONLY:
        {
          "score": 7,
          "maxScore": 10,
          "conceptualAccuracy": "Strong conceptual grounding with minor gap in second paragraph",
          "keyPointsCovered": ["Point 1", "Point 2"],
          "missingPoints": ["Point 3 that should have been cited"],
          "suggestions": "Suggestions on structure and terminology",
          "sampleAnswer": "Model ideal answer for study"
        }
      """.trimIndent()

      val responseText = withTimeout(60_000L) {
        generalModel.generateContent(prompt).text
      } ?: throw IllegalStateException("Model returned empty response.")

      val cleanJson = cleanJsonString(responseText)
      val json = JSONObject(cleanJson)

      val kpArr = json.optJSONArray("keyPointsCovered") ?: JSONArray()
      val kp = (0 until kpArr.length()).map { kpArr.getString(it) }

      val mpArr = json.optJSONArray("missingPoints") ?: JSONArray()
      val mp = (0 until mpArr.length()).map { mpArr.getString(it) }

      Result.success(
        EvaluationResult(
          score = json.optInt("score", 7),
          maxScore = json.optInt("maxScore", 10),
          conceptualAccuracy = json.optString("conceptualAccuracy", "Good effort."),
          keyPointsCovered = kp,
          missingPoints = mp,
          suggestions = json.optString("suggestions", "Add more direct examples and units."),
          sampleAnswer = json.optString("sampleAnswer", "Sample benchmark answer.")
        )
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Answer evaluation failed", e)
      Result.failure(e)
    }
  }

  // 6. Generate Personalized Study Timetable
  suspend fun generateStudyPlan(
    exam: String,
    targetDate: String,
    dailyHours: Int,
    subjects: List<String>
  ): Result<String> {
    return try {
      val prompt = """
        You are StudyNova AI's academic scheduler.
        Generate a realistic, balanced study plan for:
        Examination: $exam
        Target Date: $targetDate
        Daily Hours: $dailyHours hours/day
        Target Subjects: ${subjects.joinToString(", ")}

        Return a raw valid JSON object with:
        {
          "overview": "Summary of plan and strategy",
          "dailyRoutine": [
            {"time": "08:00 - 10:00", "task": "Core Subject Theory", "focus": "Deep work"},
            {"time": "14:00 - 15:30", "task": "Practice MCQs & Numericals", "focus": "Speed & accuracy"},
            {"time": "19:00 - 20:30", "task": "Revision & Flashcards", "focus": "Spaced repetition"}
          ],
          "weeklyMilestones": [
            "Week 1: High-weightage foundational chapters",
            "Week 2: Secondary chapters and initial mock test",
            "Week 3: Formula revision and weak area drilling",
            "Week 4: Full-length timed mock tests"
          ]
        }
      """.trimIndent()

      val responseText = withTimeout(60_000L) {
        generalModel.generateContent(prompt).text
      } ?: throw IllegalStateException("Empty plan response.")

      Result.success(cleanJsonString(responseText))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Study plan generation failed", e)
      Result.failure(e)
    }
  }

  private fun cleanJsonString(raw: String): String {
    var s = raw.trim()
    if (s.startsWith("```json")) {
      s = s.substring(7)
    } else if (s.startsWith("```")) {
      s = s.substring(3)
    }
    if (s.endsWith("```")) {
      s = s.substring(0, s.length - 3)
    }
    return s.trim()
  }
}
