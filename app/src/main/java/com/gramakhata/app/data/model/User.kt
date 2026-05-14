package com.gramakhata.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val phone: String,
    val name: String,
    val password: String,
    val photoUri: String? = null,
    val dob: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
