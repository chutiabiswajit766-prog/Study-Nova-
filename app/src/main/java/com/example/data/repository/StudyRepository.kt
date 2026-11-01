package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Flashcard
import com.example.data.model.MockTestResult
import com.example.data.model.StudyMaterial
import com.example.data.model.StudyPlan
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "StudyRepository"

class StudyRepository(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth = Firebase.auth

  fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing StudyNova cloud data.")
  }

  // --- USER PROFILE ---
  fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
    val docRef = db.collection("users").document(userId)
    val registration = docRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        Log.e(TAG, "Error observing user profile", error)
        close(error)
        return@addSnapshotListener
      }
      val profile = snapshot?.toObject(UserProfile::class.java)
      trySend(profile)
    }
    awaitClose { registration.remove() }
  }

  suspend fun saveUserProfile(profile: UserProfile) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    val payload = profile.toMap() + mapOf(
      "userId" to uid,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload).await()
  }

  suspend fun addXpPoints(amount: Long) {
    val uid = auth.currentUser?.uid ?: return
    val docRef = db.collection("users").document(uid)
    try {
      docRef.update("points", FieldValue.increment(amount)).await()
    } catch (e: Exception) {
      Log.w(TAG, "Could not update XP points", e)
    }
  }

  // --- STUDY MATERIALS ---
  fun observeStudyMaterials(userId: String): Flow<List<StudyMaterial>> {
    val collectionRef = db.collection("users").document(userId).collection("materials")
    return collectionRef.snapshots()
      .map { snapshot -> snapshot.toObjects(StudyMaterial::class.java) }
      .catch { e ->
        Log.e(TAG, "Error observing study materials", e)
        emit(emptyList())
      }
  }

  suspend fun saveStudyMaterial(material: StudyMaterial) {
    val uid = requireUserId()
    val collectionRef = db.collection("users").document(uid).collection("materials")
    val id = if (material.materialId.isNotBlank()) material.materialId else collectionRef.document().id
    val docRef = collectionRef.document(id)
    val payload = material.copy(materialId = id, userId = uid).toMap() + mapOf(
      "userId" to uid,
      "materialId" to id,
      "createdAt" to (material.createdAt ?: FieldValue.serverTimestamp()),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload).await()
    addXpPoints(20)
  }

  suspend fun toggleFavoriteMaterial(material: StudyMaterial) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("materials").document(material.materialId)
    docRef.update(
      "isFavorite", !material.isFavorite,
      "updatedAt", FieldValue.serverTimestamp()
    ).await()
  }

  suspend fun deleteStudyMaterial(userId: String, materialId: String) {
    val uid = requireUserId()
    db.collection("users").document(uid).collection("materials").document(materialId).delete().await()
  }

  // --- FLASHCARDS ---
  fun observeFlashcards(userId: String): Flow<List<Flashcard>> {
    val collectionRef = db.collection("users").document(userId).collection("flashcards")
    return collectionRef.snapshots()
      .map { snapshot -> snapshot.toObjects(Flashcard::class.java) }
      .catch { e ->
        Log.e(TAG, "Error observing flashcards", e)
        emit(emptyList())
      }
  }

  suspend fun saveFlashcard(flashcard: Flashcard) {
    val uid = requireUserId()
    val collectionRef = db.collection("users").document(uid).collection("flashcards")
    val id = if (flashcard.cardId.isNotBlank()) flashcard.cardId else collectionRef.document().id
    val docRef = collectionRef.document(id)
    val payload = flashcard.copy(cardId = id, userId = uid).toMap() + mapOf(
      "userId" to uid,
      "cardId" to id,
      "createdAt" to (flashcard.createdAt ?: FieldValue.serverTimestamp()),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload).await()
    addXpPoints(10)
  }

  suspend fun updateFlashcardReview(cardId: String, newLevel: Long, reviewCount: Long) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("flashcards").document(cardId)
    docRef.update(
      "repetitionLevel", newLevel,
      "reviewCount", reviewCount,
      "updatedAt", FieldValue.serverTimestamp()
    ).await()
    addXpPoints(5)
  }

  suspend fun deleteFlashcard(cardId: String) {
    val uid = requireUserId()
    db.collection("users").document(uid).collection("flashcards").document(cardId).delete().await()
  }

  // --- MOCK TEST RESULTS ---
  fun observeMockResults(userId: String): Flow<List<MockTestResult>> {
    val collectionRef = db.collection("users").document(userId).collection("mock_results")
    return collectionRef.snapshots()
      .map { snapshot -> snapshot.toObjects(MockTestResult::class.java) }
      .catch { e ->
        Log.e(TAG, "Error observing mock results", e)
        emit(emptyList())
      }
  }

  suspend fun saveMockResult(result: MockTestResult) {
    val uid = requireUserId()
    val collectionRef = db.collection("users").document(uid).collection("mock_results")
    val id = if (result.resultId.isNotBlank()) result.resultId else collectionRef.document().id
    val docRef = collectionRef.document(id)
    val payload = result.copy(resultId = id, userId = uid).toMap() + mapOf(
      "userId" to uid,
      "resultId" to id,
      "createdAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload).await()
    addXpPoints(50)
  }

  // --- STUDY PLANS ---
  fun observeStudyPlan(userId: String): Flow<StudyPlan?> = callbackFlow {
    val collectionRef = db.collection("users").document(userId).collection("study_plans")
    val registration = collectionRef.limit(1).addSnapshotListener { snapshot, error ->
      if (error != null) {
        close(error)
        return@addSnapshotListener
      }
      val plan = snapshot?.documents?.firstOrNull()?.toObject(StudyPlan::class.java)
      trySend(plan)
    }
    awaitClose { registration.remove() }
  }

  suspend fun saveStudyPlan(plan: StudyPlan) {
    val uid = requireUserId()
    val collectionRef = db.collection("users").document(uid).collection("study_plans")
    val id = if (plan.planId.isNotBlank()) plan.planId else collectionRef.document().id
    val docRef = collectionRef.document(id)
    val payload = plan.copy(planId = id, userId = uid).toMap() + mapOf(
      "userId" to uid,
      "planId" to id,
      "createdAt" to (plan.createdAt ?: FieldValue.serverTimestamp()),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload).await()
  }
}
