package com.example.siginak.communicationitems

data class Deprem(
    val dateTime: String,
    val latitude: Double,
    val longitude: Double,
    val depth: Double,
    val magnitude: Double,
    val location: String
)

