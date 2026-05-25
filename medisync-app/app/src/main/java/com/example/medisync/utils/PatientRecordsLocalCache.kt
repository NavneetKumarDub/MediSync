package com.example.medisync.utils

import android.content.Context
import com.example.medisync.networks.PatientRecordDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.security.MessageDigest

object PatientRecordsLocalCache {
    private const val PREF_NAME = "patient_records_cache"
    private const val RECORDS_PREFIX = "records_"
    private val gson = Gson()

    fun save(context: Context, ownerKey: String, records: List<PatientRecordDto>) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(RECORDS_PREFIX + sha256(ownerKey), gson.toJson(records))
            .apply()
    }

    fun load(context: Context, ownerKey: String): List<PatientRecordDto> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(RECORDS_PREFIX + sha256(ownerKey), null)
            ?: return emptyList()

        return runCatching {
            val type = object : TypeToken<List<PatientRecordDto>>() {}.type
            gson.fromJson<List<PatientRecordDto>>(json, type)
        }.getOrDefault(emptyList())
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(24)
    }
}
