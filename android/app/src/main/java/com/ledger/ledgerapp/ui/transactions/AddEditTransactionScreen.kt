package com.ledger.ledgerapp.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.ledger.ledgerapp.data.CategoryData
import com.ledger.ledgerapp.data.TokenManager
import com.ledger.ledgerapp.network.models.Transaction
import com.ledger.ledgerapp.network.models.TransactionType
import com.ledger.ledgerapp.viewmodel.TransactionViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    tokenManager: TokenManager,
    transactionId: Int? = null,
    transaction: Transaction? = null,
    onBack: () -> Unit,
    viewModel: TransactionViewModel = viewModel { TransactionViewModel(tokenManager) }
) {
    val uiState by viewModel.uiState.collectAsState()
    var loadedTransaction by remember { mutableStateOf<Transaction?>(transaction) }
    val isEdit = transactionId != null || transaction != null
    
    // 如果提供了 transactionId 但没有 transaction，从 ViewModel 加载
    LaunchedEffect(transactionId) {
        if (transactionId != null && loadedTransaction == null) {
            loadedTransaction = viewModel.getTransactionById(transactionId)
        }
    }
    
    // 表单状态
    var amountText by remember(loadedTransaction) { mutableStateOf(loadedTransaction?.amount?.toString() ?: "") }
    var selectedType by remember(loadedTransaction) { 
        mutableStateOf<TransactionType>(
            loadedTransaction?.type?.let { TransactionType.fromApiString(it) } ?: TransactionType.EXPENSE
        )
    }
    var categoryText by remember(loadedTransaction) { mutableStateOf(loadedTransaction?.category ?: "") }
    
    // 当类型改变时，如果当前分类不在新类型的列表中，则清空分类（除非是初始加载）
    LaunchedEffect(selectedType) {
        val categories = CategoryData.getCategoriesByType(selectedType)
        if (categoryText.isNotEmpty() && categories.none { it.name == categoryText }) {
            // 只有当不是初始加载时才清空，这里简单处理，如果是用户切换类型，通常需要重选分类
            // 但为了避免编辑时刚进来就被清空，我们需要区分是用户点击还是初始加载
            // 由于 selectedType 是 remember 的，初始加载后不会变，除非用户点击
            // 所以这里可以安全地清空，但要注意编辑模式下初始 categoryText 可能不匹配默认 type（如果数据有问题）
            // 实际逻辑：如果当前 categoryText 不在新的 categories 列表中，且不是空，则清空
             if (loadedTransaction == null || loadedTransaction!!.type != selectedType.toApiString()) {
                 categoryText = ""
             }
        }
    }

    var descriptionText by remember(loadedTransaction) { mutableStateOf(loadedTransaction?.description ?: "") }
    var dateText by remember(loadedTransaction) { mutableStateOf(
        loadedTransaction?.date?.let {
            try {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it)
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            }
        } ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    ) }
    var timeText by remember(loadedTransaction) { mutableStateOf(
        loadedTransaction?.date?.let {
            try {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it)
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            }
        } ?: SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    ) }
    
    var imagePath by remember(loadedTransaction) { mutableStateOf(loadedTransaction?.imagePath) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    var showError by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "编辑账目" else "添加账目") },
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
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 类型选择
            Text(
                text = "类型",
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { selectedType = TransactionType.INCOME },
                    label = { Text("收入") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFF4CAF50)
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { selectedType = TransactionType.EXPENSE },
                    label = { Text("支出") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF44336).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFFF44336)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 金额
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("金额") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("¥") },
                supportingText = {
                    if (amountText.isNotEmpty() && amountText.toDoubleOrNull() == null) {
                        Text("请输入有效的数字", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            
            // 分类选择器
            CategorySelector(
                categories = CategoryData.getCategoriesByType(selectedType),
                selectedCategory = categoryText,
                onCategorySelected = { categoryText = it }
            )
            
            // 描述
            OutlinedTextField(
                value = descriptionText,
                onValueChange = { descriptionText = it },
                label = { Text("描述（可选）") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                placeholder = { Text("添加备注信息") }
            )
            
            // 日期
            OutlinedTextField(
                value = dateText,
                onValueChange = { dateText = it },
                label = { Text("日期") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("格式：yyyy-MM-dd") }
            )
            
            // 时间
            OutlinedTextField(
                value = timeText,
                onValueChange = { timeText = it },
                label = { Text("时间") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("格式：HH:mm") }
            )
            
            // 错误提示
            showError?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // 图片上传
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("凭证/小票", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(bottom = 8.dp)
                        )
                        Button(onClick = { selectedImageUri = null }) {
                            Text("移除图片")
                        }
                    } else if (imagePath != null) {
                        // Show existing image from server
                        val baseUrl = "http://10.0.2.2:8000" 
                        AsyncImage(
                            model = "$baseUrl$imagePath",
                            contentDescription = "Existing Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(bottom = 8.dp)
                        )
                        Row {
                            Button(onClick = { launcher.launch("image/*") }) {
                                Text("更换图片")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { imagePath = null }) {
                                Text("移除图片")
                            }
                        }
                    } else {
                        Button(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("上传图片")
                        }
                    }
                }
            }
            
            // 确认和取消按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 取消按钮
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = !uiState.isLoading
                ) {
                    Text("取消")
                }
                
                // 确认按钮
                Button(
                    onClick = {
                        // 验证输入
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            showError = "请输入有效的金额"
                            return@Button
                        }
                        
                        if (categoryText.isBlank()) {
                            showError = "请输入分类"
                            return@Button
                        }
                        
                        // 格式化日期时间
                        val dateTime = try {
                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateText)
                            val timeParts = timeText.split(":")
                            val hour = timeParts[0].toInt()
                            val minute = timeParts[1].toInt()
                            val calendar = Calendar.getInstance().apply {
                                time = date
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND, 0)
                            }
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(calendar.time)
                        } catch (e: Exception) {
                            showError = "日期或时间格式错误"
                            return@Button
                        }
                        
                        showError = null
                        
                        val saveAction = { uploadedImagePath: String? ->
                            val finalImagePath = uploadedImagePath ?: imagePath
                            
                            val targetTransaction = loadedTransaction ?: transaction
                            if (isEdit && (targetTransaction != null || transactionId != null)) {
                                val id = targetTransaction?.id ?: transactionId!!
                                viewModel.updateTransaction(
                                    id = id,
                                    amount = amount,
                                    type = selectedType,
                                    category = categoryText,
                                    description = descriptionText.ifBlank { null },
                                    imagePath = finalImagePath,
                                    date = dateTime,
                                    onSuccess = onBack,
                                    onError = { showError = it }
                                )
                            } else {
                                viewModel.createTransaction(
                                    amount = amount,
                                    type = selectedType,
                                    category = categoryText,
                                    description = descriptionText.ifBlank { null },
                                    imagePath = finalImagePath,
                                    date = dateTime,
                                    onSuccess = onBack,
                                    onError = { showError = it }
                                )
                            }
                        }

                        if (selectedImageUri != null) {
                            try {
                                val inputStream = context.contentResolver.openInputStream(selectedImageUri!!)
                                val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                                val outputStream = FileOutputStream(file)
                                inputStream?.copyTo(outputStream)
                                inputStream?.close()
                                outputStream.close()
                                
                                viewModel.uploadImage(
                                    file = file,
                                    onSuccess = { url -> saveAction(url) },
                                    onError = { showError = it }
                                )
                            } catch (e: Exception) {
                                showError = "图片处理失败: ${e.message}"
                            }
                        } else {
                            saveAction(null)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (isEdit) "确认保存" else "确认添加")
                    }
                }
            }
            
            // 底部间距
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

