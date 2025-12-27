package com.ledger.ledgerapp.network

import android.util.Log
import com.ledger.ledgerapp.data.TokenManager
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    private const val TAG = "RetrofitClient"
    
    // 确保使用正确的 BASE_URL - 直接硬编码避免编译缓存问题
    private const val BASE_URL = "http://10.0.2.2:8000/"
    
    init {
        Log.d(TAG, "Initializing RetrofitClient with BASE_URL: $BASE_URL")
    }
    
    // 日志拦截器
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // 拦截和记录 HTTP 请求/响应的详细信息
        level = HttpLoggingInterceptor.Level.BODY // BODY记录完整的请求和响应体（包括 headers 和 body）
    }

    fun createOkHttpClient(tokenManager: TokenManager? = null): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        
        // 如果提供了 TokenManager，添加认证拦截器
        tokenManager?.let {
            builder.addInterceptor(AuthInterceptor(it))
        }
        
        return builder.build()
    }

    private val okHttpClient = createOkHttpClient()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create()) // Gson 转换器将 JSON 响应自动转换为 Kotlin 数据类
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
    
    // 创建带认证的 API 服务
    fun createAuthenticatedApiService(tokenManager: TokenManager): ApiService {
        val client = createOkHttpClient(tokenManager)
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}