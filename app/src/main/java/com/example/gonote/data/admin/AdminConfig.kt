package com.example.gonote.data.admin

import com.example.gonote.BuildConfig

object AdminConfig {
    val ADMIN_EMAIL = BuildConfig.ADMIN_EMAIL
    val ADMIN_PASSWORD = BuildConfig.ADMIN_PASSWORD
    const val ADMIN_USER_ID = "admin_gonote_001"

    fun validateCredentials(email: String, password: String): Boolean {
        return email.lowercase().trim() == ADMIN_EMAIL.lowercase() && password == ADMIN_PASSWORD
    }
}





















