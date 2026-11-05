package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.ai.context.TopicContextLock
import com.example.ai.prompts.SubjectExpertPrompts
import com.example.ai.questions.QuestionDistribution
import com.example.ai.questions.QuestionGenerationEngine
import com.example.ai.research.ResearchEngine
import com.example.ai.research.ResearchWorkflowResult
import com.example.ai.subject.AcademicSubject
import com.example.ai.subject.SubjectClassifier
import com.example.ai.validation.PipelineValidationResult
import com.example.ai.validation.ResponseValidationEngine
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
  val mindMapOutline: List<String>,
  val keyPoints: List<String> = emptyList(),
  val examples: List<String> = emptyList(),
  val examOrientedNotes: String = "",
  val commonMistakes: List<String> = emptyList(),
  val quickRevisionSheet: List<String> = emptyList(),
  val contextLock: TopicContextLock? = null,
  val researchResult: ResearchWorkflowResult? = null,
  val validationReport: PipelineValidationResult? = null
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

data class DocumentAnalysisResult(
  val rawText: String,
  val detectedSubject: AcademicSubject,
  val detectedTopic: String,
  val isUnreadable: Boolean = false,
  val unreadableReason: String? = null
)

class GeminiAiService {

  val generalModel by lazy {
    Firebase.ai.generativeModel(modelName = "gemini-3.8-flash")
  }

