package de.tfc.kassenapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import de.tfc.kassenapp.ui.KassenAppScreen
import de.tfc.kassenapp.ui.theme.KassenappTheme
import de.tfc.kassenapp.viewmodel.KassenViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: KassenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KassenappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KassenAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
