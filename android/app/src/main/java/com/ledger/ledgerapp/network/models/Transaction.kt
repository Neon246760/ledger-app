package com.ledger.ledgerapp.network.models

import com.google.gson.annotations.SerializedName

data class Transaction(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val type: String, // "income" or "expense"
    val amount: Double,
    val category: String,
    val description: String? = null,
    @SerializedName("image_path")
    val imagePath: String? = null,
    val date: String, // ISO 8601 format: "2024-01-15T10:30:00"
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

enum class TransactionType {
    @SerializedName("income")
    INCOME,
    
    @SerializedName("expense")
    EXPENSE;
    
    // 转换为后端API使用的字符串
    fun toApiString(): String = when (this) {
        INCOME -> "income"
        EXPENSE -> "expense"
    }
    
    companion object {
        // 从后端API字符串转换为枚举
        fun fromApiString(value: String): TransactionType = when (value.lowercase()) {
            "income" -> INCOME
            "expense" -> EXPENSE
            else -> throw IllegalArgumentException("Unknown transaction type: $value")
        }
    }
}

// Transaction 扩展函数，方便在 UI 层使用枚举类型
fun Transaction.getTypeEnum(): TransactionType = TransactionType.fromApiString(this.type)

data class TransactionRequest(
    val amount: Double,
    val type: String, // "income" or "expense"
    val category: String,
    val description: String? = null,
    @SerializedName("image_path")
    val imagePath: String? = null,
    val date: String
)

// 单个交易记录的响应（后端直接返回Transaction对象）
typealias TransactionResponse = Transaction

// 交易列表响应（与后端API一致）
data class TransactionListResponse(
    val total: Int,
    val items: List<Transaction>
)

// 交易统计摘要响应
data class TransactionSummaryResponse(
    @SerializedName("total_income")
    val totalIncome: Double,
    @SerializedName("total_expense")
    val totalExpense: Double,
    val balance: Double
)

