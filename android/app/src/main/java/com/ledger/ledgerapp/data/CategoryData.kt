package com.ledger.ledgerapp.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.ledger.ledgerapp.network.models.TransactionType

data class Category(
    val name: String,
    val icon: ImageVector,
    val type: TransactionType
)

object CategoryData {
    val expenseCategories = listOf(
        Category("餐饮", Icons.Default.Restaurant, TransactionType.EXPENSE),
        Category("交通", Icons.Default.DirectionsCar, TransactionType.EXPENSE),
        Category("购物", Icons.Default.ShoppingCart, TransactionType.EXPENSE),
        Category("娱乐", Icons.Default.Movie, TransactionType.EXPENSE),
        Category("居住", Icons.Default.Home, TransactionType.EXPENSE),
        Category("医疗", Icons.Default.LocalHospital, TransactionType.EXPENSE),
        Category("教育", Icons.Default.School, TransactionType.EXPENSE),
        Category("其他", Icons.Default.Category, TransactionType.EXPENSE)
    )

    val incomeCategories = listOf(
        Category("工资", Icons.Default.AttachMoney, TransactionType.INCOME),
        Category("奖金", Icons.Default.CardGiftcard, TransactionType.INCOME),
        Category("理财", Icons.Default.TrendingUp, TransactionType.INCOME),
        Category("兼职", Icons.Default.Work, TransactionType.INCOME),
        Category("其他", Icons.Default.Category, TransactionType.INCOME)
    )

    fun getCategoriesByType(type: TransactionType): List<Category> {
        return if (type == TransactionType.EXPENSE) expenseCategories else incomeCategories
    }
}
