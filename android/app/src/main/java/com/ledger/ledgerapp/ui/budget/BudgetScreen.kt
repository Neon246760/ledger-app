package com.ledger.ledgerapp.ui.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledger.ledgerapp.data.CategoryData
import com.ledger.ledgerapp.data.TokenManager
import com.ledger.ledgerapp.viewmodel.BudgetViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    tokenManager: TokenManager,
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = viewModel { BudgetViewModel(tokenManager) }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var selectedCategoryForBudget by remember { mutableStateOf<String?>(null) }
    var currentBudgetAmount by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadBudgetData()
    }

    if (showSetBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showSetBudgetDialog = false },
            title = { Text(text = if (selectedCategoryForBudget == null) "设置月度总预算" else "设置 ${selectedCategoryForBudget} 预算") },
            text = {
                OutlinedTextField(
                    value = currentBudgetAmount,
                    onValueChange = { currentBudgetAmount = it },
                    label = { Text("金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = currentBudgetAmount.toDoubleOrNull()
                        if (amount != null) {
                            viewModel.setBudget(selectedCategoryForBudget, amount)
                        }
                        showSetBudgetDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetBudgetDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预算管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Month Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    cal.time = sdf.parse(uiState.currentMonth)!!
                    cal.add(Calendar.MONTH, -1)
                    viewModel.changeMonth(sdf.format(cal.time))
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "上个月")
                }
                
                Text(
                    text = uiState.currentMonth,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    cal.time = sdf.parse(uiState.currentMonth)!!
                    cal.add(Calendar.MONTH, 1)
                    viewModel.changeMonth(sdf.format(cal.time))
                }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "下个月")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Total Budget Summary
            val totalBudget = uiState.budgets.find { it.category == null }
            val totalBudgetAmount = totalBudget?.amount ?: 0.0
            val totalProgress = if (totalBudgetAmount > 0) (uiState.totalSpending / totalBudgetAmount).toFloat() else 0f
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("月度总预算", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = {
                            selectedCategoryForBudget = null
                            currentBudgetAmount = totalBudgetAmount.toString()
                            showSetBudgetDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = totalProgress.coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = if (totalProgress > 1f) Color.Red else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("支出: ¥${String.format("%.2f", uiState.totalSpending)}")
                        Text("预算: ¥${String.format("%.2f", totalBudgetAmount)}")
                    }
                    if (totalProgress > 1f) {
                        Text(
                            "超支: ¥${String.format("%.2f", uiState.totalSpending - totalBudgetAmount)}",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            "剩余: ¥${String.format("%.2f", totalBudgetAmount - uiState.totalSpending)}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("分类预算", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Category List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CategoryData.expenseCategories) { category ->
                    val budget = uiState.budgets.find { it.category == category.name }
                    val budgetAmount = budget?.amount ?: 0.0
                    val spending = uiState.categorySpending[category.name] ?: 0.0
                    val progress = if (budgetAmount > 0) (spending / budgetAmount).toFloat() else 0f

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedCategoryForBudget = category.name
                            currentBudgetAmount = budgetAmount.toString()
                            showSetBudgetDialog = true
                        },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category.name, fontWeight = FontWeight.Bold)
                                if (budgetAmount > 0) {
                                    Text("${(progress * 100).toInt()}%")
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            if (budgetAmount > 0) {
                                LinearProgressIndicator(
                                    progress = progress.coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = if (progress > 1f) Color.Red else MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("¥${String.format("%.2f", spending)}", style = MaterialTheme.typography.bodySmall)
                                    Text("¥${String.format("%.2f", budgetAmount)}", style = MaterialTheme.typography.bodySmall)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("支出: ¥${String.format("%.2f", spending)}", style = MaterialTheme.typography.bodySmall)
                                    Text("未设置预算", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
