package com.example.liga_ja

data class Contact(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val calculationCode: String,
    val actionType: String = "CALL", // CALL ou WHATSAPP
    val whatsappMessage: String = "Preciso de ajuda. Entre em contato comigo imediatamente."
)