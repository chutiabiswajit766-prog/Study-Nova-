package com.example.data.model

data class ExamInfo(
  val id: String,
  val name: String,
  val subtitle: String,
  val subjects: List<String>,
  val durationMinutes: Int,
  val markingScheme: String,
  val correctMark: Double,
  val negativeMark: Double,
  val syllabusOverview: String
)

data class Question(
  val id: String = "",
  val exam: String = "",
  val subject: String = "",
  val topic: String = "",
  val questionText: String = "",
  val options: List<String> = emptyList(),
  val correctOptionIndex: Int = 0,
  val explanation: String = "",
  val difficulty: String = "Medium",
  val source: String = "AI Generated Practice" // or "Official PYQ 2024"
)

data class FormulaItem(
  val id: String,
  val title: String,
  val formula: String,
  val subject: String,
  val topic: String,
  val variables: String,
  val condition: String,
  val unit: String
)

data class DailyChallenge(
  val id: String,
  val date: String,
  val title: String,
  val subject: String,
  val xpReward: Int,
  val questions: List<Question>
)

data class Achievement(
  val id: String,
  val title: String,
  val description: String,
  val iconName: String,
  val xpBonus: Int,
  val isUnlocked: Boolean = false
)

object ExamRepository {
  val supportedExams = listOf(
    ExamInfo(
      id = "jee",
      name = "JEE Main & Advanced",
      subtitle = "Engineering Entrance Exam",
      subjects = listOf("Physics", "Chemistry", "Mathematics"),
      durationMinutes = 180,
      markingScheme = "+4 for correct, -1 for incorrect",
      correctMark = 4.0,
      negativeMark = 1.0,
      syllabusOverview = "Kinematics, Thermodynamics, Optics, Organic & Physical Chem, Calculus, Vectors & 3D"
    ),
    ExamInfo(
      id = "neet",
      name = "NEET UG",
      subtitle = "Medical Entrance Exam",
      subjects = listOf("Physics", "Chemistry", "Biology (Botany & Zoology)"),
      durationMinutes = 200,
      markingScheme = "+4 for correct, -1 for incorrect",
      correctMark = 4.0,
      negativeMark = 1.0,
      syllabusOverview = "Human Physiology, Genetics, Ecology, Mechanics, Electrostatics, Equilibrium, Coordination"
    ),
    ExamInfo(
      id = "upsc",
      name = "UPSC Civil Services",
      subtitle = "IAS, IPS & Central Services",
      subjects = listOf("General Studies I-IV", "Current Affairs", "CSAT Aptitude", "Polity & History"),
      durationMinutes = 120,
      markingScheme = "+2 for correct, -0.66 for incorrect",
      correctMark = 2.0,
      negativeMark = 0.66,
      syllabusOverview = "Indian Constitution, Modern History, Geography, Economy, Ethics, International Relations"
    ),
    ExamInfo(
      id = "class12",
      name = "Class 12 Boards",
      subtitle = "CBSE & State Board Senior Secondary",
      subjects = listOf("Physics", "Chemistry", "Mathematics", "Biology", "English"),
      durationMinutes = 180,
      markingScheme = "+1 per question, no negative marking",
      correctMark = 1.0,
      negativeMark = 0.0,
      syllabusOverview = "CBSE Core Curriculum with Board pattern questions and numericals"
    ),
    ExamInfo(
      id = "class10",
      name = "Class 10 Boards",
      subtitle = "CBSE & State Board Secondary",
      subjects = listOf("Science", "Mathematics", "Social Science", "English"),
      durationMinutes = 180,
      markingScheme = "+1 per question, no negative marking",
      correctMark = 1.0,
      negativeMark = 0.0,
      syllabusOverview = "Standard 10 syllabus covering foundational STEM and humanities"
    ),
    ExamInfo(
      id = "ssc",
      name = "SSC & Banking",
      subtitle = "CGL, CHSL, IBPS PO & Clerk",
      subjects = listOf("Quantitative Aptitude", "Reasoning", "English Comprehension", "General Awareness"),
      durationMinutes = 60,
      markingScheme = "+2 for correct, -0.5 for incorrect",
      correctMark = 2.0,
      negativeMark = 0.5,
      syllabusOverview = "Speed math, Logical reasoning, Banking awareness, Grammar, Vocabulary"
    )
  )

