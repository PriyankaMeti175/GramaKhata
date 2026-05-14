package com.gramakhata.app.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("GramaKhataPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_NAME = "user_name"
    }

    fun saveUser(phone: String, name: String) {
        prefs.edit().apply {
            putString(KEY_USER_PHONE, phone)
            putString(KEY_USER_NAME, name)
            apply()
        }
    }

    fun getUserPhone(): String? = prefs.getString(KEY_USER_PHONE, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, "User")

    fun logout() {
        prefs.edit().apply {
            remove(KEY_USER_PHONE)
            remove(KEY_USER_NAME)
            apply()
        }
    }
}