  /**
   * 1. Generate full structured study workspace with Strict Subject Locking,
   * Domain-Specific Expert Guidance, and 10-Point Deterministic Validation.
   */
  suspend fun generateStudyWorkspace(
    topicOrMaterial: String,
    exam: String = "General",
    subject: String = "General Science",
    difficulty: String = "Intermediate",
    language: String = "English",
    bitmap: Bitmap? = null
  ): Result<StudyWorkspaceResult> {
    return try {
      // Step A: Topic Context Lock & Subject Identification
      val contextLock = TopicContextLock.create(
        rawTopicOrMaterial = topicOrMaterial,
        userSelectedSubjectStr = subject,
        exam = exam,
        difficulty = difficulty,
        language = language,
        task = "Comprehensive Study Workspace & Summary Generation",
        sourceMode = if (bitmap != null) "Uploaded Document Analysis" else "Direct Topic Formulation"
      )

      // Handle Genuine Ambiguity
      if (contextLock.isAmbiguous) {
        val promptClarify = contextLock.ambiguityClarification ?: "Please clarify the subject."
        return Result.failure(
          IllegalArgumentException(
            "AMBIGUOUS_TOPIC: $promptClarify Options: ${contextLock.clarificationOptions.joinToString(" | ")}"
          )
        )
      }

      // Step B: Task-specific prompt construction
      val taskPrompt = """
Generate a rigorous, subject-specific academic study workspace for:
Topic: ${contextLock.topic}
Subtopic: ${contextLock.subtopic}
Target Exam: ${contextLock.exam}
Difficulty: ${contextLock.difficulty}
Language: ${contextLock.language}

Provide a comprehensive academic breakdown conforming strictly to ${contextLock.subject.displayName}.
Return your output as a clean JSON object with EXACTLY this structure (no markdown wrappers):
{
  "shortSummary": "A concise, high-yield executive summary (3-4 sentences)",
  "detailedSummary": "A structured, in-depth academic breakdown detailing mechanisms, theorems, and core principles",
  "simpleExplanation": "An intuitive, real-world analogy explanation suitable for deep conceptual intuition",
  "importantDefinitions": ["Definition 1 with formal term and exact scientific/mathematical meaning", "Definition 2"],
  "formulaSheet": ["Equation 1 with all variables, units, and boundary conditions", "Equation 2"],
  "keyPoints": ["High-yield bullet 1", "High-yield bullet 2", "High-yield bullet 3"],
  "examples": ["Concrete worked example 1 with values/reaction/proof", "Example 2"],
  "examOrientedNotes": "High-priority exam tips, typical question traps, and syllabus weightage notes for ${contextLock.exam}",
  "commonMistakes": ["Common misconception 1 and why students lose marks", "Mistake 2"],
  "quickRevisionSheet": ["Rapid-fire fact 1", "Rapid-fire fact 2", "Formula reminder 3"],
  "mcqs": [
    {
      "questionText": "Question statement directly testing ${contextLock.topic}?",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correctOptionIndex": 0,
      "explanation": "Clear step-by-step rationale why Option A is correct and why B, C, D are incorrect",
      "topic": "${contextLock.topic}"
    }
  ],
  "questionsAndAnswers": [
    {"question": "Core conceptual or analytical question?", "answer": "Clear, verified step-by-step model answer"}
  ],
  "flashcards": [
    {"front": "Key Term or Question", "back": "Precise Definition or Verified Solution"}
  ],
  "mindMapOutline": [
    "${contextLock.topic}",
    "  -> ${contextLock.subtopic}",
    "     * Core Law or Reaction",
    "  -> Secondary Concepts",
    "     * Applications & Boundary Conditions"
  ]
}
""".trimIndent()

      val fullPrompt = SubjectExpertPrompts.buildPrompt(contextLock, taskPrompt)

      // Step C: Model invocation with timeout
      var rawResponse = withTimeout(60_000L) {
        if (bitmap != null) {
          generalModel.generateContent(
            content {
              image(bitmap)
              text(fullPrompt)
            }
          ).text
        } else {
          generalModel.generateContent(fullPrompt).text
        }
      } ?: throw IllegalStateException("Model returned empty response.")

      // Step D: Parse payload
      var parsed = parseWorkspaceJson(rawResponse, contextLock)

      // Step E: 10-Point Response Validation
      var validation = ResponseValidationEngine.validateWorkspace(
        contextLock = contextLock,
        shortSummary = parsed.shortSummary,
        detailedSummary = parsed.detailedSummary,
        mcqs = parsed.mcqs,
        formulas = parsed.formulaSheet,
        definitions = parsed.importantDefinitions
      )

      // Step F: Controlled single retry if critical validation fails
      if (!validation.isValid && validation.shouldRetry) {
        Log.w(TAG, "Validation failed: ${validation.primaryFailureReason}. Executing controlled correction...")
        val correctivePrompt = """
$fullPrompt

PREVIOUS ATTEMPT VALIDATION FAILED:
${validation.correctivePromptAdvice}
Re-generate the JSON object ensuring 100% adherence to ${contextLock.subject.displayName} and ${contextLock.topic}. Fix the reported issues.
""".trimIndent()

        val retryResponse = withTimeout(60_000L) {
          generalModel.generateContent(correctivePrompt).text
        }
        if (!retryResponse.isNullOrBlank()) {
          val retryParsed = parseWorkspaceJson(retryResponse, contextLock)
          val retryValidation = ResponseValidationEngine.validateWorkspace(
            contextLock = contextLock,
            shortSummary = retryParsed.shortSummary,
            detailedSummary = retryParsed.detailedSummary,
            mcqs = retryParsed.mcqs,
            formulas = retryParsed.formulaSheet,
            definitions = retryParsed.importantDefinitions
          )
          if (retryValidation.isValid || retryParsed.detailedSummary.isNotBlank()) {
            parsed = retryParsed
            validation = retryValidation
          }
        }
      }

      // Step G: Attach Research Engine verification
      val research = ResearchEngine.executeResearchWorkflow(
        contextLock = contextLock,
        userQuery = topicOrMaterial,
        modelGeneratedExplanation = parsed.detailedSummary
      )

      Result.success(
        parsed.copy(
          contextLock = contextLock,
          researchResult = research,
          validationReport = validation
        )
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Study workspace generation failed", e)
      Result.failure(e)
    }
  }

  /**
   * 2. SnapStudy Scanner: Enhanced with image readability check, subject & topic detection,
   * formula preservation, and step-by-step problem solver.
   */
  suspend fun analyzeDocumentImage(
    bitmap: Bitmap,
    action: String,
    exam: String = "General",
    language: String = "English"
  ): Result<String> {
    return try {
      // Step A: Inspect image clarity & detect subject first
      val detectionPrompt = """
You are StudyNova's Image & Document Inspector.
Analyze this uploaded educational image:
1. Is this image clear enough to read? If it is blank, pitch black, severely blurry, or completely unreadable, respond with: UNREADABLE: <reason>.
2. What primary academic subject is shown (Physics, Chemistry, Mathematics, Biology, History, Geography, Computer Science, or Other)?
3. What is the specific chapter or topic title shown?
4. Identify any equations, chemical formulas, or numbered questions.

Return a concise initial classification in this exact format:
SUBJECT: <Subject>
TOPIC: <Topic>
READABLE: YES (or NO: <reason>)
SUMMARY_OF_CONTENTS: <Brief 1-sentence description>
""".trimIndent()

      val detectionResponse = withTimeout(45_000L) {
        generalModel.generateContent(
          content {
            image(bitmap)
            text(detectionPrompt)
          }
        ).text.orEmpty()
      }

      if (detectionResponse.contains("READABLE: NO", ignoreCase = true) || detectionResponse.startsWith("UNREADABLE", ignoreCase = true)) {
        val reason = detectionResponse.substringAfter("NO:").substringAfter("UNREADABLE:").trim()
          .ifBlank { "The uploaded image is too blurry, dark, or low-resolution to reliably extract academic content." }
        return Result.failure(
          IllegalArgumentException("UNREADABLE_IMAGE: $reason Please upload a well-lit, steady, higher-resolution photo of your study material.")
        )
      }

      // Extract detected subject and topic from inspection
      val detectedSubjectStr = detectionResponse.lines().find { it.startsWith("SUBJECT:", ignoreCase = true) }
        ?.substringAfter(":")?.trim() ?: "General Science"
      val detectedTopicStr = detectionResponse.lines().find { it.startsWith("TOPIC:", ignoreCase = true) }
        ?.substringAfter(":")?.trim() ?: "Document Content"

      val detectedSubject = AcademicSubject.fromString(detectedSubjectStr)
      val contextLock = TopicContextLock.create(
        rawTopicOrMaterial = detectedTopicStr,
        userSelectedSubjectStr = detectedSubject.displayName,
        exam = exam,
        language = language,
        task = "Document Analysis ($action)",
        sourceMode = "Uploaded Image Analysis"
      )

      // Step B: Formulate action prompt with domain rules
      val actionPrompt = when (action) {
        "Explain this page" -> "Explain all key concepts, equations, and diagrams visible on this page clearly and thoroughly. Maintain exact subject terminology."
        "Summarize this page" -> "Provide an executive summary and high-yield bulleted summary of this page, preserving all key definitions and laws."
        "Solve the questions" -> "Identify all questions, exercises, or problems visible on this page. For each, provide Given Information, Formula/Principle, Step-by-Step Working, Final Answer with Units, and Conceptual Explanation."
        "Generate MCQs" -> "Extract the core concepts from this page and generate 5 exam-style MCQs with 4 distinct options each, correct answer, and verified step-by-step explanations."
        "Extract formulas" -> "Extract every mathematical, chemical, and physical formula from this page with variable definitions, standard units, and conditions of applicability."
        "Make revision notes" -> "Convert this page into high-yield revision notes with bolded key terms, essential formulas, and core takeaways."
        else -> "Analyze and explain the educational contents of this page with subject fidelity."
      }

      val fullPrompt = """
${SubjectExpertPrompts.CORE_SYSTEM_INSTRUCTION}

[DOCUMENT INSPECTION REPORT]
- Detected Subject: ${contextLock.subject.displayName}
- Identified Topic: ${contextLock.topic}
- Target Exam: $exam
- Language: $language

${SubjectExpertPrompts.getExpertDomainRules(contextLock.subject)}

=== USER ACTION: $actionPrompt ===
Preserve all mathematical formulas, chemical structures, reaction conditions, and scientific symbols exactly as written.
If any portion is handwritten and ambiguous, state the uncertainty explicitly rather than guessing.
Provide a clean, beautifully formatted Markdown response with clear section headings.
""".trimIndent()

      val responseText = withTimeout(60_000L) {
        generalModel.generateContent(
          content {
            image(bitmap)
            text(fullPrompt)
          }
        ).text
      } ?: throw IllegalStateException("Model returned empty response.")

      Result.success(responseText)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "SnapStudy document analysis failed", e)
      Result.failure(e)
    }
  }

