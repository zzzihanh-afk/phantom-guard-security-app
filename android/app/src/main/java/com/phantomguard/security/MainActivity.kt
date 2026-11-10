package com.phantomguard.security

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.phantomguard.security.ui.navigation.PhantomNavGraph
import com.phantomguard.security.ui.theme.PhantomGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhantomGuardTheme {
                PhantomNavGraph()
            }
        }
    }
}
