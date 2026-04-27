package com.example.medisync.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Single DataStore instance per app
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "medisync_prefs")

object TokenManager {

    private val TOKEN_KEY  = stringPreferencesKey("jwt_token")
    private val USER_ID_KEY = intPreferencesKey("user_id")
    private val ROLE_KEY   = stringPreferencesKey("user_role")

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun getToken(context: Context): String? {
        return context.dataStore.data
            .map { it[TOKEN_KEY] }
            .first()
    }

    suspend fun saveUserId(context: Context, userId: Int) {
        context.dataStore.edit { it[USER_ID_KEY] = userId }
    }

    suspend fun getUserId(context: Context): Int? {
        return context.dataStore.data
            .map { it[USER_ID_KEY] }
            .first()
    }

    suspend fun saveRole(context: Context, role: String) {
        context.dataStore.edit { it[ROLE_KEY] = role }
    }

    suspend fun getRole(context: Context): String? {
        return context.dataStore.data
            .map { it[ROLE_KEY] }
            .first()
    }

    suspend fun clear(context: Context) {
        context.dataStore.edit { it.clear() }
    }
}