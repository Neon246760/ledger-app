package com.ledger.ledgerapp.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.ledger.ledgerapp.network.RetrofitClient
import com.ledger.ledgerapp.data.CategoryData
import com.ledger.ledgerapp.data.TokenManager
import com.ledger.ledgerapp.network.models.Transaction
import com.ledger.ledgerapp.network.models.TransactionType
import com.ledger.ledgerapp.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    tokenManager: TokenManager,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    viewModel: TransactionViewModel = viewModel { TransactionViewModel(tokenManager) }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    
    // 初始加载数据
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
    }
    
    if (showFilterSheet) {
        FilterBottomSheet(
            currentType = uiState.filterType,
            currentStartDate = uiState.filterStartDate,
            currentEndDate = uiState.filterEndDate,
            currentCategory = uiState.filterCategory,
            onDismiss = { showFilterSheet = false },
            onApply = { type, start, end, category ->
                viewModel.loadTransactions(
                    type = type,
                    startDate = start,
                    endDate = end,
                    category = category
                )
                showFilterSheet = false
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账目记录") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "筛选")
                    }
                    IconButton(onClick = { viewModel.loadTransactions() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                    onNavigateToHome?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.Default.Home, contentDescription = "返回首页")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "添加账目")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 统计卡片
            StatisticsCard(
                totalIncome = uiState.totalIncome,
                totalExpense = uiState.totalExpense,
                balance = uiState.balance
            )
            
            // 账目列表
            if (uiState.isLoading && uiState.transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无账目记录", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onEdit = { onEditTransaction(transaction) },
                            onDelete = {
                                viewModel.deleteTransaction(
                                    id = transaction.id!!,
                                    onSuccess = { },
                                    onError = { }
                                )
                            }
                        )
                    }
                }
            }
            
            // 错误提示
            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    // 可以显示 Snackbar
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentType: TransactionType?,
    currentStartDate: String?,
    currentEndDate: String?,
    currentCategory: String?,
    onDismiss: () -> Unit,
    onApply: (TransactionType?, String?, String?, String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedType by remember { mutableStateOf(currentType) }
    var startDate by remember { mutableStateOf(currentStartDate) }
    var endDate by remember { mutableStateOf(currentEndDate) }
    var selectedCategory by remember { mutableStateOf(currentCategory) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    fun showDatePicker(currentDate: String?, onDateSelected: (String) -> Unit) {
        val parts = currentDate?.split("-")
        val year = parts?.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
        val month = (parts?.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
        val day = parts?.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, y, m, d ->
                val formattedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                onDateSelected(formattedDate)
            },
            year, month, day
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "筛选条件",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // 类型筛选
            Text(text = "类型", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    label = { Text("全部") }
                )
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { selectedType = TransactionType.EXPENSE },
                    label = { Text("支出") }
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { selectedType = TransactionType.INCOME },
                    label = { Text("收入") }
                )
            }

            // 日期筛选
            Text(text = "日期范围", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showDatePicker(startDate) { startDate = it } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(startDate ?: "开始日期")
                }
                Text("-")
                OutlinedButton(
                    onClick = { showDatePicker(endDate) { endDate = it } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(endDate ?: "结束日期")
                }
            }

            // 分类筛选
            Text(text = "分类", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = remember(selectedType) {
                    if (selectedType == TransactionType.INCOME) {
                        CategoryData.incomeCategories
                    } else if (selectedType == TransactionType.EXPENSE) {
                        CategoryData.expenseCategories
                    } else {
                        CategoryData.expenseCategories + CategoryData.incomeCategories
                    }
                }

                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("全部分类") }
                )

                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category.name,
                        onClick = {
                            selectedCategory = if (selectedCategory == category.name) null else category.name
                        },
                        label = { Text(category.name) }
                    )
                }
            }

            // 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedType = null
                        startDate = null
                        endDate = null
                        selectedCategory = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重置")
                }
                Button(
                    onClick = { onApply(selectedType, startDate, endDate, selectedCategory) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("应用")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatisticsCard(
    totalIncome: Double,
    totalExpense: Double,
    balance: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "统计",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem("收入", totalIncome, Color(0xFF4CAF50))
                StatisticItem("支出", totalExpense, Color(0xFFF44336))
                StatisticItem("余额", balance, Color(0xFF2196F3))
            }
        }
    }
}

@Composable
fun StatisticItem(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "¥${String.format("%.2f", value)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 类型标签
                    Surface(
                        color = if (transaction.type == "income") 
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else 
                            Color(0xFFF44336).copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (transaction.type == "income") "收入" else "支出",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (transaction.type == "income") 
                                Color(0xFF4CAF50)
                            else 
                                Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 分类
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 描述
                transaction.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 图片标识
                if (!transaction.imagePath.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 构建完整的图片URL
                    val imageUrl = if (transaction.imagePath.startsWith("http")) {
                        transaction.imagePath
                    } else {
                        val path = if (transaction.imagePath.startsWith("/")) transaction.imagePath.substring(1) else transaction.imagePath
                        "${RetrofitClient.BASE_URL}$path"
                    }

                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "凭证图片",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 4.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 日期时间
                Text(
                    text = try {
                        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(transaction.date)
                        "${dateFormat.format(date)} ${timeFormat.format(date)}"
                    } catch (e: Exception) {
                        transaction.date
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // 金额
                Text(
                    text = "${if (transaction.type == "income") "+" else "-"}¥${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == "income") 
                        Color(0xFF4CAF50)
                    else 
                        Color(0xFFF44336)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 操作按钮
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

