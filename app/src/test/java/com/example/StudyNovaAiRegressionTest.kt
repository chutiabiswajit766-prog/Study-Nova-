package com.example

import com.example.ai.context.TopicContextLock
import com.example.ai.prompts.SubjectExpertPrompts
import com.example.ai.questions.QuestionDistribution
import com.example.ai.questions.QuestionGenerationEngine
import com.example.ai.research.ResearchEngine
import com.example.ai.subject.AcademicSubject
import com.example.ai.subject.SubjectClassifier
import com.example.ai.validation.ResponseValidationEngine
import com.example.data.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyNovaAiRegressionTest {

  @Test
  fun testA_Chemistry_ChemicalBonding() {
    val classification = SubjectClassifier.classifyTopic("Chemical Bonding")
    assertEquals(AcademicSubject.CHEMISTRY, classification.subject)
    assertTrue("Topic must be Chemical Bonding", classification.topic.contains("Chemical Bonding", ignoreCase = true))
    assertTrue("Subtopics must contain ionic or covalent", classification.subtopics.any { it.contains("Bonding", ignoreCase = true) })
    assertFalse(classification.isAmbiguous)

    val contextLock = TopicContextLock.create(
      rawTopicOrMaterial = "Chemical Bonding",
      userSelectedSubjectStr = "Chemistry",
      exam = "JEE Main"
    )
    assertEquals(AcademicSubject.CHEMISTRY, contextLock.subject)
    val rules = SubjectExpertPrompts.getExpertDomainRules(contextLock.subject)
    assertTrue(rules.contains("CHEMISTRY EXPERT SYSTEM"))
    assertTrue(rules.contains("chemical bonding", ignoreCase = true))

    // Validate a model response for Chemical Bonding
    val sampleSummary = "Chemical bonding is the physical process responsible for the attractive interactions between atoms and molecules. Ionic bonding involves complete electron transfer, while covalent bonding involves electron pair sharing."
    val sampleDetailed = "Chemical bonding governs the stability of matter according to Lewis octet rule and VSEPR theory. Hybridization (sp, sp2, sp3) determines molecular geometry. Molecular orbital theory (MOT) accounts for paramagnetic nature of O2 with bond orders."
    val sampleMcqs = (1..5).map { i ->
      Question(
        id = "chem_q_$i",
        exam = "JEE Main",
        subject = "Chemistry",
        topic = "Chemical Bonding",
        questionText = "Which molecule exhibits sp3d hybridization with linear shape? (Question $i)",
        options = listOf("XeF2", "BF3", "CH4", "SF6"),
        correctOptionIndex = 0,
        explanation = "XeF2 has 2 bond pairs and 3 lone pairs in equatorial positions of trigonal bipyramidal arrangement, resulting in linear geometry.",
        difficulty = "Medium",
        source = "StudyNova Practice Question (Chemistry)"
      )
    }

    val validation = ResponseValidationEngine.validateWorkspace(
      contextLock = contextLock,
      shortSummary = sampleSummary,
      detailedSummary = sampleDetailed,
      mcqs = sampleMcqs,
      formulas = listOf("Bond Order = 1/2 * (Nb - Na)", "Formal Charge = V - N_nonbonding - 1/2 * B_bonding"),
      definitions = listOf("VSEPR: Valence Shell Electron Pair Repulsion theory", "Hybridization: Mixing of atomic orbitals to form degenerate hybrid orbitals")
    )

    assertTrue("Validation must pass for Chemical Bonding: ${validation.primaryFailureReason}", validation.isValid)
  }

  @Test
  fun testB_Physics_NewtonsLawsOfMotion() {
    val classification = SubjectClassifier.classifyTopic("Newton's Laws of Motion")
    assertEquals(AcademicSubject.PHYSICS, classification.subject)
    assertTrue(classification.topic.contains("Laws of Motion", ignoreCase = true))
    assertTrue(classification.subtopics.any { it.contains("Newton", ignoreCase = true) || it.contains("Inertia", ignoreCase = true) })

    val contextLock = TopicContextLock.create(
      rawTopicOrMaterial = "Newton's Laws of Motion",
      userSelectedSubjectStr = "Physics",
      exam = "JEE Main"
    )
    assertEquals(AcademicSubject.PHYSICS, contextLock.subject)
    val rules = SubjectExpertPrompts.getExpertDomainRules(contextLock.subject)
    assertTrue(rules.contains("PHYSICS EXPERT SYSTEM"))
    assertTrue(rules.contains("Free Body Diagrams"))
    assertTrue(rules.contains("SI units"))

    // Verify zero chemistry bleed
    assertFalse(rules.contains("organic reaction mechanisms", ignoreCase = true))
  }

  @Test
  fun testC_Mathematics_Differentiation() {
    val classification = SubjectClassifier.classifyTopic("Differentiation")
    assertEquals(AcademicSubject.MATHEMATICS, classification.subject)
    assertTrue(classification.topic.contains("Calculus", ignoreCase = true) || classification.topic.contains("Derivative", ignoreCase = true))

    val contextLock = TopicContextLock.create(
      rawTopicOrMaterial = "Differentiation",
      userSelectedSubjectStr = "Mathematics",
      exam = "Class 12 Boards"
    )
    assertEquals(AcademicSubject.MATHEMATICS, contextLock.subject)
    val rules = SubjectExpertPrompts.getExpertDomainRules(contextLock.subject)
    assertTrue(rules.contains("MATHEMATICS EXPERT SYSTEM"))
    assertTrue(rules.contains("Calculus & Analysis"))
    assertTrue(rules.contains("chain rule", ignoreCase = true))
  }

  @Test
  fun testD_Biology_Photosynthesis() {
    val classification = SubjectClassifier.classifyTopic("Photosynthesis")
    assertEquals(AcademicSubject.BIOLOGY, classification.subject)
    assertTrue(classification.topic.contains("Photosynthesis", ignoreCase = true))

    val contextLock = TopicContextLock.create(
      rawTopicOrMaterial = "Photosynthesis",
      userSelectedSubjectStr = "Biology",
      exam = "NEET UG"
    )
    assertEquals(AcademicSubject.BIOLOGY, contextLock.subject)
    val rules = SubjectExpertPrompts.getExpertDomainRules(contextLock.subject)
    assertTrue(rules.contains("BIOLOGY EXPERT SYSTEM"))
    assertTrue(rules.contains("Photosynthesis"))
    assertTrue(rules.contains("Calvin cycle", ignoreCase = true))
  }

  @Test
  fun testE_SubjectSwitching_ZeroStaleBleed() {
    // Request 1: Chemical Bonding
    val lock1 = TopicContextLock.create("Chemical Bonding", "Chemistry")
    assertEquals(AcademicSubject.CHEMISTRY, lock1.subject)
    val req1Id = lock1.requestId

    // Request 2: Newton's Laws of Motion immediately afterwards
    val lock2 = TopicContextLock.create("Newton's Laws of Motion", "Physics")
    assertEquals(AcademicSubject.PHYSICS, lock2.subject)
    val req2Id = lock2.requestId

    // Ensure completely distinct request contexts
    assertFalse(req1Id == req2Id)
    assertEquals("Laws of Motion & Classical Mechanics", lock2.topic)
    assertEquals(AcademicSubject.PHYSICS, lock2.subject)
  }

  @Test
  fun testF_SubjectTopicMismatch_AutoCorrection() {
    // Student leaves dropdown on "Physics", but enters "Chemical Bonding"
    val mismatch = SubjectClassifier.checkSubjectMismatch("Chemical Bonding", AcademicSubject.PHYSICS)
    assertTrue(mismatch.isMismatch)
    assertEquals(AcademicSubject.CHEMISTRY, mismatch.detectedSubject)
    assertEquals(AcademicSubject.PHYSICS, mismatch.userSelectedSubject)

    // TopicContextLock must auto-lock to the genuine detected subject (Chemistry)
    val lock = TopicContextLock.create("Chemical Bonding", "Physics")
    assertEquals(AcademicSubject.CHEMISTRY, lock.subject)
  }

  @Test
  fun testG_Research_GenuineCurriculumReference() {
    val contextLock = TopicContextLock.create(
      rawTopicOrMaterial = "Chemical Bonding",
      userSelectedSubjectStr = "Chemistry",
      exam = "JEE Main"
    )

    val research = ResearchEngine.executeResearchWorkflow(
      contextLock = contextLock,
      userQuery = "Chemical Bonding",
      modelGeneratedExplanation = "Ionic and covalent bonding principles."
    )

    assertEquals("Subject: Chemistry | Canonical Topic: Chemical Bonding & Molecular Structure | Subtopic: Ionic Bonding", research.step1SubjectAndTopic)
    assertTrue(research.step2SyllabusRequirements.contains("NTA JEE Main Official Syllabus"))
    assertTrue(research.step8References.isNotEmpty())
    assertFalse(research.step8References.first().isLiveGroundingConnected)
    assertFalse(research.researchTransparencyNotice.contains("http://"))
    assertTrue(research.researchTransparencyNotice.contains("No unverified third-party websites or fabricated citations were generated"))
  }

  @Test
  fun testH_QuestionQuality_TwentyMCQs() {
    val contextLock = TopicContextLock.create(
      rawTopicOrMaterial = "Chemical Bonding",
      userSelectedSubjectStr = "Chemistry",
      exam = "JEE Main"
    )
    val distribution = QuestionDistribution(5, 10, 5)
    assertEquals(20, distribution.total)

    // Build raw JSON representing 20 valid questions
    val sb = StringBuilder("[")
    for (i in 1..20) {
      if (i > 1) sb.append(",")
      sb.append("""
        {
          "id": "q$i",
          "questionType": "MCQ",
          "questionText": "Chemical bonding question $i?",
          "options": ["Option A", "Option B", "Option C", "Option D"],
          "correctOptionIndex": ${(i - 1) % 4},
          "difficulty": "${if (i <= 5) "Easy" else if (i <= 15) "Medium" else "Hard"}",
          "topic": "Chemical Bonding",
          "subtopic": "Ionic and Covalent Bonding",
          "explanation": "Option ${('A' + ((i - 1) % 4))} is correct based on octet theory."
        }
      """.trimIndent())
    }
    sb.append("]")

    val questions = QuestionGenerationEngine.parseAndValidateQuestions(sb.toString(), contextLock)
    assertEquals(20, questions.size)
    for (q in questions) {
      assertEquals(4, q.options.size)
      assertTrue(q.correctOptionIndex in 0..3)
      assertEquals("Chemistry", q.subject)
      assertTrue(q.source.contains("StudyNova Practice Question"))
    }
  }

  @Test
  fun testI_AmbiguousInput_ResonanceClarification() {
    val classification = SubjectClassifier.classifyTopic("Resonance")
    assertTrue("Resonance must be flagged as ambiguous", classification.isAmbiguous)
    assertTrue("Should provide multiple subject options", classification.ambiguityOptions.size >= 2)
    assertNotNull(classification.clarificationPrompt)
    assertTrue(classification.clarificationPrompt!!.contains("distinct subjects"))

    // If student specifies unambiguous "Resonance in Chemistry", it resolves directly
    val resolved = SubjectClassifier.classifyTopic("Resonance in Chemistry")
    assertFalse(resolved.isAmbiguous)
    assertEquals(AcademicSubject.CHEMISTRY, resolved.subject)
  }

  @Test
  fun testJ_Validation_DetectsCrossBleedAndInvalidMcqs() {
    val contextLock = TopicContextLock.create("Chemical Bonding", "Chemistry")

    // Flawed response where MCQs have only 2 options and contains physics projectile motion keywords
    val flawedSummary = "Projectile motion kinematics trajectory."
    val flawedMcqs = listOf(
      Question(
        id = "bad_q1",
        exam = "JEE",
        subject = "Chemistry",
        topic = "Chemical Bonding",
        questionText = "Bad question?",
        options = listOf("Opt 1", "Opt 2"), // only 2 options!
        correctOptionIndex = 0,
        explanation = "Option A",
        difficulty = "Easy"
      )
    )

    val validation = ResponseValidationEngine.validateWorkspace(
      contextLock = contextLock,
      shortSummary = flawedSummary,
      detailedSummary = "Trajectory velocity acceleration projectile kinematics.",
      mcqs = flawedMcqs,
      formulas = emptyList(),
      definitions = emptyList()
    )

    assertFalse("Flawed cross-bleed response must fail validation", validation.isValid)
    assertTrue("Should recommend retry", validation.shouldRetry)
  }
}
