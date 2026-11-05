package com.example.ai.subject

import java.util.Locale

enum class AcademicSubject(val displayName: String) {
  PHYSICS("Physics"),
  CHEMISTRY("Chemistry"),
  MATHEMATICS("Mathematics"),
  BIOLOGY("Biology"),
  ENGLISH("English"),
  HISTORY("History"),
  GEOGRAPHY("Geography"),
  POLITICAL_SCIENCE("Political Science"),
  ECONOMICS("Economics"),
  COMPUTER_SCIENCE("Computer Science"),
  GENERAL_KNOWLEDGE("General Knowledge"),
  OTHER("Other");

  companion object {
    fun fromString(name: String?): AcademicSubject {
      if (name.isNullOrBlank()) return OTHER
      val clean = name.trim().lowercase(Locale.ROOT)
      return entries.find { 
        it.displayName.lowercase(Locale.ROOT) == clean ||
        it.name.lowercase(Locale.ROOT) == clean
      } ?: when {
        clean.contains("phys") -> PHYSICS
        clean.contains("chem") -> CHEMISTRY
        clean.contains("math") -> MATHEMATICS
        clean.contains("bio") -> BIOLOGY
        clean.contains("eng") -> ENGLISH
        clean.contains("hist") -> HISTORY
        clean.contains("geog") -> GEOGRAPHY
        clean.contains("pol") || clean.contains("civic") -> POLITICAL_SCIENCE
        clean.contains("econ") -> ECONOMICS
        clean.contains("comp") || clean.contains("code") || clean.contains("program") -> COMPUTER_SCIENCE
        clean.contains("gk") || clean.contains("general") -> GENERAL_KNOWLEDGE
        else -> OTHER
      }
    }
  }
}

data class ClassificationResult(
  val subject: AcademicSubject,
  val topic: String,
  val subtopics: List<String>,
  val relevantExams: List<String>,
  val educationLevel: String,
  val isAmbiguous: Boolean = false,
  val ambiguityOptions: List<String> = emptyList(),
  val clarificationPrompt: String? = null,
  val confidence: Float = 1.0f
)

data class SubjectMismatch(
  val isMismatch: Boolean,
  val userSelectedSubject: AcademicSubject,
  val detectedSubject: AcademicSubject,
  val explanation: String
)

object SubjectClassifier {

  // Ambiguous topics across disciplines
  private val ambiguousTopics = mapOf(
    "resonance" to listOf(
      "Resonance in Chemistry (Delocalized electrons, mesomeric structures in benzene/carbonates)",
      "Resonance in Physics (Acoustics, tuning forks, RLC alternating current circuits)",
      "Orbital Resonance in Astronomy (Planetary/lunar gravitational resonance)"
    ),
    "induction" to listOf(
      "Electromagnetic Induction in Physics (Faraday's law, Lenz's law, induced EMF)",
      "Mathematical Induction in Mathematics (Principle of mathematical induction, base & inductive steps)",
      "Embryonic/Enzyme Induction in Biology (Cell differentiation or gene regulation)",
      "Inductive Reasoning in Philosophy/English (Logical derivation from specific to general)"
    ),
    "equilibrium" to listOf(
      "Chemical Equilibrium in Chemistry (Le Chatelier's principle, Kc, Kp, ionic equilibrium)",
      "Mechanical Equilibrium in Physics (Translational and rotational equilibrium, net force and torque = 0)",
      "Economic Equilibrium in Economics (Supply and demand market clearance, Nash equilibrium)",
      "Genetic Equilibrium in Biology (Hardy-Weinberg equilibrium in populations)"
    ),
    "evolution" to listOf(
      "Biological Evolution in Biology (Darwinian natural selection, speciation, genetics)",
      "Stellar Evolution in Physics/Astronomy (Life cycle of stars, white dwarfs, supernovae)",
      "Social or Cultural Evolution in History/Sociology"
    ),
    "matrix" to listOf(
      "Matrices in Mathematics (Linear algebra, determinants, matrix multiplication, inverses)",
      "Mitochondrial/Extracellular Matrix in Biology (Cellular anatomy and connective tissue)",
      "Material Matrix in Metallurgy/Material Science"
    ),
    "wave" to listOf(
      "Mechanical and EM Waves in Physics (Wavelength, frequency, interference, diffraction)",
      "P and S Seismic Waves in Geography (Earthquake propagation through earth mantle)"
    ),
    "flux" to listOf(
      "Magnetic/Electric Flux in Physics (Gauss's law, field line integration, Weber/Tesla)",
      "Chemical Flux / Metallurgical Flux in Chemistry (Substance added to promote melting/slag formation)"
    ),
    "entropy" to listOf(
      "Thermodynamic Entropy in Physics/Chemistry (Second law of thermodynamics, delta S, reversibility)",
      "Information Entropy in Computer Science (Shannon entropy, data compression limits)"
    )
  )

