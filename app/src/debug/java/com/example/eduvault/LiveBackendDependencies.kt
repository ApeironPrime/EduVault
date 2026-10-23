package com.example.eduvault

import com.example.eduvault.domain.repository.AuthRepository
import com.example.eduvault.domain.repository.TrainingRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LiveBackendDependencies {
    fun auth(): AuthRepository
    fun training(): TrainingRepository
    fun firebaseAuth(): FirebaseAuth
}
