package hu.toliver.vinet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import hu.toliver.vinet.ui.LauncherScreen
import hu.toliver.vinet.ui.ViNetTheme

@AndroidEntryPoint
class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ViNetTheme {
                LauncherScreen()
            }
        }
    }
}