  // Subject topic taxonomies (Keyword + Subtopics + Exams + Education Level)
  private data class TopicProfile(
    val canonicalTopic: String,
    val subject: AcademicSubject,
    val subtopics: List<String>,
    val exams: List<String>,
    val educationLevel: String,
    val keywords: List<String>
  )

  private val taxonomy: List<TopicProfile> = listOf(
    // CHEMISTRY
    TopicProfile(
      canonicalTopic = "Chemical Bonding & Molecular Structure",
      subject = AcademicSubject.CHEMISTRY,
      subtopics = listOf("Ionic Bonding", "Covalent Bonding", "Coordinate Bonding", "VSEPR Theory", "Hybridization", "Molecular Orbital Theory", "Hydrogen Bonding"),
      exams = listOf("JEE Main", "JEE Advanced", "NEET UG", "Class 11 Boards"),
      educationLevel = "Class 11 / Higher Secondary",
      keywords = listOf("chemical bonding", "covalent bond", "ionic bond", "vsepr", "hybridization", "molecular orbital", "mot", "hydrogen bond", "dipole moment", "lewis structure", "resonance structure", "octet rule", "fajan")
    ),
    TopicProfile(
      canonicalTopic = "Periodic Table & Periodicity",
      subject = AcademicSubject.CHEMISTRY,
      subtopics = listOf("Modern Periodic Law", "Periodic Trends", "Ionization Enthalpy", "Electron Gain Enthalpy", "Electronegativity", "Atomic & Ionic Radii"),
      exams = listOf("JEE Main", "NEET UG", "Class 10 Boards", "Class 11 Boards"),
      educationLevel = "Class 10-11",
      keywords = listOf("periodic table", "periodicity", "ionization enthalpy", "electron gain", "electronegativity", "atomic radii", "s-block", "p-block", "d-block", "f-block", "mendeleev")
    ),
    TopicProfile(
      canonicalTopic = "Organic Chemistry: Hydrocarbons & Reaction Mechanisms",
      subject = AcademicSubject.CHEMISTRY,
      subtopics = listOf("Alkanes, Alkenes, Alkynes", "Electrophilic Addition", "Nucleophilic Substitution (SN1, SN2)", "Elimination (E1, E2)", "Aromaticity & Benzene Reactions", "IUPAC Nomenclature"),
      exams = listOf("JEE Main", "JEE Advanced", "NEET UG", "Class 11-12 Boards"),
      educationLevel = "Class 11-12",
      keywords = listOf("organic chemistry", "hydrocarbon", "alkane", "alkene", "alkyne", "benzene", "sn1", "sn2", "electrophile", "nucleophile", "iupac", "carbocation", "isomerism", "markovnikov", "grignard")
    ),
    TopicProfile(
      canonicalTopic = "Chemical Thermodynamics & Energetics",
      subject = AcademicSubject.CHEMISTRY,
      subtopics = listOf("First Law of Thermodynamics", "Enthalpy & Hess's Law", "Entropy & Spontaneity", "Gibbs Free Energy", "Heat Capacity & Calorimetry"),
      exams = listOf("JEE Main", "JEE Advanced", "NEET UG", "Class 11 Boards"),
      educationLevel = "Class 11",
      keywords = listOf("chemical thermodynamics", "enthalpy", "hess law", "gibbs free energy", "spontaneity", "exothermic", "endothermic", "calorimetry", "bond dissociation")
    ),
    TopicProfile(
      canonicalTopic = "Chemical & Ionic Equilibrium",
      subject = AcademicSubject.CHEMISTRY,
      subtopics = listOf("Law of Mass Action", "Le Chatelier's Principle", "Kc and Kp", "pH and pOH", "Buffer Solutions", "Solubility Product (Ksp)", "Acid-Base Theories"),
      exams = listOf("JEE Main", "NEET UG", "Class 11 Boards"),
      educationLevel = "Class 11",
      keywords = listOf("chemical equilibrium", "ionic equilibrium", "le chatelier", "solubility product", "ksp", "buffer solution", "arrhenius", "bronsted", "ph calculation", "salt hydrolysis")
    ),
    TopicProfile(
      canonicalTopic = "Electrochemistry & Redox Reactions",
      subject = AcademicSubject.CHEMISTRY,
      subtopics = listOf("Galvanic Cells", "Nernst Equation", "Faraday's Laws of Electrolysis", "Kohlrausch's Law", "Electrochemical Series", "Corrosion & Batteries"),
      exams = listOf("JEE Main", "JEE Advanced", "NEET UG", "Class 12 Boards"),
      educationLevel = "Class 12",
      keywords = listOf("electrochemistry", "galvanic cell", "nernst equation", "faraday law", "electrolysis", "kohlrausch", "redox", "oxidation state", "standard electrode potential", "cell emf")
    ),
    TopicProfile(
      canonicalTopic = "Coordination Compounds",
      subject = AcademicSubject.CHEMISTRY,
      subtopics = listOf("Werner's Theory", "IUPAC Nomenclature of Complexes", "Valence Bond Theory", "Crystal Field Theory (CFT)", "Isomerism in Coordination Compounds", "Ligands"),
      exams = listOf("JEE Main", "JEE Advanced", "NEET UG", "Class 12 Boards"),
      educationLevel = "Class 12",
      keywords = listOf("coordination compound", "crystal field theory", "cft", "ligand", "chelate", "werner theory", "spectrochemical series", "coordination number", "isomerism in complexes")
    ),

    // PHYSICS
    TopicProfile(
      canonicalTopic = "Laws of Motion & Classical Mechanics",
      subject = AcademicSubject.PHYSICS,
      subtopics = listOf("Inertia & Force", "Newton's First Law", "Newton's Second Law (F=ma, Momentum)", "Newton's Third Law (Action-Reaction)", "Friction", "Circular Motion & Centripetal Force", "Free Body Diagrams"),
      exams = listOf("JEE Main", "JEE Advanced", "NEET UG", "Class 9 Boards", "Class 11 Boards"),
      educationLevel = "Class 9-11",
      keywords = listOf("newton's laws", "newton law", "laws of motion", "f=ma", "inertia", "momentum", "impulse", "friction", "centripetal", "free body diagram", "fbd", "action reaction", "banking of road")
    ),
    TopicProfile(
      canonicalTopic = "Work, Energy and Power",
      subject = AcademicSubject.PHYSICS,
      subtopics = listOf("Work Done by Constant & Variable Force", "Kinetic Energy & Work-Energy Theorem", "Potential Energy (Gravitational & Elastic)", "Conservation of Mechanical Energy", "Collisions (Elastic & Inelastic)", "Power"),
      exams = listOf("JEE Main", "JEE Advanced", "NEET UG", "Class 11 Boards"),
      educationLevel = "Class 11",
      keywords = listOf("work energy", "kinetic energy", "potential energy", "work energy theorem", "elastic collision", "inelastic collision", "mechanical energy", "power in physics", "spring potential energy")
    ),
    TopicProfile(
      canonicalTopic = "Kinematics: Motion in 1D & 2D",
      subject = AcademicSubject.PHYSICS,
      subtopics = listOf("Position, Displacement & Velocity", "Uniformly Accelerated Motion Equations", "Projectile Motion", "Relative Velocity", "Graphical Analysis of Motion"),
      exams = listOf("JEE Main", "NEET UG", "Class 11 Boards"),
      educationLevel = "Class 11",
      keywords = listOf("kinematics", "projectile motion", "trajectory", "velocity", "acceleration", "motion in a straight line", "motion in a plane", "relative velocity", "equations of motion")
    ),
    TopicProfile(
      canonicalTopic = "Gravitation",
      subject = AcademicSubject.PHYSICS,
      subtopics = listOf("Newton's Universal Law of Gravitation", "Acceleration due to Gravity (g)", "Gravitational Potential Energy", "Escape Velocity", "Kepler's Laws of Planetary Motion", "Orbital Velocity & Satellites"),
      exams = listOf("JEE Main", "NEET UG", "Class 9 Boards", "Class 11 Boards"),
      educationLevel = "Class 9-11",
      keywords = listOf("gravitation", "gravity", "escape velocity", "kepler's law", "kepler law", "gravitational field", "orbital velocity", "geostationary satellite", "weightlessness")
    ),
    TopicProfile(
      canonicalTopic = "Electrostatics & Capacitance",
      subject = AcademicSubject.PHYSICS,
      subtopics = listOf("Coulomb's Law", "Electric Field & Dipole", "Gauss's Theorem & Applications", "Electric Potential & Equipotential Surfaces", "Capacitors in Series & Parallel", "Dielectrics"),
      exams = listOf("JEE Main", "JEE Advanced", "NEET UG", "Class 12 Boards"),
      educationLevel = "Class 12",
      keywords = listOf("electrostatics", "coulomb's law", "coulomb law", "electric field", "gauss law", "gauss theorem", "electric potential", "capacitor", "capacitance", "dielectric", "electric flux")
    ),
    TopicProfile(
      canonicalTopic = "Current Electricity",
      subject = AcademicSubject.PHYSICS,
      subtopics = listOf("Ohm's Law & Resistance", "Drift Velocity & Mobility", "Kirchhoff's Laws", "Wheatstone Bridge & Potentiometer", "EMF & Internal Resistance", "Electrical Power"),
      exams = listOf("JEE Main", "NEET UG", "Class 10 Boards", "Class 12 Boards"),
      educationLevel = "Class 10-12",
      keywords = listOf("current electricity", "ohm's law", "ohm law", "resistors in series", "kirchhoff law", "wheatstone bridge", "potentiometer", "drift velocity", "internal resistance", "electric current")
    ),
    TopicProfile(
      canonicalTopic = "Optics: Ray and Wave Optics",
      subject = AcademicSubject.PHYSICS,
      subtopics = listOf("Reflection & Refraction", "Snell's Law & Total Internal Reflection", "Lenses & Mirrors Formula", "Optical Instruments (Microscope, Telescope)", "Huygens' Principle", "Interference & Young's Double Slit", "Diffraction"),
      exams = listOf("JEE Main", "JEE Advanced", "NEET UG", "Class 10-12 Boards"),
      educationLevel = "Class 10-12",
      keywords = listOf("optics", "ray optics", "wave optics", "snell's law", "total internal reflection", "young double slit", "ydse", "diffraction", "lens formula", "refractive index", "microscope", "telescope")
    ),

    // MATHEMATICS
    TopicProfile(
      canonicalTopic = "Differential Calculus & Derivatives",
      subject = AcademicSubject.MATHEMATICS,
      subtopics = listOf("Concept of Derivative", "Product, Quotient & Chain Rule", "Derivatives of Trigonometric & Exponential Functions", "Implicit Differentiation", "Logarithmic Differentiation", "Applications of Derivatives (Maxima/Minima, Tangents & Normals)"),
      exams = listOf("JEE Main", "JEE Advanced", "Class 12 Boards", "Engineering Entrance"),
      educationLevel = "Class 11-12",
      keywords = listOf("differentiation", "derivative", "differential calculus", "chain rule", "product rule", "quotient rule", "implicit differentiation", "maxima and minima", "tangent and normal", "dy/dx", "rate of change")
    ),
    TopicProfile(
      canonicalTopic = "Integral Calculus",
      subject = AcademicSubject.MATHEMATICS,
      subtopics = listOf("Indefinite Integrals & Substitution", "Integration by Parts", "Partial Fractions", "Definite Integrals & Properties", "Area Under Curves", "Differential Equations"),
      exams = listOf("JEE Main", "JEE Advanced", "Class 12 Boards"),
      educationLevel = "Class 12",
      keywords = listOf("integration", "integral calculus", "definite integral", "indefinite integral", "integration by parts", "partial fraction", "area under curve", "differential equation", "antiderivative")
    ),
    TopicProfile(
      canonicalTopic = "Matrices and Determinants",
      subject = AcademicSubject.MATHEMATICS,
      subtopics = listOf("Types of Matrices", "Matrix Operations (Addition, Multiplication)", "Determinant Properties", "Minors and Cofactors", "Adjoint and Inverse of a Matrix", "Cramer's Rule & Linear Systems"),
      exams = listOf("JEE Main", "Class 12 Boards", "SSC CGL"),
      educationLevel = "Class 12",
      keywords = listOf("matrices", "matrix", "determinant", "inverse of matrix", "adjoint", "cramer rule", "matrix multiplication", "eigenvalue", "system of linear equations")
    ),
    TopicProfile(
      canonicalTopic = "Coordinate Geometry & Conic Sections",
      subject = AcademicSubject.MATHEMATICS,
      subtopics = listOf("Straight Lines & Slope", "Circles & Tangents", "Parabola", "Ellipse", "Hyperbola", "Eccentricity & Directrix"),
      exams = listOf("JEE Main", "JEE Advanced", "Class 11 Boards"),
      educationLevel = "Class 11",
      keywords = listOf("coordinate geometry", "straight line", "parabola", "ellipse", "hyperbola", "conic section", "circle equation", "eccentricity", "slope of line", "latus rectum")
    ),
    TopicProfile(
      canonicalTopic = "Trigonometry & Inverse Trigonometric Functions",
      subject = AcademicSubject.MATHEMATICS,
      subtopics = listOf("Trigonometric Ratios & Identities", "Compound Angles & Multiple Angles", "Trigonometric Equations", "Inverse Trigonometric Functions & Properties", "Heights and Distances"),
      exams = listOf("JEE Main", "Class 10 Boards", "Class 11-12 Boards", "NDA"),
      educationLevel = "Class 10-12",
      keywords = listOf("trigonometry", "sin theta", "cos theta", "tan theta", "trigonometric identities", "inverse trigonometric", "arcsin", "heights and distances", "radian")
    ),

    // BIOLOGY
    TopicProfile(
      canonicalTopic = "Photosynthesis in Higher Plants",
      subject = AcademicSubject.BIOLOGY,
      subtopics = listOf("Chloroplast Structure & Pigments", "Light Reaction (Photophosphorylation, Z-Scheme)", "Dark Reaction (Calvin C3 Cycle)", "C4 Pathway & CAM", "Photorespiration", "Factors Affecting Photosynthesis"),
      exams = listOf("NEET UG", "Class 11 Boards"),
      educationLevel = "Class 11",
      keywords = listOf("photosynthesis", "calvin cycle", "light reaction", "dark reaction", "chlorophyll", "chloroplast", "c3 pathway", "c4 pathway", "rubisco", "photophosphorylation", "thylakoid")
    ),
    TopicProfile(
      canonicalTopic = "Cell Biology: Cell Structure and Function",
      subject = AcademicSubject.BIOLOGY,
      subtopics = listOf("Prokaryotic vs Eukaryotic Cells", "Cell Membrane & Fluid Mosaic Model", "Cell Organelles (Mitochondria, ER, Golgi)", "Nucleus & Chromatin", "Cell Cycle & Mitosis/Meiosis"),
      exams = listOf("NEET UG", "Class 9 Boards", "Class 11 Boards"),
      educationLevel = "Class 9-11",
      keywords = listOf("cell biology", "mitosis", "meiosis", "cell cycle", "mitochondria", "ribosome", "golgi", "endoplasmic reticulum", "fluid mosaic", "prokaryote", "eukaryote", "chromosome")
    ),
    TopicProfile(
      canonicalTopic = "Genetics and Molecular Biology",
      subject = AcademicSubject.BIOLOGY,
      subtopics = listOf("Mendelian Inheritance & Laws", "Incomplete Dominance & Codominance", "DNA Structure & Double Helix", "DNA Replication", "Transcription & Translation", "Genetic Code & Mutations", "Lac Operon"),
      exams = listOf("NEET UG", "Class 10 Boards", "Class 12 Boards"),
      educationLevel = "Class 10-12",
      keywords = listOf("genetics", "mendel", "dna replication", "transcription", "translation", "rna", "mutation", "allele", "inheritance", "lac operon", "double helix", "pedigree analysis")
    ),
    TopicProfile(
      canonicalTopic = "Human Physiology: Circulatory & Excretory Systems",
      subject = AcademicSubject.BIOLOGY,
      subtopics = listOf("Blood Composition & Blood Groups", "Heart Structure & Cardiac Cycle", "ECG & Blood Pressure", "Kidney Anatomy & Nephron Function", "Urine Formation", "Regulation of Kidney Function"),
      exams = listOf("NEET UG", "Class 10 Boards", "Class 11 Boards"),
      educationLevel = "Class 10-11",
      keywords = listOf("human physiology", "circulatory system", "cardiac cycle", "heart", "ecg", "blood group", "nephron", "kidney", "excretion", "dialysis", "glomerular filtration")
    ),

    // SOCIAL SCIENCES & OTHER
    TopicProfile(
      canonicalTopic = "Modern Indian History & Freedom Struggle",
      subject = AcademicSubject.HISTORY,
      subtopics = listOf("Revolt of 1857", "Rise of Nationalism & INC", "Gandhian Movements (Non-Cooperation, Civil Disobedience, Quit India)", "Partition and Independence", "Social Reform Movements"),
      exams = listOf("UPSC Civil Services", "SSC CGL", "Class 10 Boards"),
      educationLevel = "Class 10 / Civil Services",
      keywords = listOf("indian national movement", "freedom struggle", "mahatma gandhi", "revolt of 1857", "quit india", "non cooperation", "civil disobedience", "subhas chandra bose", "british raj", "east india company")
    ),
    TopicProfile(
      canonicalTopic = "Indian Constitution & Polity",
      subject = AcademicSubject.POLITICAL_SCIENCE,
      subtopics = listOf("Preamble & Salient Features", "Fundamental Rights & Duties", "Directive Principles of State Policy (DPSP)", "Parliament & President of India", "Judiciary & Supreme Court", "Constitutional Amendments"),
      exams = listOf("UPSC Civil Services", "SSC CGL", "State PCS", "Class 10-12 Boards"),
      educationLevel = "Senior Secondary / Civil Services",
      keywords = listOf("constitution of india", "fundamental rights", "dpsp", "preamble", "supreme court", "parliament of india", "president of india", "lok sabha", "rajya sabha", "article 21", "article 32")
    ),
    TopicProfile(
      canonicalTopic = "Physical & Indian Geography",
      subject = AcademicSubject.GEOGRAPHY,
      subtopics = listOf("Plate Tectonics & Earthquakes", "Atmosphere & Monsoons", "Drainage Systems & Rivers of India", "Soil Types & Agriculture", "Natural Resources & Climate"),
      exams = listOf("UPSC Civil Services", "SSC CGL", "Class 9-10 Boards"),
      educationLevel = "Class 9-10 / Competitive",
      keywords = listOf("geography", "plate tectonics", "monsoon in india", "himalayas", "ganga river", "soil erosion", "volcano", "earthquake", "climate zones", "lithosphere")
    ),
    TopicProfile(
      canonicalTopic = "Computer Science: Data Structures & Algorithms",
      subject = AcademicSubject.COMPUTER_SCIENCE,
      subtopics = listOf("Arrays, Linked Lists, Stacks, Queues", "Trees & Graphs", "Sorting & Searching Algorithms", "Time & Space Complexity (Big-O)", "Object-Oriented Programming (OOP)"),
      exams = listOf("GATE CS", "Class 12 CS", "University Engineering"),
      educationLevel = "Higher Secondary / Undergrad",
      keywords = listOf("data structures", "algorithms", "binary tree", "linked list", "recursion", "stack and queue", "big o notation", "sorting algorithm", "object oriented programming", "python programming")
    )
  )

