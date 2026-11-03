package de.tfc.kassenapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import de.tfc.kassenapp.model.ThemeMode
import de.tfc.kassenapp.ui.KassenAppScreen
import de.tfc.kassenapp.ui.theme.KassenappTheme
import de.tfc.kassenapp.viewmodel.KassenViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: KassenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bildschirm wach halten, solange die Kassenapp im Vordergrund ist
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()
        setContent {
            val useDarkTheme = when (viewModel.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            KassenappTheme(darkTheme = useDarkTheme) {
                val currentDensity = LocalDensity.current
                val scaledDensity = Density(
                    density = currentDensity.density * viewModel.uiScale,
                    fontScale = currentDensity.fontScale * viewModel.uiScale
                )

                CompositionLocalProvider(LocalDensity provides scaledDensity) {
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
}
