package com.ledger.ledgerapp.network
import com.ledger.ledgerapp.network.models.Budget
import com.ledger.ledgerapp.network.models.BudgetRequest
import com.ledger.ledgerapp.network.models.LoginRequest
import com.ledger.ledgerapp.network.models.LoginResponse
import com.ledger.ledgerapp.network.models.RegisterRequest
import com.ledger.ledgerapp.network.models.RegisterResponse
import com.ledger.ledgerapp.network.models.Transaction
import com.ledger.ledgerapp.network.models.TransactionRequest
import com.ledger.ledgerapp.network.models.TransactionResponse
import com.ledger.ledgerapp.network.models.TransactionListResponse
import com.ledger.ledgerapp.network.models.TransactionSummaryResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // 认证相关
    @FormUrlEncoded
    @POST("api/login")
    suspend fun login(
        @Field("username") username : String,
        @Field("password") password : String
    ): Response<LoginResponse>

    @POST("api/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>
    
    // 交易记录相关
    // 所有交易API都在 /api/transactions 路径下
    @GET("api/transactions")
    suspend fun getTransactions(
        @Query("skip") skip: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("type") type: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("category") category: String? = null
    ): Response<TransactionListResponse>
    
    @GET("api/transactions/{id}")
    suspend fun getTransaction(@Path("id") id: Int): Response<TransactionResponse>
    
    @POST("api/transactions")
    suspend fun createTransaction(
        @Body transaction: TransactionRequest
    ): Response<TransactionResponse>
    
    @PUT("api/transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: Int,
        @Body transaction: TransactionRequest
    ): Response<TransactionResponse>
    
    @DELETE("api/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Int): Response<Unit>
    
    // 获取交易统计摘要
    @GET("api/transactions/summary/statistics")
    suspend fun getTransactionSummary(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<TransactionSummaryResponse>

    // 图片上传
    @Multipart
    @POST("api/upload/image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<Map<String, String>>

    // 预算相关
    @GET("api/budgets")
    suspend fun getBudgets(
        @Query("month") month: String
    ): Response<List<Budget>>

    @POST("api/budgets")
    suspend fun setBudget(
        @Body budget: BudgetRequest
    ): Response<Budget>
}
