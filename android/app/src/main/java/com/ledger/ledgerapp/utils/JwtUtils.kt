package com.ledger.ledgerapp.utils

import android.util.Base64
import org.json.JSONObject

object JwtUtils {
    fun decodeJwt(token : String): Map<String, Any>? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val jsonObject = JSONObject(payload)
            val map = mutableMapOf<String, Any>()
            jsonObject.keys().forEach { key ->
                map[key] = jsonObject.get(key)
            }
            map
        } catch (e: Exception) {
            null
        }
    }

    fun getUsernameFromToken(token : String) : String? {
        val decoded = decodeJwt(token)
        return decoded?.get("sub") as? String
    }
}