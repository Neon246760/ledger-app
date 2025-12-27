package com.ledger.ledgerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ledger.ledgerapp.data.TokenManager
import com.ledger.ledgerapp.navigation.AppNavigation
import com.ledger.ledgerapp.ui.theme.LedgerAPPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 确保窗口立即显示，避免黑屏
        window.setBackgroundDrawableResource(android.R.color.white)
        // 确保窗口在setContent之前就可见
        window.decorView.setBackgroundColor(android.graphics.Color.WHITE)

        val tokenManager = TokenManager(this@MainActivity)

        setContent {
            LedgerAPPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(tokenManager = tokenManager)
                }
            }
        }
    }
}
