package com.example.util

import android.content.Context
import android.util.Log
import com.example.BuildConfig

object AiConfigHelper {

    private const val PREFS_NAME = "dev_sprint_ai_config"
    private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"

    fun getCustomGeminiApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CUSTOM_API_KEY, null)?.trim().orEmpty()
    }

    fun isUserKeyConfigured(context: Context): Boolean {
        return getCustomGeminiApiKey(context).isNotBlank()
    }

    fun getGeminiApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val customKey = prefs.getString(KEY_CUSTOM_API_KEY, null)?.trim().orEmpty()
        if (customKey.isNotBlank()) {
            return customKey
        }

        // Check if injected via BuildConfig from secrets (safe reflection)
        return try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            val buildConfigKey = (field.get(null) as? String)?.trim().orEmpty()
            if (buildConfigKey.isNotBlank() && !buildConfigKey.contains("MY_GEMINI_API_KEY") && !buildConfigKey.contains("YOUR_")) {
                buildConfigKey
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun setCustomGeminiApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun clearCustomGeminiApiKey(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_CUSTOM_API_KEY).apply()
    }

    fun getAppCheckDebugToken(context: Context): String? {
        return try {
            val persistenceKey = com.google.firebase.FirebaseApp.getInstance().persistenceKey
            val prefs = context.getSharedPreferences(
                "com.google.firebase.appcheck.debug.store.$persistenceKey",
                Context.MODE_PRIVATE
            )
            prefs.getString("com.google.firebase.appcheck.debug.DEBUG_SECRET", null)
        } catch (e: Exception) {
            Log.w("AiConfigHelper", "Could not read debug token: ${e.message}")
            null
        }
    }
}