  /**
   * Main classification method: determines the subject, canonical topic,
   * subtopics, and flags ambiguities.
   */
  fun classifyTopic(input: String, userSelectedSubject: AcademicSubject? = null): ClassificationResult {
    val clean = input.trim().lowercase(Locale.ROOT)

    // 1. Check for genuine multidisciplinary ambiguity first
    for ((ambKey, options) in ambiguousTopics) {
      if (clean == ambKey || clean.startsWith("$ambKey ") || clean.endsWith(" $ambKey") || clean.contains(" $ambKey ")) {
        // Check if query itself or userSelectedSubject provides the resolving context
        val resolvedSubject = when {
          clean.contains("chem") -> AcademicSubject.CHEMISTRY
          clean.contains("phys") -> AcademicSubject.PHYSICS
          clean.contains("math") -> AcademicSubject.MATHEMATICS
          clean.contains("bio") -> AcademicSubject.BIOLOGY
          clean.contains("econ") -> AcademicSubject.ECONOMICS
          else -> userSelectedSubject
        }

        if (resolvedSubject != null && resolvedSubject != AcademicSubject.OTHER) {
          val matchingProfile = taxonomy.find { 
            it.subject == resolvedSubject
          }
          if (matchingProfile != null) {
            return ClassificationResult(
              subject = matchingProfile.subject,
              topic = matchingProfile.canonicalTopic,
              subtopics = matchingProfile.subtopics,
              relevantExams = matchingProfile.exams,
              educationLevel = matchingProfile.educationLevel,
              isAmbiguous = false
            )
          }
        }

        // Return Ambiguity Alert!
        return ClassificationResult(
          subject = AcademicSubject.OTHER,
          topic = input.trim(),
          subtopics = emptyList(),
          relevantExams = listOf("All relevant competitive & board curricula"),
          educationLevel = "Variable",
          isAmbiguous = true,
          ambiguityOptions = options,
          clarificationPrompt = "The term \"${input.trim()}\" spans multiple distinct subjects. Which academic domain do you want to study?"
        )
      }
    }

    // 2. Score match against taxonomy profiles
    var bestProfile: TopicProfile? = null
    var bestScore = 0

    for (profile in taxonomy) {
      var score = 0
      for (kw in profile.keywords) {
        if (clean == kw) {
          score += 10
        } else if (clean.contains(kw)) {
          score += 5
        } else {
          val words = kw.split(" ")
          val matchCount = words.count { clean.contains(it) }
          if (matchCount > 1) {
            score += matchCount * 2
          }
        }
      }

      // If user selected this subject manually and there's some keyword match, grant a bias
      if (userSelectedSubject != null && profile.subject == userSelectedSubject && score > 0) {
        score += 3
      }

      if (score > bestScore) {
        bestScore = score
        bestProfile = profile
      }
    }

    if (bestProfile != null && bestScore >= 3) {
      return ClassificationResult(
        subject = bestProfile.subject,
        topic = bestProfile.canonicalTopic,
        subtopics = bestProfile.subtopics,
        relevantExams = bestProfile.exams,
        educationLevel = bestProfile.educationLevel,
        isAmbiguous = false,
        confidence = if (bestScore >= 10) 1.0f else 0.8f
      )
    }

    // 3. Fallback: Keyword heuristic detection if no exact taxonomy entry hit
    val heuristicSubject = detectSubjectFromHeuristic(clean)
    val finalSubject = if (heuristicSubject != AcademicSubject.OTHER) {
      heuristicSubject
    } else if (userSelectedSubject != null && userSelectedSubject != AcademicSubject.OTHER) {
      userSelectedSubject
    } else {
      AcademicSubject.OTHER
    }

    return ClassificationResult(
      subject = finalSubject,
      topic = input.trim(),
      subtopics = listOf("Fundamental Concepts", "Key Principles & Theories", "Formulas & Equations", "Applications & Practice Questions"),
      relevantExams = listOf("Standard Board & Competitive Exams"),
      educationLevel = "Standard Academic Curriculum",
      isAmbiguous = false,
      confidence = 0.6f
    )
  }

