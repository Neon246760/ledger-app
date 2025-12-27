package com.ledger.ledgerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.ledgerapp.data.TokenManager
import com.ledger.ledgerapp.network.RetrofitClient
import com.ledger.ledgerapp.network.models.Transaction
import com.ledger.ledgerapp.network.models.TransactionRequest
import com.ledger.ledgerapp.network.models.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

data class CategoryStat(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class DailyStat(
    val date: String,
    val income: Double,
    val expense: Double
)

data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val expenseStats: List<CategoryStat> = emptyList(),
    val dailyStats: List<DailyStat> = emptyList(),
    // Filter states
    val filterType: TransactionType? = null,
    val filterStartDate: String? = null,
    val filterEndDate: String? = null,
    val filterCategory: String? = null
)


class TransactionViewModel(private val tokenManager: TokenManager) : ViewModel() {
    private val apiService = RetrofitClient.createAuthenticatedApiService(tokenManager)
    
    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()
    
    // 移除init块中的loadTransactions()调用，避免在ViewModel创建时立即加载数据
    // 数据加载应该由UI层在合适的时机触发（如HomeScreen中的LaunchedEffect）
    
    fun loadTransactions(
        type: TransactionType? = null,
        startDate: String? = null,
        endDate: String? = null,
        category: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null,
                filterType = type,
                filterStartDate = startDate,
                filterEndDate = endDate,
                filterCategory = category
            )
            
            try {
                val response = apiService.getTransactions(
                    type = type?.name?.lowercase(),
                    startDate = startDate,
                    endDate = endDate,
                    category = category
                )
                
                if (response.isSuccessful) {
                    val transactionList = response.body()?.items ?: emptyList()
                    
                    // 计算总收入和总支出
                    val totalIncome = transactionList
                        .filter { it.type == "income" }
                        .sumOf { it.amount }
                    val totalExpense = transactionList
                        .filter { it.type == "expense" }
                        .sumOf { it.amount }
                    val balance = totalIncome - totalExpense
                    
                    // 计算分类统计
                    val expenseStats = transactionList
                        .filter { it.type == "expense" }
                        .groupBy { it.category }
                        .map { (category, transactions) ->
                            val amount = transactions.sumOf { it.amount }
                            CategoryStat(
                                category = category,
                                amount = amount,
                                percentage = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                            )
                        }
                        .sortedByDescending { it.amount }

                    // 计算每日收支统计
                    val dailyStats = transactionList
                        .groupBy { 
                            // 提取日期部分 (YYYY-MM-DD)
                            if (it.date.length >= 10) it.date.substring(0, 10) else it.date 
                        }
                        .map { (date, transactions) ->
                            val dailyIncome = transactions
                                .filter { it.type == "income" }
                                .sumOf { it.amount }
                            val dailyExpense = transactions
                                .filter { it.type == "expense" }
                                .sumOf { it.amount }
                            DailyStat(date, dailyIncome, dailyExpense)
                        }
                        .sortedBy { it.date }

                    _uiState.value = _uiState.value.copy(
                        transactions = transactionList,
                        isLoading = false,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        balance = balance,
                        expenseStats = expenseStats,
                        dailyStats = dailyStats
                    )
                } else {
                    // 如果是401错误，清除token
                    if (response.code() == 401) {
                        tokenManager.clearToken()
                    }
                    
                    val errorMessage = when (response.code()) {
                        401 -> "未授权，请重新登录"
                        403 -> "无权限访问"
                        404 -> "接口不存在"
                        else -> "加载失败: ${response.message()}"
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "网络错误: ${e.message}"
                )
            }
        }
    }
    
    fun createTransaction(
        amount: Double,
        type: TransactionType,
        category: String,
        description: String?,
        imagePath: String?,
        date: String,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val request = TransactionRequest(
                    amount = amount,
                    type = type.name.lowercase(),
                    category = category,
                    description = description,
                    imagePath = imagePath,
                    date = date
                )
                
                val response = apiService.createTransaction(request)
                
                if (response.isSuccessful) {
                    loadTransactions() // 重新加载列表
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when {
                        response.code() == 404 -> "接口不存在 (404): 请检查API路径是否正确"
                        response.code() == 401 -> "未授权 (401): 请重新登录"
                        response.code() == 403 -> "无权限 (403): 请检查账户权限"
                        errorBody != null -> "创建失败: $errorBody"
                        else -> "创建失败: ${response.message()} (${response.code()})"
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                val errorMsg = when {
                    e.message?.contains("404") == true -> "接口不存在: 请检查API路径是否为 /api/transactions"
                    e.message?.contains("401") == true -> "未授权: 请重新登录"
                    else -> "网络错误: ${e.message}"
                }
                onError(errorMsg)
            }
        }
    }
    
    fun uploadImage(
        file: File,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                val response = apiService.uploadImage(body)
                
                if (response.isSuccessful) {
                    val url = response.body()?.get("url")
                    if (url != null) {
                        onSuccess(url)
                    } else {
                        onError("Upload failed: No URL returned")
                    }
                } else {
                    onError("Upload failed: ${response.message()}")
                }
            } catch (e: Exception) {
                onError("Upload error: ${e.message}")
            }
        }
    }

    fun updateTransaction(
        id: Int,
        amount: Double,
        type: TransactionType,
        category: String,
        description: String?,
        imagePath: String?,
        date: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val request = TransactionRequest(
                    amount = amount,
                    type = type.name.lowercase(),
                    category = category,
                    description = description,
                    imagePath = imagePath,
                    date = date
                )
                
                val response = apiService.updateTransaction(id, request)
                
                if (response.isSuccessful) {
                    loadTransactions() // 重新加载列表
                    onSuccess()
                } else {
                    val errorMsg = "更新失败: ${response.message()} (${response.code()})"
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onError("网络错误: ${e.message}")
            }
        }
    }
    
    fun deleteTransaction(
        id: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val response = apiService.deleteTransaction(id)
                
                if (response.isSuccessful) {
                    loadTransactions() // 重新加载列表
                    onSuccess()
                } else {
                    val errorMsg = "删除失败: ${response.message()}"
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onError("网络错误: ${e.message}")
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    // 根据 ID 获取单个 transaction
    suspend fun getTransactionById(id: Int): Transaction? {
        return try {
            val response = apiService.getTransaction(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