  /**
   * 3. AI Tutor streaming chat response with Stale Context Flush:
   * When user changes subjects or topics, stale conversational context from
   * previous subjects is automatically flushed!
   */
  fun streamTutorResponse(
    userPrompt: String,
    conversationHistory: List<Pair<String, String>> = emptyList(),
    examContext: String = "General",
    language: String = "English"
  ): Flow<String> = flow {
    // Step A: Classify new user prompt
    val classification = SubjectClassifier.classifyTopic(userPrompt)
    val activeSubject = classification.subject

    // Step B: Stale context flush: Filter history to only include messages from the same academic domain
    val relevantHistory = if (activeSubject != AcademicSubject.OTHER) {
      conversationHistory.takeLast(6).filter { (_, msg) ->
        val msgSubj = SubjectClassifier.classifyTopic(msg).subject
        msgSubj == activeSubject || msgSubj == AcademicSubject.OTHER
      }
    } else {
      conversationHistory.takeLast(4)
    }

    val contextLock = TopicContextLock.create(
      rawTopicOrMaterial = userPrompt,
      userSelectedSubjectStr = activeSubject.displayName,
      exam = examContext,
      language = language,
      task = "Conversational Tutoring",
      sourceMode = "Interactive Chat"
    )

    val systemContext = """
${SubjectExpertPrompts.CORE_SYSTEM_INSTRUCTION}

${contextLock.toPromptContext()}

${SubjectExpertPrompts.getExpertDomainRules(contextLock.subject)}

Pedagogical Directives:
- Explain concepts clearly from fundamentals to advanced nuance.
- Break difficult math and derivations into numbered steps.
- Use the student's preferred language ($language).
- Never allow previous chat history to drag you into a different subject.
""".trimIndent()

    val promptWithHistory = buildString {
      append(systemContext)
      append("\n\n=== RELEVANT CONVERSATION CONTEXT ===\n")
      relevantHistory.forEach { (role, msg) ->
        append("$role: $msg\n")
      }
      append("\nUser: $userPrompt\nStudyNova Tutor:")
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

  /**
   * 4. Generate Timed Mock Test Questions with Subject Locking, Configurable Difficulty,
   * and Question Verification.
   */
  suspend fun generateMockTestQuestions(
    exam: String,
    subject: String,
    questionCount: Int = 10,
    difficulty: String = "Medium",
    language: String = "English",
    topicFocus: String? = null
  ): Result<List<Question>> {
    return try {
      val parsedSubject = AcademicSubject.fromString(subject)
      val targetTopic = topicFocus ?: "${parsedSubject.displayName} Core Curriculum"

      val contextLock = TopicContextLock.create(
        rawTopicOrMaterial = targetTopic,
        userSelectedSubjectStr = parsedSubject.displayName,
        exam = exam,
        difficulty = difficulty,
        language = language,
        task = "Timed Mock Examination Generation",
        sourceMode = "Curriculum Generator"
      )

      val distribution = QuestionDistribution.balanced(questionCount)
      val questionPrompt = QuestionGenerationEngine.buildQuestionGenerationPrompt(contextLock, distribution)
      val fullPrompt = SubjectExpertPrompts.buildPrompt(contextLock, questionPrompt)

      val responseText = withTimeout(60_000L) {
        generalModel.generateContent(fullPrompt).text
      } ?: throw IllegalStateException("Model returned empty text.")

      val parsedQuestions = QuestionGenerationEngine.parseAndValidateQuestions(responseText, contextLock)

      if (parsedQuestions.isEmpty()) {
        throw IllegalStateException("Failed to parse valid examination questions from model response.")
      }

      Result.success(parsedQuestions)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Mock test generation failed", e)
      Result.failure(e)
    }
  }

  /**
   * 5. AI Answer Evaluator
   */
  suspend fun evaluateAnswer(
    question: String,
    studentAnswer: String,
    exam: String = "UPSC",
    subject: String = "General Studies"
  ): Result<EvaluationResult> {
    return try {
      val parsedSubject = AcademicSubject.fromString(subject)
      val contextLock = TopicContextLock.create(
        rawTopicOrMaterial = question,
        userSelectedSubjectStr = parsedSubject.displayName,
        exam = exam,
        task = "Answer Evaluation",
        sourceMode = "Student Response Assessment"
      )

      val prompt = """
${SubjectExpertPrompts.CORE_SYSTEM_INSTRUCTION}

${contextLock.toPromptContext()}

${SubjectExpertPrompts.getExpertDomainRules(contextLock.subject)}

You are an expert exam evaluator for $exam (${contextLock.subject.displayName}).
Evaluate this student's written response against official rubric:
Question: $question
Student's Answer: $studentAnswer

Return a raw valid JSON object ONLY (no markdown code fence):
{
  "score": 7,
  "maxScore": 10,
  "conceptualAccuracy": "Specific assessment of conceptual and factual correctness",
  "keyPointsCovered": ["Key point 1 correctly addressed", "Key point 2"],
  "missingPoints": ["Omission or misconception 1 that should be corrected"],
  "suggestions": "Actionable feedback on terminology, calculations, or presentation",
  "sampleAnswer": "Ideal benchmark answer demonstrating complete mastery"
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
          conceptualAccuracy = json.optString("conceptualAccuracy", "Assessment completed."),
          keyPointsCovered = kp,
          missingPoints = mp,
          suggestions = json.optString("suggestions", "Focus on core terms and SI units."),
          sampleAnswer = json.optString("sampleAnswer", "Model benchmark answer.")
        )
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Answer evaluation failed", e)
      Result.failure(e)
    }
  }

  /**
   * 6. Generate Personalized Study Timetable
   */
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

  private fun parseWorkspaceJson(raw: String, contextLock: TopicContextLock): StudyWorkspaceResult {
    val cleanJson = cleanJsonString(raw)
    val json = JSONObject(cleanJson)

    val shortSummary = json.optString("shortSummary", "Summary generated.")
    val detailedSummary = json.optString("detailedSummary", "Detailed summary generated.")
    val simpleExplanation = json.optString("simpleExplanation", "Concept explanation.")
    val examNotes = json.optString("examOrientedNotes", "Exam-oriented review.")

    val defsArray = json.optJSONArray("importantDefinitions") ?: JSONArray()
    val definitions = (0 until defsArray.length()).map { defsArray.getString(it) }

    val formulaArray = json.optJSONArray("formulaSheet") ?: JSONArray()
    val formulas = (0 until formulaArray.length()).map { formulaArray.getString(it) }

    val kpArray = json.optJSONArray("keyPoints") ?: JSONArray()
    val keyPoints = (0 until kpArray.length()).map { kpArray.getString(it) }

    val exArray = json.optJSONArray("examples") ?: JSONArray()
    val examples = (0 until exArray.length()).map { exArray.getString(it) }

    val cmArray = json.optJSONArray("commonMistakes") ?: JSONArray()
    val commonMistakes = (0 until cmArray.length()).map { cmArray.getString(it) }

    val qrArray = json.optJSONArray("quickRevisionSheet") ?: JSONArray()
    val quickRevisionSheet = (0 until qrArray.length()).map { qrArray.getString(it) }

    val mcqsList = mutableListOf<Question>()
    val mcqsArray = json.optJSONArray("mcqs") ?: JSONArray()
    for (i in 0 until mcqsArray.length()) {
      val qObj = mcqsArray.getJSONObject(i)
      val optsArr = qObj.optJSONArray("options") ?: JSONArray()
      val opts = (0 until optsArr.length()).map { optsArr.getString(it) }
      mcqsList.add(
        Question(
          id = "mcq_${contextLock.requestId.take(6)}_$i",
          exam = contextLock.exam,
          subject = contextLock.subject.displayName,
          topic = qObj.optString("topic", contextLock.topic),
          questionText = qObj.optString("questionText", "Question $i"),
          options = if (opts.size >= 4) opts.take(4) else listOf("Option A", "Option B", "Option C", "Option D"),
          correctOptionIndex = qObj.optInt("correctOptionIndex", 0).coerceIn(0, 3),
          explanation = qObj.optString("explanation", "Verified based on ${contextLock.topic}."),
          difficulty = contextLock.difficulty,
          source = "StudyNova Practice Question (${contextLock.subject.displayName})"
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

    return StudyWorkspaceResult(
      shortSummary = shortSummary,
      detailedSummary = detailedSummary,
      simpleExplanation = simpleExplanation,
      importantDefinitions = definitions,
      formulaSheet = formulas,
      mcqs = mcqsList,
      questionsAndAnswers = qaList,
      flashcards = fcList,
      mindMapOutline = mindMap,
      keyPoints = keyPoints,
      examples = examples,
      examOrientedNotes = examNotes,
      commonMistakes = commonMistakes,
      quickRevisionSheet = quickRevisionSheet
    )
  }

  private fun cleanJsonString(raw: String): String {
    var s = raw.trim()
    if (s.startsWith("```json")) s = s.substring(7)
    if (s.startsWith("```")) s = s.substring(3)
    if (s.endsWith("```")) s = s.substring(0, s.length - 3)
    return s.trim()
  }
}
