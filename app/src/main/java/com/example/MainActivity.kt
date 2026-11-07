package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.SnakeGameScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SnakeViewModel

class MainActivity : ComponentActivity() {

    private val snakeViewModel: SnakeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SnakeGameScreen(viewModel = snakeViewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Automatically pause game when app loses focus or screen turns off
        snakeViewModel.pauseGame()
    }
}
