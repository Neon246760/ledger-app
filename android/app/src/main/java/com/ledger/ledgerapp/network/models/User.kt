package com.ledger.ledgerapp.network.models

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String,
    @SerializedName("avatar_url")
    val avatarUrl: String?
)

data class UserUpdate(
    @SerializedName("avatar_url")
    val avatarUrl: String?
)
