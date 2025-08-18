package com.example.siginak.communicationitems

import com.google.firebase.Timestamp

data class yardimTalebi(
    val id: String = "",
    val title: String = "",
    val detail: String = "",
    val address: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)

