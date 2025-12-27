package com.ledger.ledgerapp.utils

object Constants {
    // 使用 10.0.2.2 访问本地主机（Android模拟器）
    // 真机测试时改为你的电脑IP地址，例如 "http://192.168.1.100:8000/"
    // 注意：Retrofit 的 baseUrl 必须以 "/" 结尾
    const val BASE_URL = "http://10.0.2.2:8000/"

    const val TOKEN_PREF_KEY = "access_token"
    const val TOKEN_TYPE_KEY = "token_type"
}