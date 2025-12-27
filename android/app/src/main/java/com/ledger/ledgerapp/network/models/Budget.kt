package com.ledger.ledgerapp.network.models

import com.google.gson.annotations.SerializedName

data class Budget(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val amount: Double,
    val category: String?,
    val month: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class BudgetRequest(
    val amount: Double,
    val category: String?,
    val month: String
)
