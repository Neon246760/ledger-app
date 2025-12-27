package com.ledger.ledgerapp.network.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("repeat_password") val repeatPassword: String
)
