package com.phantomguard.security

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.phantomguard.security.data.remote.FirestoreSyncManager
import com.phantomguard.security.ui.navigation.PhantomNavGraph
import com.phantomguard.security.ui.theme.PhantomGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirestoreSyncManager.getInstance(this).startContinuousSync()
        setContent {
            PhantomGuardTheme {
                PhantomNavGraph()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        FirestoreSyncManager.getInstance(this).startContinuousSync()
    }
}
