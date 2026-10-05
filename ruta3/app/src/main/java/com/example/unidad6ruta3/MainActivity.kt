package com.example.unidad6ruta3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unidad6ruta3.ui.FlightSearchScreen
import com.example.unidad6ruta3.ui.FlightSearchViewModel
import com.example.unidad6ruta3.ui.theme.Unidad6Ruta3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Unidad6Ruta3Theme {
                Scaffold { innerPadding ->
                    FlightSearchScreen(
                        viewModel = viewModel(),
                        modifier = Modifier
                            .padding(innerPadding)
                            .imePadding()
                    )
                }
            }
        }
    }
}
