package com.example.util

import android.util.Log
import org.json.JSONObject

enum class FirestoreOperationType {
    CREATE,
    UPDATE,
    DELETE,
    LIST,
    GET,
    WRITE
}

object FirestoreErrorHandler {
    private const val TAG = "FirestoreError"

    fun handleFirestoreError(
        error: Exception,
        operationType: FirestoreOperationType,
        path: String?,
        deviceId: String? = null
    ) {
        val payload = JSONObject().apply {
            put("error", error.message ?: error.javaClass.simpleName)
            put("operationType", operationType.name.lowercase())
            put("path", path ?: JSONObject.NULL)
            put("deviceId", deviceId ?: JSONObject.NULL)
            put("authMode", "device_license_no_login")
        }
        Log.e(TAG, "Firestore Error: $payload", error)
    }
}