  val samplePyqs = listOf(
    Question(
      id = "pyq_jee_2024_1",
      exam = "JEE Main & Advanced",
      subject = "Physics",
      topic = "Work, Energy & Power",
      questionText = "A block of mass m is released from rest from a height h on a smooth incline. The velocity at the bottom of the incline depends on:",
      options = listOf("Height h and acceleration due to gravity g", "Angle of inclination theta", "Mass of the block m", "Length of the incline"),
      correctOptionIndex = 0,
      explanation = "By conservation of mechanical energy: mgh = (1/2)mv^2 => v = sqrt(2gh). It depends only on h and g, independent of mass or inclination angle.",
      difficulty = "Medium",
      source = "Verified JEE Main 2024 Session 1"
    ),
    Question(
      id = "pyq_neet_2024_1",
      exam = "NEET UG",
      subject = "Biology (Botany & Zoology)",
      topic = "Genetics & Evolution",
      questionText = "In Mendel's dihybrid cross, what is the phenotypic ratio obtained in the F2 generation?",
      options = listOf("9:3:3:1", "1:2:1", "3:1", "9:7"),
      correctOptionIndex = 0,
      explanation = "Mendel's law of independent assortment in a dihybrid cross gives a 9:3:3:1 phenotypic ratio (Round-Yellow : Round-Green : Wrinkled-Yellow : Wrinkled-Green).",
      difficulty = "Easy",
      source = "Verified NEET UG 2024 Official Paper"
    ),
    Question(
      id = "pyq_upsc_2024_1",
      exam = "UPSC Civil Services",
      subject = "Polity & History",
      topic = "Indian Constitution",
      questionText = "Which Article of the Constitution of India safeguards one's right to marry the person of one's choice?",
      options = listOf("Article 19", "Article 21", "Article 25", "Article 29"),
      correctOptionIndex = 1,
      explanation = "In the landmark Hadiya Case (Shafin Jahan v. Asokan K.M., 2018), the Supreme Court ruled that the right to marry a person of one's choice is integral to Article 21 (Right to Life and Personal Liberty).",
      difficulty = "Medium",
      source = "Verified UPSC CSE Prelims 2019/2024 Reference"
    )
  )

  val coreFormulas = listOf(
    FormulaItem(
      id = "f_1",
      title = "Kinetic Energy",
      formula = "K = (1/2) * m * v^2",
      subject = "Physics",
      topic = "Mechanics",
      variables = "m = mass (kg), v = velocity (m/s)",
      condition = "Classical mechanics (v << c)",
      unit = "Joules (J)"
    ),
    FormulaItem(
      id = "f_2",
      title = "Ideal Gas Law",
      formula = "P * V = n * R * T",
      subject = "Chemistry",
      topic = "Thermodynamics",
      variables = "P = pressure, V = volume, n = moles, R = gas constant, T = absolute temp",
      condition = "Low pressure and high temperature (ideal behavior)",
      unit = "P in Pa, V in m^3, T in Kelvin"
    ),
    FormulaItem(
      id = "f_3",
      title = "Euler's Identity / De Moivre",
      formula = "e^(i*theta) = cos(theta) + i*sin(theta)",
      subject = "Mathematics",
      topic = "Complex Numbers",
      variables = "theta in radians, i = sqrt(-1)",
      condition = "Valid for all real and complex numbers theta",
      unit = "Dimensionless"
    ),
    FormulaItem(
      id = "f_4",
      title = "Ohm's Law",
      formula = "V = I * R",
      subject = "Physics",
      topic = "Current Electricity",
      variables = "V = potential difference, I = current, R = resistance",
      condition = "Constant physical conditions (temperature, strain)",
      unit = "Volts (V), Amperes (A), Ohms (Ω)"
    )
  )
}
