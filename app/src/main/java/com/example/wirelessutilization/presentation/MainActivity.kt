package com.example.wirelessutilization.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wirelessutilization.presentation.screen.BluetoothScreen
import com.example.wirelessutilization.presentation.screen.FingerprintSensorScreen
import com.example.wirelessutilization.presentation.screen.MainSelectionScreen
import com.example.wirelessutilization.presentation.screen.NFCScreen
import com.example.wirelessutilization.presentation.screen.WiFiScreen
import com.example.wirelessutilization.presentation.theme.WirelessUtilizationTheme
import kotlinx.serialization.Serializable

sealed class Destination {

    @Serializable
    data object Selection: Destination()

    @Serializable
    data object WiFi: Destination()

    @Serializable
    data object Bluetooth: Destination()

    @Serializable
    data object NFC: Destination()

    @Serializable
    data object Fingerprint: Destination()
}

val LocalNavController = staticCompositionLocalOf<NavController> { error("Not Provided") }

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            CompositionLocalProvider(LocalNavController provides navController) {
                WirelessUtilizationTheme {
                    NavHost(navController = navController, startDestination = Destination.Selection) {
                        composable<Destination.Selection> {
                            MainSelectionScreen(modifier = Modifier.fillMaxSize())
                        }
                        composable<Destination.WiFi> {
                            WiFiScreen(modifier = Modifier.fillMaxSize())
                        }
                        composable<Destination.Bluetooth> {
                            BluetoothScreen(modifier = Modifier.fillMaxSize())
                        }
                        composable<Destination.NFC> {
                            NFCScreen(modifier = Modifier.fillMaxSize())
                        }
                        composable<Destination.Fingerprint> {
                            FingerprintSensorScreen(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
