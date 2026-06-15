package com.dotfield.dotcal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dotfield.dotcal.ui.DotCalApp
import com.dotfield.dotcal.ui.DotCalViewModel
import com.dotfield.dotcal.ui.theme.DotCalTheme

class MainActivity : ComponentActivity() {
    private val viewModel: DotCalViewModel by viewModels {
        val app = application as DotCalApplication
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DotCalViewModel(app.repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DotCalTheme {
                DotCalApp(viewModel = viewModel)
            }
        }
    }
}