  /**
   * Detects if the student's manually chosen subject directly conflicts
   * with the entered topic (e.g., User picked "Physics", entered "Chemical Bonding").
   */
  fun checkSubjectMismatch(
    topicInput: String,
    userSelectedSubject: AcademicSubject
  ): SubjectMismatch {
    if (userSelectedSubject == AcademicSubject.OTHER) {
      return SubjectMismatch(false, userSelectedSubject, AcademicSubject.OTHER, "")
    }

    val classification = classifyTopic(topicInput, userSelectedSubject = null)
    if (!classification.isAmbiguous && 
        classification.subject != AcademicSubject.OTHER && 
        classification.subject != userSelectedSubject &&
        classification.confidence >= 0.8f) {
      return SubjectMismatch(
        isMismatch = true,
        userSelectedSubject = userSelectedSubject,
        detectedSubject = classification.subject,
        explanation = "\"${topicInput.trim()}\" is a core concept in ${classification.subject.displayName}, but \"${userSelectedSubject.displayName}\" was selected. StudyNova AI will lock to ${classification.subject.displayName} to prevent erroneous content generation."
      )
    }

    return SubjectMismatch(false, userSelectedSubject, userSelectedSubject, "")
  }

  private fun detectSubjectFromHeuristic(text: String): AcademicSubject {
    val chemClues = listOf("reaction", "molecule", "atom", "bond", "acid", "base", "solution", "organic", "inorganic", "compound", "valence", "electron", "proton", "molar", "stoichiometry", "catalyst", "synthesis", "precipitation", "titration")
    val physClues = listOf("velocity", "acceleration", "force", "gravity", "friction", "inertia", "momentum", "torque", "energy", "power", "charge", "current", "voltage", "resistance", "magnetic", "lens", "mirror", "oscillation", "wave", "thermodynamics", "optics")
    val mathClues = listOf("derivative", "integral", "matrix", "determinant", "trigonometry", "algebra", "calculus", "geometry", "probability", "equation", "theorem", "logarithm", "function", "limit", "vector", "polynomial")
    val bioClues = listOf("cell", "organ", "tissue", "dna", "rna", "gene", "chromosome", "plant", "animal", "photosynthesis", "respiration", "digestion", "circulation", "excretion", "reproduction", "bacteria", "virus", "enzyme", "hormone", "ecosystem")

    val chemCount = chemClues.count { text.contains(it) }
    val physCount = physClues.count { text.contains(it) }
    val mathCount = mathClues.count { text.contains(it) }
    val bioCount = bioClues.count { text.contains(it) }

    val max = maxOf(chemCount, physCount, mathCount, bioCount)
    if (max == 0) return AcademicSubject.OTHER

    return when (max) {
      chemCount -> AcademicSubject.CHEMISTRY
      physCount -> AcademicSubject.PHYSICS
      mathCount -> AcademicSubject.MATHEMATICS
      bioCount -> AcademicSubject.BIOLOGY
      else -> AcademicSubject.OTHER
    }
  }
}
