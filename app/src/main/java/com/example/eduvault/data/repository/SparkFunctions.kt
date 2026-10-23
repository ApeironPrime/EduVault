package com.example.eduvault.data.repository

import com.example.eduvault.BuildConfig
import com.google.firebase.functions.FirebaseFunctions

/** Keep unsupported Spark actions explicit instead of calling absent Functions. */
internal fun FirebaseFunctions.availableCallable(name: String) = run {
    check(!BuildConfig.SPARK_ONLY) { "Tính năng này cần backend Cloud Functions và chưa bật trong bản Spark 0 phí." }
    getHttpsCallable(name)
}
