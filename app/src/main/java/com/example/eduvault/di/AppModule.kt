package com.example.eduvault.di

import com.example.eduvault.data.repository.AuthRepositoryImpl
import com.example.eduvault.data.repository.DocumentRepositoryImpl
import com.example.eduvault.data.repository.CategoryRepositoryImpl
import com.example.eduvault.data.repository.AdminRepositoryImpl
import com.example.eduvault.data.repository.AiRepositoryImpl
import com.example.eduvault.data.repository.NotificationRepositoryImpl
import com.example.eduvault.data.repository.NoteRepositoryImpl
import com.example.eduvault.data.repository.FlashcardRepositoryImpl
import com.example.eduvault.domain.repository.NoteRepository
import com.example.eduvault.domain.repository.AuthRepository
import com.example.eduvault.domain.repository.DocumentRepository
import com.example.eduvault.domain.repository.CategoryRepository
import com.example.eduvault.domain.repository.AdminRepository
import com.example.eduvault.domain.repository.AiRepository
import com.example.eduvault.domain.repository.NotificationRepository
import com.example.eduvault.domain.repository.FlashcardRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module cung cấp các dependency cho toàn app.
 *
 * Quy tắc: Module này là nơi DUY NHẤT biết về Firebase và các implementation cụ thể.
 * ViewModel và Repository interface không cần biết chúng đến từ đâu.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * Bind AuthRepositoryImpl vào interface AuthRepository.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    /**
     * Bind DocumentRepositoryImpl vào interface DocumentRepository.
     */
    @Binds
    @Singleton
    abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository

    /**
     * Bind CategoryRepositoryImpl vào interface CategoryRepository.
     */
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    /**
     * Bind AdminRepositoryImpl vào interface AdminRepository.
     */
    @Binds
    @Singleton
    abstract fun bindAdminRepository(impl: AdminRepositoryImpl): AdminRepository

    /**
     * Bind AiRepositoryImpl vào interface AiRepository.
     */
    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository

    /**
     * Bind NotificationRepositoryImpl vào interface NotificationRepository.
     */
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    /**
     * Bind NoteRepositoryImpl vào interface NoteRepository.
     */
    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindFlashcardRepository(impl: FlashcardRepositoryImpl): FlashcardRepository

    companion object {

        /**
         * Cung cấp instance FirebaseAuth duy nhất (Singleton).
         */
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        /**
         * Cung cấp instance FirebaseFirestore duy nhất (Singleton).
         */
        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseFunctions(): FirebaseFunctions = FirebaseFunctions.getInstance("asia-southeast1")

        /**
         * Cung cấp instance FirebaseStorage duy nhất (Singleton).
         */
        @Provides
        @Singleton
        fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
    }
}
