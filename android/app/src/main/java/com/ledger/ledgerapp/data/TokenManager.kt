package com.ledger.ledgerapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ledger.ledgerapp.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map

private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "token_prefs")

class TokenManager(private val context: Context) {
    private val tokenKey = stringPreferencesKey(Constants.TOKEN_PREF_KEY)
    private val tokenTypeKey = stringPreferencesKey(Constants.TOKEN_TYPE_KEY)

    val token : Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[tokenKey]
    }

    suspend fun saveToken(token : String, tokenType : String) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = token
            preferences[tokenTypeKey] = tokenType
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(tokenKey)
            preferences.remove(tokenTypeKey)
        }
    }

    // 有问题的实现！!
    suspend fun getToken() : String? {
        //return context.dataStore.data.map { it[tokenKey] }.collect { it }
        return token.first()
    }
}