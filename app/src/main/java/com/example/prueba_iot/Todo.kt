package com.example.prueba_iot

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Expense(
    val id: String = "",
    val `object`: String = "",
    val price: Double = 0.0,
    val qty: Int = 0,
    val userId: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
