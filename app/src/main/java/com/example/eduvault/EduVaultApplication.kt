package com.example.eduvault

import android.app.Application
import android.content.pm.ApplicationInfo
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EduVaultApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val provider = if (isDebuggable) debugAppCheckProvider() else null
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            provider ?: PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }

    /** Uses reflection so the debug-only provider never becomes a release dependency. */
    private fun debugAppCheckProvider(): AppCheckProviderFactory? = runCatching {
        val providerClass = Class.forName(
            "com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory"
        )
        providerClass.getMethod("getInstance").invoke(null) as AppCheckProviderFactory
    }.getOrNull()
}
