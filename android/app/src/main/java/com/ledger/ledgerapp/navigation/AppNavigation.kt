package com.ledger.ledgerapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ledger.ledgerapp.data.TokenManager
import com.ledger.ledgerapp.network.models.Transaction
import com.ledger.ledgerapp.ui.home.HomeScreen
import com.ledger.ledgerapp.ui.login.LoginScreen
import com.ledger.ledgerapp.ui.register.RegisterScreen
import com.ledger.ledgerapp.ui.profile.ProfileScreen
import com.ledger.ledgerapp.ui.statistics.StatisticsScreen
import com.ledger.ledgerapp.ui.budget.BudgetScreen
import com.ledger.ledgerapp.ui.transactions.AddEditTransactionScreen
import com.ledger.ledgerapp.ui.transactions.TransactionListScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Transactions : Screen("transactions")
    object Statistics : Screen("statistics")
    object Budget : Screen("budget")
    object AddTransaction : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: Int) = "edit_transaction/$transactionId"
    }
}

@Composable
fun AppNavigation(
    tokenManager: TokenManager,
    navController: NavHostController = rememberNavController()
) {
    // 完全避免在初始渲染时读取token，使用状态变量
    var hasCheckedToken by remember { mutableStateOf(false) }
    var initialToken by remember { mutableStateOf<String?>(null) }
    var currentTokenState by remember { mutableStateOf<String?>(null) }
    
    // 延迟读取初始token，完全在后台进行，不阻塞UI
    LaunchedEffect(Unit) {
        // 延迟读取，确保UI先完全渲染
        delay(500)
        try {
            initialToken = tokenManager.getToken()
            currentTokenState = initialToken
        } catch (e: Exception) {
            // 忽略错误
        } finally {
            hasCheckedToken = true
        }
    }
    
    // 延迟开始监听token变化（用于登录后的导航）
    LaunchedEffect(hasCheckedToken) {
        if (!hasCheckedToken) return@LaunchedEffect
        // 开始收集token Flow，但只在UI渲染后
        delay(100)
        tokenManager.token.collect { token ->
            currentTokenState = token
        }
    }

    // 始终从登录页开始 - 立即渲染，不延迟
    val startDestination = Screen.Login.route

    // 根据初始token或token变化进行导航
    LaunchedEffect(hasCheckedToken, initialToken, currentTokenState) {
        if (!hasCheckedToken) return@LaunchedEffect
        
        val currentToken = currentTokenState ?: initialToken
        // 等待NavHost完全初始化
        delay(200)
        
        if (currentToken != null && currentToken.isNotBlank()) {
            // 如果有token，导航到主页
            try {
                // 确保NavHost已经准备好
                if (navController.currentBackStackEntry != null) {
                    val currentRoute = navController.currentDestination?.route
                    if (currentRoute != Screen.Home.route) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            } catch (e: Exception) {
                // 忽略导航错误，可能是NavHost还未完全初始化
                // 如果导航失败，用户仍然可以看到登录页面
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // 注册成功后返回登录页
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                tokenManager = tokenManager,
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions.route)
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                tokenManager = tokenManager,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Budget.route) {
            BudgetScreen(
                tokenManager = tokenManager,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToBudget = {
                    navController.navigate(Screen.Budget.route)
                }
            )
        }
        
        composable(Screen.Transactions.route) {
            TransactionListScreen(
                tokenManager = tokenManager,
                onAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onEditTransaction = { transaction ->
                    transaction.id?.let { id ->
                        navController.navigate(Screen.EditTransaction.createRoute(id))
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.AddTransaction.route) {
            AddEditTransactionScreen(
                tokenManager = tokenManager,
                transaction = null,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getInt("transactionId")
            AddEditTransactionScreen(
                tokenManager = tokenManager,
                transactionId = transactionId,
                transaction = null,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}