package com.example.liga_ja

data class Contact(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val calculationCode: String
)
