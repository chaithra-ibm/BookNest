package com.example.booknest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.booknest.ui.BookStoreApp
import com.example.booknest.ui.theme.BookNestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookNestTheme {
                BookStoreApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookStoreAppPreview() {
    BookNestTheme {
        BookStoreApp()
    }
}