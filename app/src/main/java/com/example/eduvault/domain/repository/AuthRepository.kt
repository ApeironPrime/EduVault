package com.example.eduvault.domain.repository

import com.example.eduvault.domain.model.User

/**
 * Interface của AuthRepository — định nghĩa hợp đồng cho tầng data.
 *
 * Quy tắc tuân thủ:
 * - Interface này không biết gì về Firebase, Retrofit, hay bất kỳ thư viện bên ngoài nào.
 * - Mọi phương thức trả về Result<T> để xử lý lỗi an toàn kiểu (type-safe).
 * - ViewModel chỉ gọi interface này, không quan tâm đến implementation.
 */
interface AuthRepository {

    /**
     * Đăng nhập bằng email/password.
     * @return [Result.success] với [User] nếu thành công, [Result.failure] nếu thất bại.
     */
    suspend fun login(email: String, password: String): Result<User>

    /**
     * Đăng ký tài khoản mới.
     * Tạo user trong Firebase Auth VÀ lưu thông tin vào Firestore.
     * @return [Result.success] với [User] nếu thành công, [Result.failure] nếu thất bại.
     */
    suspend fun register(fullName: String, email: String, password: String): Result<User>

    /**
     * Gửi email đặt lại mật khẩu.
     * @return [Result.success] nếu email được gửi, [Result.failure] nếu email không tồn tại.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Xác thực mã đặt lại mật khẩu (oobCode) từ Firebase Auth email link.
     * @return [Result.success] với email của tài khoản tương ứng nếu mã hợp lệ, [Result.failure] nếu mã sai hoặc hết hạn.
     */
    suspend fun verifyPasswordResetCode(code: String): Result<String>

    /**
     * Đặt lại mật khẩu mới sử dụng mã xác thực oobCode đã được phê duyệt.
     */
    suspend fun confirmPasswordReset(oobCode: String, newPassword: String): Result<Unit>

    /**
     * Lấy thông tin người dùng hiện tại từ Firestore (nếu đã đăng nhập).
     * @return [Result.success] với [User] hoặc null nếu chưa đăng nhập.
     */
    suspend fun getCurrentUser(): Result<User?>

    /**
     * Cập nhật thông tin hồ sơ của người dùng hiện tại.
     */
    suspend fun updateProfile(fullName: String, university: String, avatarUrl: String): Result<User>

    /**
     * Tăng lượt sử dụng tài liệu khả dụng (+ amount).
     */
    suspend fun addCredits(amount: Int): Result<User>

    /**
     * Giảm 1 lượt sử dụng tài liệu khả dụng (-1).
     */
    suspend fun consumeCredit(): Result<User>

    /**
     * Mở khóa tài liệu bằng 1 Credit.
     */
    suspend fun unlockDocument(docId: String): Result<User>

    /**
     * Lấy danh sách bộ đề từ collection "quizzes" trên Firestore.
     */
    suspend fun getQuizSets(): Result<List<com.example.eduvault.feature.quiz.ui.QuizSet>>

    /**
     * Lấy danh sách câu hỏi chi tiết của đề từ Firestore.
     */
    suspend fun getQuizQuestions(quizId: String): Result<List<com.example.eduvault.feature.quiz.ui.QuizQuestion>>

    /**
     * Lấy toàn bộ danh sách người dùng từ Firestore để tính điểm bảng xếp hạng.
     */
    suspend fun getAllUsers(): Result<List<User>>

    /**
     * Cập nhật chỉ số thi đua trắc nghiệm và điểm XP của người dùng lên Firestore bằng Transaction an toàn.
     */
    suspend fun updateUserQuizStats(score: Float, xpEarned: Int): Result<User>

    /**
     * Lưu hoặc bỏ lưu tài liệu (toggle bookmark) đồng bộ trực tiếp lên Firestore.
     */
    suspend fun toggleSaveDocument(docId: String): Result<User>

    /**
     * Xác thực với Firebase Authentication bằng Google ID Token và đồng bộ hóa Firestore.
     */
    suspend fun loginWithGoogle(idToken: String): Result<User>

    /**
     * Thay đổi mật khẩu người dùng hiện tại.
     * Xác thực lại bằng mật khẩu hiện tại rồi đổi sang mật khẩu mới.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    /**
     * Lưu bộ đề trắc nghiệm AI đã tạo lên Firestore.
     */
    suspend fun saveQuizSet(quizSet: com.example.eduvault.feature.quiz.ui.QuizSet, questions: List<com.example.eduvault.feature.quiz.ui.QuizQuestion>): Result<Unit>

    /**
     * Đăng xuất.
     */
    fun logout()
}


