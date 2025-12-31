package com.ledger.ledgerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.ledgerapp.data.TokenManager
import com.ledger.ledgerapp.network.RetrofitClient
import com.ledger.ledgerapp.network.models.Budget
import com.ledger.ledgerapp.network.models.BudgetRequest
import com.ledger.ledgerapp.network.models.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BudgetUiState(
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentMonth: String = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()),
    val categorySpending: Map<String, Double> = emptyMap(),
    val totalSpending: Double = 0.0
)

class BudgetViewModel(private val tokenManager: TokenManager) : ViewModel() {
    private val apiService = RetrofitClient.createAuthenticatedApiService(tokenManager)
    
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    fun loadBudgetData(month: String = _uiState.value.currentMonth) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, currentMonth = month)
            try {
                // 1. Get Budgets
                val budgetResponse = apiService.getBudgets(month)
                
                // 2. Get Transactions for spending calculation
                // Calculate start and end date for the month
                val calendar = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                calendar.time = sdf.parse(month)!!
                
                // Start of month
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(calendar.time)
                
                // End of month
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val endDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(calendar.time)

                val transactionResponse = apiService.getTransactions(
                    type = "expense",
                    startDate = startDate,
                    endDate = endDate,
                    limit = 1000 // Assuming not more than 1000 transactions per month for now
                )

                if (budgetResponse.isSuccessful && transactionResponse.isSuccessful) {
                    val budgets = budgetResponse.body() ?: emptyList()
                    val transactions = transactionResponse.body()?.items ?: emptyList()

                    val categorySpending = transactions
                        .groupBy { it.category }
                        .mapValues { (_, list) -> list.sumOf { it.amount } }
                    
                    val totalSpending = transactions.sumOf { it.amount }

                    _uiState.value = _uiState.value.copy(
                        budgets = budgets,
                        categorySpending = categorySpending,
                        totalSpending = totalSpending,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load data"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun setBudget(category: String?, amount: Double) {
        viewModelScope.launch {
            try {
                val response = apiService.setBudget(
                    BudgetRequest(
                        amount = amount,
                        category = category,
                        month = _uiState.value.currentMonth
                    )
                )
                if (response.isSuccessful) {
                    loadBudgetData() // Reload to refresh
                } else {
                    _uiState.value = _uiState.value.copy(error = "Failed to set budget")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun changeMonth(month: String) {
        loadBudgetData(month)
    }
}
