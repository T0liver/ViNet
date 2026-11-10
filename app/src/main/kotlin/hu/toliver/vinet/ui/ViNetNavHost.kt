package hu.toliver.vinet.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hu.toliver.vinet.ui.scan.ScanScreen

object ViNetRoutes {
    const val SCAN = "scan"
}

@Composable
fun ViNetNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ViNetRoutes.SCAN,
        modifier = modifier,
    ) {
        composable(ViNetRoutes.SCAN) {
            ScanScreen()
        }
    }
}
