package com.example.eduvault.di

import com.example.eduvault.data.repository.AuthRepositoryImpl
import com.example.eduvault.data.repository.DocumentRepositoryImpl
import com.example.eduvault.data.repository.CategoryRepositoryImpl
import com.example.eduvault.data.repository.AdminRepositoryImpl
import com.example.eduvault.data.repository.AiRepositoryImpl
import com.example.eduvault.data.repository.NotificationRepositoryImpl
import com.example.eduvault.data.repository.NoteRepositoryImpl
import com.example.eduvault.data.repository.FlashcardRepositoryImpl
import com.example.eduvault.data.repository.TrainingRepositoryImpl
import com.example.eduvault.data.repository.MentorRepositoryImpl
import com.example.eduvault.domain.repository.NoteRepository
import com.example.eduvault.domain.repository.AuthRepository
import com.example.eduvault.domain.repository.DocumentRepository
import com.example.eduvault.domain.repository.CategoryRepository
import com.example.eduvault.domain.repository.AdminRepository
import com.example.eduvault.domain.repository.AiRepository
import com.example.eduvault.domain.repository.NotificationRepository
import com.example.eduvault.domain.repository.FlashcardRepository
import com.example.eduvault.domain.repository.TrainingRepository
import com.example.eduvault.domain.repository.MentorRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Repository bindings and the shared Firebase cloud instances. */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(impl: AdminRepositoryImpl): AdminRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindFlashcardRepository(impl: FlashcardRepositoryImpl): FlashcardRepository

    @Binds
    @Singleton
    abstract fun bindTrainingRepository(impl: TrainingRepositoryImpl): TrainingRepository

    @Binds
    @Singleton
    abstract fun bindMentorRepository(impl: MentorRepositoryImpl): MentorRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseApp(): FirebaseApp = FirebaseApp.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseAuth(app: FirebaseApp): FirebaseAuth = FirebaseAuth.getInstance(app)

        @Provides
        @Singleton
        fun provideFirebaseFirestore(app: FirebaseApp): FirebaseFirestore = FirebaseFirestore.getInstance(app)

        @Provides
        @Singleton
        fun provideFirebaseFunctions(app: FirebaseApp): FirebaseFunctions = FirebaseFunctions.getInstance(app, "asia-southeast1")

        @Provides
        @Singleton
        fun provideFirebaseStorage(app: FirebaseApp): FirebaseStorage = FirebaseStorage.getInstance(app)
    }
}
