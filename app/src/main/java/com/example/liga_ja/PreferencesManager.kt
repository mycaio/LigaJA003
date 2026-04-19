package com.example.liga_ja

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("liga_ja_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_EMERGENCY_NUMBER = "emergency_number"
        private const val KEY_CALL_CODE = "call_code"
        private const val KEY_SETTINGS_CODE = "settings_code"
        private const val KEY_CONTACTS = "contacts"
        private const val KEY_CRUD_CODE = "crud_code"
    }

    fun getContacts(): List<Contact> {
        val json = prefs.getString(KEY_CONTACTS, null) ?: return emptyList()
        val type = object : TypeToken<List<Contact>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveContacts(contacts: List<Contact>) {
        val json = gson.toJson(contacts)
        prefs.edit().putString(KEY_CONTACTS, json).apply()
    }

    fun getCrudCode(): String {
        return prefs.getString(KEY_CRUD_CODE, "111+") ?: "111+"
    }

    fun saveCrudCode(code: String) {
        prefs.edit().putString(KEY_CRUD_CODE, code).apply()
    }

    fun getEmergencyNumber(): String {
        return prefs.getString(KEY_EMERGENCY_NUMBER, "190") ?: "190"
    }

    fun saveEmergencyNumber(number: String) {
        prefs.edit().putString(KEY_EMERGENCY_NUMBER, number).apply()
    }

    fun getCallCode(): String {
        return prefs.getString(KEY_CALL_CODE, "190+") ?: "190+"
    }

    fun saveCallCode(code: String) {
        prefs.edit().putString(KEY_CALL_CODE, code).apply()
    }

    fun getSettingsCode(): String {
        return prefs.getString(KEY_SETTINGS_CODE, "444+") ?: "444+"
    }

    fun saveSettingsCode(code: String) {
        prefs.edit().putString(KEY_SETTINGS_CODE, code).apply()
    }

    fun getWhatsAppNumber(): String =
        prefs.getString("whatsapp_number", "5543999999999") ?: "5543999999999"

    fun saveWhatsAppNumber(number: String) {
        prefs.edit().putString("whatsapp_number", number).apply()
    }

    fun getWhatsAppCode(): String =
        prefs.getString("whatsapp_code", "555+") ?: "555+"

    fun saveWhatsAppCode(code: String) {
        prefs.edit().putString("whatsapp_code", code).apply()
    }

    fun getWhatsAppMessage(): String =
        prefs.getString(
            "whatsapp_message",
            "Preciso de ajuda. Entre em contato comigo imediatamente."
        ) ?: "Preciso de ajuda. Entre em contato comigo imediatamente."

    fun saveWhatsAppMessage(message: String) {
        prefs.edit().putString("whatsapp_message", message).apply()
    }
}