package com.ledger.ledgerapp.network.models

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("detail") val detail: String
)
