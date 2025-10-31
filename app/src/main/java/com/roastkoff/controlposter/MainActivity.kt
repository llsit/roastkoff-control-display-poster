package com.roastkoff.controlposter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.roastkoff.controlposter.ui.theme.ControlPosterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControlPosterTheme {
                ControlPosterApp()
            }
        }
    }
}

@Composable
private fun ControlPosterApp() {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        ControlPosterNavigation(paddingValues = paddingValues)
    }
}
