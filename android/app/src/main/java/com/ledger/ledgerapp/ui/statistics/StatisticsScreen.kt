package com.ledger.ledgerapp.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledger.ledgerapp.data.TokenManager
import com.ledger.ledgerapp.viewmodel.TransactionViewModel

import com.ledger.ledgerapp.data.CategoryData
import androidx.compose.material.icons.filled.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    tokenManager: TokenManager,
    onBack: () -> Unit,
    viewModel: TransactionViewModel = viewModel { TransactionViewModel(tokenManager) }
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 初始加载数据
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
    }

    val colors = listOf(
        Color(0xFFEF5350),
        Color(0xFFEC407A),
        Color(0xFFAB47BC),
        Color(0xFF7E57C2),
        Color(0xFF5C6BC0),
        Color(0xFF42A5F5),
        Color(0xFF29B6F6),
        Color(0xFF26C6DA),
        Color(0xFF26A69A),
        Color(0xFF66BB6A),
        Color(0xFF9CCC65),
        Color(0xFFD4E157),
        Color(0xFFFFEE58),
        Color(0xFFFFCA28),
        Color(0xFFFFA726),
        Color(0xFFFF7043)
    )

    val pieChartData = uiState.expenseStats.mapIndexed { index, stat ->
        PieChartData(
            value = stat.amount,
            color = colors[index % colors.size],
            label = stat.category
        )
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("分类支出", "收支趋势")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计分析") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTabIndex) {
                    0 -> CategoryExpenseView(uiState, pieChartData)
                    1 -> TrendView(uiState)
                }
            }
        }
    }
}

@Composable
fun TrendView(uiState: com.ledger.ledgerapp.viewmodel.TransactionUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "每日收支趋势",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        LineChart(
            data = uiState.dailyStats,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Summary for the period
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("本期概览", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("总收入", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "¥${String.format("%.2f", uiState.totalIncome)}",
                            color = Color(0xFF66BB6A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text("总支出", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "¥${String.format("%.2f", uiState.totalExpense)}",
                            color = Color(0xFFEF5350),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text("结余", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "¥${String.format("%.2f", uiState.balance)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryExpenseView(
    uiState: com.ledger.ledgerapp.viewmodel.TransactionUiState,
    pieChartData: List<PieChartData>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pieChartData.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无支出数据")
            }
        } else {
            // 饼图
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                PieChart(
                    data = pieChartData,
                    radiusOuter = 100.dp,
                    chartBarWidth = 30.dp,
                    animDuration = 1000
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "总支出",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥${String.format("%.2f", uiState.totalExpense)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 列表
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.expenseStats.zip(pieChartData)) { (stat, chartData) ->
                    val icon = CategoryData.expenseCategories.find { it.name == stat.category }?.icon 
                        ?: CategoryData.incomeCategories.find { it.name == stat.category }?.icon
                        ?: Icons.Default.Category

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 颜色指示器
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(chartData.color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // 图标
                        Icon(
                            imageVector = icon,
                            contentDescription = stat.category,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = stat.category,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "¥${String.format("%.2f", stat.amount)}",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${String.format("%.1f", stat.percentage * 100)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Divider(modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}
