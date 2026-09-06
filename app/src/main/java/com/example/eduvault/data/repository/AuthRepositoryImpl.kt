package com.example.eduvault.data.repository

import com.example.eduvault.domain.model.User
import com.example.eduvault.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Triển khai thực của [AuthRepository] sử dụng Firebase Auth + Firestore.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Đăng nhập thất bại. Vui lòng thử lại."))

            val user = fetchUserFromFirestore(firebaseUser.uid)
                ?: return Result.failure(Exception("Thông tin người dùng không tồn tại trên Firestore."))

            if (user.isBlocked) {
                return Result.failure(Exception("Tài khoản của bạn đã bị khóa bởi Admin."))
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    // ─── Register ─────────────────────────────────────────────────────────────

    override suspend fun register(fullName: String, email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Đăng ký thất bại."))

            // Cập nhật Profile trong Firebase Auth
            val profileUpdates = userProfileChangeRequest {
                displayName = fullName
            }
            firebaseUser.updateProfile(profileUpdates).await()

            val newUser = User(
                uid = firebaseUser.uid,
                fullName = fullName,
                email = email,
                documentCredits = 1,
                uploadCount = 0,
                createdAt = System.currentTimeMillis()
            )

            saveUserToFirestore(newUser)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    // ─── Password Reset ───────────────────────────────────────────────────────

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    override suspend fun verifyPasswordResetCode(code: String): Result<String> {
        return try {
            val email = firebaseAuth.verifyPasswordResetCode(code).await()
            Result.success(email)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    override suspend fun confirmPasswordReset(oobCode: String, newPassword: String): Result<Unit> {
        return try {
            firebaseAuth.confirmPasswordReset(oobCode, newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    // ─── Current User ─────────────────────────────────────────────────────────

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                return Result.success(null)
            }
            val user = fetchUserFromFirestore(firebaseUser.uid)
            if (user != null && user.isBlocked) {
                firebaseAuth.signOut()
                return Result.failure(Exception("Tài khoản của bạn đã bị khóa bởi Admin."))
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    // ─── Credits Management ───────────────────────────────────────────────────

    override suspend fun addCredits(amount: Int): Result<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("Người dùng chưa đăng nhập."))
        return try {
            val userRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
            val snapshot = userRef.get().await()
            val currentCredits = snapshot.getLong("documentCredits")?.toInt() ?: 1
            val newCredits = currentCredits + amount
            
            userRef.update("documentCredits", newCredits).await()
            
            val updatedUser = fetchUserFromFirestore(firebaseUser.uid)
                ?: return Result.failure(Exception("Không thể cập nhật thông tin người dùng."))
            
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    override suspend fun consumeCredit(): Result<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("Người dùng chưa đăng nhập."))
        return try {
            val userRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
            val snapshot = userRef.get().await()
            val currentCredits = snapshot.getLong("documentCredits")?.toInt() ?: 1
            if (currentCredits <= 0) {
                return Result.failure(Exception("Bạn đã hết lượt sử dụng tài liệu."))
            }
            val newCredits = currentCredits - 1
            userRef.update("documentCredits", newCredits).await()
            
            val updatedUser = fetchUserFromFirestore(firebaseUser.uid)
                ?: return Result.failure(Exception("Không thể cập nhật thông tin người dùng."))
            
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    override suspend fun unlockDocument(docId: String): Result<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("Người dùng chưa đăng nhập."))
        return try {
            val userRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
            val docRef = firestore.collection("documents").document(docId)
            
            firestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val currentCredits = userSnapshot.getLong("documentCredits")?.toInt() ?: 1
                val unlockedList = userSnapshot.get("unlockedDocuments") as? List<*>
                val unlockedDocs = unlockedList?.mapNotNull { it?.toString() } ?: emptyList()
                
                if (unlockedDocs.contains(docId)) {
                    return@runTransaction
                }
                
                val docSnapshot = transaction.get(docRef)
                if (!docSnapshot.exists()) {
                    throw Exception("Tài liệu không tồn tại trên hệ thống.")
                }
                val fileSizeBytes = docSnapshot.getLong("fileSizeBytes") ?: 0L
                val quantityLabel = docSnapshot.getString("quantityLabel") ?: ""
                val cost = calculatePrice(fileSizeBytes, quantityLabel)
                
                if (currentCredits < cost) {
                    throw Exception("Bạn cần $cost credit để mở khóa tài liệu này, nhưng hiện tại bạn chỉ có $currentCredits credit. Hãy chia sẻ 1 tài liệu học tập của mình để nhận thêm lượt!")
                }
                
                val newCredits = currentCredits - cost
                val newUnlockedDocs = unlockedDocs + docId
                transaction.update(userRef, mapOf(
                    "documentCredits" to newCredits,
                    "unlockedDocuments" to newUnlockedDocs
                ))
            }.await()
            
            val updatedUser = fetchUserFromFirestore(firebaseUser.uid)
                ?: return Result.failure(Exception("Không thể cập nhật thông tin người dùng."))
            
            Result.success(updatedUser)
        } catch (e: Exception) {
            val rawMsg = e.message ?: ""
            val friendlyMsg = when {
                rawMsg.contains("Bạn cần") || rawMsg.contains("Tài liệu không") -> rawMsg
                e.cause?.message?.contains("Bạn cần") == true -> e.cause?.message ?: ""
                e.cause?.message?.contains("Tài liệu không") == true -> e.cause?.message ?: ""
                else -> mapFirebaseError(e.message)
            }
            Result.failure(Exception(friendlyMsg))
        }
    }

    override suspend fun getQuizSets(): Result<List<com.example.eduvault.feature.quiz.ui.QuizSet>> {
        return try {
            val snapshot = firestore.collection("quizzes").get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                val quizID = doc.getString("quizID") ?: doc.id
                val authorId = doc.getString("authorId") ?: ""
                val documentID = doc.getString("documentID") ?: ""
                val difficultyStr = doc.getString("difficulty") ?: "Medium"
                val questionCount = doc.getLong("questionCount")?.toInt() ?: 10
                
                val difficulty = when (difficultyStr.lowercase()) {
                    "easy", "dễ" -> com.example.eduvault.feature.quiz.ui.Difficulty.EASY
                    "hard", "khó" -> com.example.eduvault.feature.quiz.ui.Difficulty.HARD
                    else -> com.example.eduvault.feature.quiz.ui.Difficulty.MEDIUM
                }
                
                val subject = doc.getString("subject")
                    ?: doc.getString("documentID")
                    ?: (if (quizID.contains("Marketing", ignoreCase = true)) "MKT301 - Marketing" else "EC0201 - Kinh tế vi mô")
                val title = doc.getString("title")
                    ?: "Đề luyện tập: ${quizID}"
                
                com.example.eduvault.feature.quiz.ui.QuizSet(
                    id = quizID,
                    subject = subject,
                    title = title,
                    difficulty = difficulty,
                    questionCount = questionCount,
                    playCount = "1",
                    isNew = true,
                    bgIndex = Math.abs(quizID.hashCode() % 6)
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    override suspend fun getQuizQuestions(quizId: String): Result<List<com.example.eduvault.feature.quiz.ui.QuizQuestion>> {
        return try {
            val doc = firestore.collection("quizzes").document(quizId).get().await()
            if (!doc.exists()) {
                return Result.failure(Exception("Đề thi không tồn tại"))
            }
            parseQuestionsFromDoc(doc)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    private fun parseQuestionsFromDoc(doc: com.google.firebase.firestore.DocumentSnapshot): Result<List<com.example.eduvault.feature.quiz.ui.QuizQuestion>> {
        return try {
            val rawQuestions = doc.get("questions") as? List<Map<String, Any>> ?: emptyList()
            val parsedList = rawQuestions.mapIndexed { idx, qMap ->
                val content = qMap["content"] as? String ?: ""
                val correctAnswerIndex = (qMap["correctAnswer"] as? Number)?.toInt() ?: 0
                val explanation = qMap["explanation"] as? String ?: ""
                val rawOptions = qMap["options"] as? List<String> ?: emptyList()
                
                val alphabet = listOf("A", "B", "C", "D")
                val optionsList = rawOptions.mapIndexed { optIdx, optText ->
                    val optKey = alphabet.getOrNull(optIdx) ?: "A"
                    com.example.eduvault.feature.quiz.ui.QuizAnswer(
                        key = optKey,
                        text = optText
                    )
                }
                
                val correctOption = alphabet.getOrNull(correctAnswerIndex) ?: "A"
                
                com.example.eduvault.feature.quiz.ui.QuizQuestion(
                    id = "firestore_q_${idx}",
                    text = content,
                    hint = "",
                    options = optionsList,
                    correctOption = correctOption,
                    explanation = explanation
                )
            }
            Result.success(parsedList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION).get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                val uid = doc.id
                val rawCreatedAt = doc.get("createdAt")
                val milliseconds = when (rawCreatedAt) {
                    is com.google.firebase.Timestamp -> rawCreatedAt.toDate().time
                    is Long -> rawCreatedAt
                    is Number -> rawCreatedAt.toLong()
                    else -> System.currentTimeMillis()
                }
                User(
                    uid = doc.getString("uid") ?: uid,
                    fullName = doc.getString("name") ?: doc.getString("fullName") ?: "",
                    email = doc.getString("email") ?: "",
                    university = doc.getString("university") ?: "",
                    avatarUrl = doc.getString("avatarUrl") ?: "",
                    documentCredits = doc.getLong("documentCredits")?.toInt() ?: 1,
                    uploadCount = doc.getLong("uploadCount")?.toInt() ?: 0,
                    createdAt = milliseconds,
                    role = doc.getString("role") ?: "user",
                    isBlocked = doc.getBoolean("isBlocked") ?: false,
                    unlockedDocuments = (doc.get("unlockedDocuments") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                    quizCount = doc.getLong("quizCount")?.toInt() ?: 0,
                    quizAverageScore = doc.getDouble("quizAverageScore")?.toFloat() ?: 0.0f,
                    totalXp = doc.getLong("totalXp")?.toInt() ?: 0,
                    savedDocuments = (doc.get("savedDocuments") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(Exception("Lấy danh sách người dùng thất bại: ${e.localizedMessage}"))
        }
    }

    override suspend fun updateUserQuizStats(score: Float, xpEarned: Int): Result<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("Người dùng chưa đăng nhập."))
        return try {
            val userRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
            
            val updatedUser = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                
                val currentQuizCount = snapshot.getLong("quizCount")?.toInt() ?: 0
                val currentAvgScore = snapshot.getDouble("quizAverageScore")?.toFloat() ?: 0.0f
                val currentTotalXp = snapshot.getLong("totalXp")?.toInt() ?: 0
                
                val newQuizCount = currentQuizCount + 1
                val newAvgScore = (currentAvgScore * currentQuizCount + score) / newQuizCount
                val newTotalXp = currentTotalXp + xpEarned
                
                transaction.update(
                    userRef,
                    mapOf(
                        "quizCount" to newQuizCount,
                        "quizAverageScore" to newAvgScore,
                        "totalXp" to newTotalXp
                    )
                )
                
                val rawCreatedAt = snapshot.get("createdAt")
                val milliseconds = when (rawCreatedAt) {
                    is com.google.firebase.Timestamp -> rawCreatedAt.toDate().time
                    is Long -> rawCreatedAt
                    is Number -> rawCreatedAt.toLong()
                    else -> System.currentTimeMillis()
                }
                
                User(
                    uid = firebaseUser.uid,
                    fullName = snapshot.getString("name") ?: snapshot.getString("fullName") ?: "",
                    email = snapshot.getString("email") ?: "",
                    university = snapshot.getString("university") ?: "",
                    avatarUrl = snapshot.getString("avatarUrl") ?: "",
                    documentCredits = snapshot.getLong("documentCredits")?.toInt() ?: 1,
                    uploadCount = snapshot.getLong("uploadCount")?.toInt() ?: 0,
                    createdAt = milliseconds,
                    role = snapshot.getString("role") ?: "user",
                    isBlocked = snapshot.getBoolean("isBlocked") ?: false,
                    unlockedDocuments = (snapshot.get("unlockedDocuments") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                    quizCount = newQuizCount,
                    quizAverageScore = newAvgScore,
                    totalXp = newTotalXp,
                    savedDocuments = (snapshot.get("savedDocuments") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                )
            }.await()
            
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    override suspend fun toggleSaveDocument(docId: String): Result<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("Người dùng chưa đăng nhập."))
        return try {
            val userRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
            val snapshot = userRef.get().await()
            val savedList = snapshot.get("savedDocuments") as? List<*>
            val savedDocs = savedList?.mapNotNull { it?.toString() } ?: emptyList()
            
            val isAlreadySaved = savedDocs.contains(docId)
            val updateValue = if (isAlreadySaved) {
                com.google.firebase.firestore.FieldValue.arrayRemove(docId)
            } else {
                com.google.firebase.firestore.FieldValue.arrayUnion(docId)
            }
            
            userRef.update("savedDocuments", updateValue).await()
            
            val updatedUser = fetchUserFromFirestore(firebaseUser.uid)
                ?: return Result.failure(Exception("Không thể cập nhật thông tin người dùng."))
            
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Đăng nhập bằng Google thất bại."))

            // Kiểm tra xem document users/{uid} đã tồn tại trên Firestore chưa
            val userRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
            val snapshot = userRef.get().await()

            val user = if (snapshot.exists()) {
                // Đăng nhập lại lần sau -> Nạp thông tin đã có sẵn
                val rawCreatedAt = snapshot.get("createdAt")
                val milliseconds = when (rawCreatedAt) {
                    is com.google.firebase.Timestamp -> rawCreatedAt.toDate().time
                    is Long -> rawCreatedAt
                    is Number -> rawCreatedAt.toLong()
                    else -> System.currentTimeMillis()
                }
                User(
                    uid = firebaseUser.uid,
                    fullName = snapshot.getString("name") ?: snapshot.getString("fullName") ?: firebaseUser.displayName ?: "Người dùng Google",
                    email = snapshot.getString("email") ?: firebaseUser.email ?: "",
                    university = snapshot.getString("university") ?: "",
                    avatarUrl = snapshot.getString("avatarUrl") ?: firebaseUser.photoUrl?.toString() ?: "",
                    documentCredits = snapshot.getLong("documentCredits")?.toInt() ?: 1,
                    uploadCount = snapshot.getLong("uploadCount")?.toInt() ?: 0,
                    createdAt = milliseconds,
                    role = snapshot.getString("role") ?: "user",
                    isBlocked = snapshot.getBoolean("isBlocked") ?: false,
                    unlockedDocuments = (snapshot.get("unlockedDocuments") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                    quizCount = snapshot.getLong("quizCount")?.toInt() ?: 0,
                    quizAverageScore = snapshot.getDouble("quizAverageScore")?.toFloat() ?: 0.0f,
                    totalXp = snapshot.getLong("totalXp")?.toInt() ?: 0,
                    savedDocuments = (snapshot.get("savedDocuments") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                )
            } else {
                // Đăng nhập lần đầu tiên -> Tạo document mới đầy đủ dữ liệu giống đăng ký thường
                val newUser = User(
                    uid = firebaseUser.uid,
                    fullName = firebaseUser.displayName ?: "Người dùng Google",
                    email = firebaseUser.email ?: "",
                    university = "",
                    avatarUrl = firebaseUser.photoUrl?.toString() ?: "",
                    documentCredits = 5, // Cấp 5 credits chào mừng cho tài khoản Google mới!
                    uploadCount = 0,
                    createdAt = System.currentTimeMillis(),
                    role = "user",
                    isBlocked = false,
                    unlockedDocuments = emptyList(),
                    quizCount = 0,
                    quizAverageScore = 0.0f,
                    totalXp = 0,
                    savedDocuments = emptyList()
                )
                // Lưu vào Firestore
                saveUserToFirestore(newUser)
                newUser
            }

            if (user.isBlocked) {
                firebaseAuth.signOut()
                return Result.failure(Exception("Tài khoản của bạn đã bị khóa bởi Admin."))
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi xác thực Google: ${e.localizedMessage}"))
        }
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    override fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("Người dùng chưa đăng nhập."))
        val email = firebaseUser.email
            ?: return Result.failure(Exception("Không tìm thấy email của tài khoản."))
        return try {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
            firebaseUser.reauthenticate(credential).await()
            firebaseUser.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val rawMsg = e.message ?: ""
            val errorMsg = when {
                rawMsg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                rawMsg.contains("wrong-password", ignoreCase = true) ||
                rawMsg.contains("WRONG_PASSWORD", ignoreCase = true) ->
                    "Mật khẩu hiện tại không chính xác."
                rawMsg.contains("weak-password", ignoreCase = true) ||
                rawMsg.contains("WEAK_PASSWORD", ignoreCase = true) ->
                    "Mật khẩu mới quá yếu. Mật khẩu phải có ít nhất 6 ký tự."
                else -> mapFirebaseError(rawMsg)
            }
            Result.failure(Exception(errorMsg))
        }
    }

    override suspend fun saveQuizSet(
        quizSet: com.example.eduvault.feature.quiz.ui.QuizSet,
        questions: List<com.example.eduvault.feature.quiz.ui.QuizQuestion>
    ): Result<Unit> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("Người dùng chưa đăng nhập."))
        return try {
            val alphabet = listOf("A", "B", "C", "D")
            val quizMap = hashMapOf(
                "quizID" to quizSet.id,
                "authorId" to firebaseUser.uid,
                "subject" to quizSet.subject,
                "title" to quizSet.title,
                "difficulty" to when (quizSet.difficulty) {
                    com.example.eduvault.feature.quiz.ui.Difficulty.EASY -> "easy"
                    com.example.eduvault.feature.quiz.ui.Difficulty.HARD -> "hard"
                    else -> "medium"
                },
                "questionCount" to quizSet.questionCount,
                "questions" to questions.map { q ->
                    val correctIdx = alphabet.indexOf(q.correctOption).coerceAtLeast(0)
                    hashMapOf(
                        "content" to q.text,
                        "correctAnswer" to correctIdx,
                        "explanation" to q.explanation,
                        "options" to q.options.map { it.text }
                    )
                }
            )
            firestore.collection("quizzes").document(quizSet.id).set(quizMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    override suspend fun updateProfile(
        fullName: String,
        university: String,
        avatarUrl: String
    ): Result<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("Người dùng chưa đăng nhập."))
        return try {
            // Cập nhật profile trong Firebase Auth (displayName)
            val profileUpdates = userProfileChangeRequest {
                displayName = fullName
            }
            firebaseUser.updateProfile(profileUpdates).await()

            // Cập nhật trong Firestore
            val userRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
            val updates = hashMapOf<String, Any>(
                "fullName" to fullName,
                "university" to university,
                "avatarUrl" to avatarUrl
            )
            userRef.update(updates).await()

            val updatedUser = fetchUserFromFirestore(firebaseUser.uid)
                ?: return Result.failure(Exception("Không thể cập nhật thông tin người dùng."))

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * Lưu [User] vào Firestore collection "users".
     */
    private suspend fun saveUserToFirestore(user: User) {
        try {
            val userMap = hashMapOf(
                "uid" to user.uid,
                "name" to user.fullName,
                "email" to user.email,
                "university" to user.university,
                "avatarUrl" to user.avatarUrl,
                "documentCredits" to user.documentCredits,
                "uploadCount" to user.uploadCount,
                "createdAt" to com.google.firebase.Timestamp(java.util.Date(user.createdAt)),
                "role" to user.role,
                "isBlocked" to user.isBlocked,
                "unlockedDocuments" to user.unlockedDocuments,
                "quizCount" to user.quizCount,
                "quizAverageScore" to user.quizAverageScore,
                "totalXp" to user.totalXp,
                "savedDocuments" to user.savedDocuments
            )
            firestore
                .collection(USERS_COLLECTION)
                .document(user.uid)
                .set(userMap)
                .await()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "saveUserToFirestore failed for uid=${user.uid}: ${e.localizedMessage}")
            throw e  // Re-throw so callers can handle (e.g. rollback auth account)
        }
    }

    /**
     * Lấy [User] từ Firestore theo UID.
     */
    private suspend fun fetchUserFromFirestore(uid: String): User? {
        val snapshot = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .await()

        if (!snapshot.exists()) return null

        val rawCreatedAt = snapshot.get("createdAt")
        val milliseconds = when (rawCreatedAt) {
            is com.google.firebase.Timestamp -> rawCreatedAt.toDate().time
            is Long -> rawCreatedAt
            is Number -> rawCreatedAt.toLong()
            else -> System.currentTimeMillis()
        }

        return User(
            uid = snapshot.getString("uid") ?: uid,
            fullName = snapshot.getString("name") ?: snapshot.getString("fullName") ?: "",
            email = snapshot.getString("email") ?: "",
            university = snapshot.getString("university") ?: "",
            avatarUrl = snapshot.getString("avatarUrl") ?: "",
            documentCredits = snapshot.getLong("documentCredits")?.toInt() ?: 1,
            uploadCount = snapshot.getLong("uploadCount")?.toInt() ?: 0,
            createdAt = milliseconds,
            role = snapshot.getString("role") ?: "user",
            isBlocked = snapshot.getBoolean("isBlocked") ?: false,
            unlockedDocuments = (snapshot.get("unlockedDocuments") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            quizCount = snapshot.getLong("quizCount")?.toInt() ?: 0,
            quizAverageScore = snapshot.getDouble("quizAverageScore")?.toFloat() ?: 0.0f,
            totalXp = snapshot.getLong("totalXp")?.toInt() ?: 0,
            savedDocuments = (snapshot.get("savedDocuments") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        )
    }

    /**
     * Map Firebase error messages sang tiếng Việt thân thiện với người dùng.
     */
    private fun mapFirebaseError(message: String?): String {
        return when {
            message == null -> "Đã xảy ra lỗi. Vui lòng thử lại."
            message.contains("network", ignoreCase = true) ||
                    message.contains("NETWORK", ignoreCase = true) ->
                "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet."
            message.contains("too-many-requests", ignoreCase = true) ||
                    message.contains("TOO_MANY_ATTEMPTS", ignoreCase = true) ->
                "Quá nhiều lần thử. Vui lòng đợi vài phút rồi thử lại."
            message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ->
                "Email hoặc mật khẩu không đúng."
            else -> "Đã xảy ra lỗi. Vui lòng thử lại."
        }
    }

    private fun calculatePrice(fileSizeBytes: Long, quantityLabel: String): Int {
        if (fileSizeBytes > 0L) {
            val sizeInMb = fileSizeBytes / (1024.0 * 1024.0)
            return when {
                sizeInMb <= 2.0 -> 1
                sizeInMb <= 5.0 -> 2
                else -> 3
            }
        }
        val cleanStr = quantityLabel.replace(",", ".").trim()
        if (cleanStr.endsWith("MB", ignoreCase = true)) {
            val sizeInMb = cleanStr.replace("MB", "", ignoreCase = true).trim().toDoubleOrNull() ?: 0.0
            return when {
                sizeInMb <= 2.0 -> 1
                sizeInMb <= 5.0 -> 2
                else -> 3
            }
        }
        return 1
    }
}
