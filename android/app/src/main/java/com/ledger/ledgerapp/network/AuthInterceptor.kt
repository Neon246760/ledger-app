package com.ledger.ledgerapp.network

import android.util.Log
import com.ledger.ledgerapp.data.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 如果是登录接口，不需要添加 token
        if (originalRequest.url.encodedPath.contains("login")) {
            return chain.proceed(originalRequest)
        }
        
        // 获取 token
        val token = runBlocking {
            tokenManager.getToken()
        }
        
        // 如果有 token，添加到请求头
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
}

