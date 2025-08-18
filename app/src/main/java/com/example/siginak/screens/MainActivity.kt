package com.example.siginak.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentFilter
import android.location.LocationManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.siginak.appNavigation.AppNavHost
import com.example.siginak.appNavigation.BottomBarScreen
import com.example.siginak.ui.theme.SiginakTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.siginak.ProviderChangeReceiver
import com.example.siginak.communicationitems.KandilliWorker
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

import com.google.android.gms.maps.MapsInitializer
import com.google.firebase.auth.ktx.auth


class MainActivity : ComponentActivity() {

    private val providerChangeReceiver = ProviderChangeReceiver()
    private var isReceiverRegistered = false

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Konum izni gerekli!", Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("SuspiciousIndentation")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Konum iznini sor
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        // 2) Google Maps initialize
        MapsInitializer.initialize(this)

        // 3) Kandilli Worker’ı schedule et
        scheduleKandilliPolling()

        // 4) Firebase Auth örneği
        val auth = Firebase.auth

        // 5) Compose içeriği
        setContent {
            SiginakTheme {
                val navController = rememberNavController()

                // Bottom Bar için ekranlar
                val screens = listOf(
                    BottomBarScreen.Home,
                    BottomBarScreen.Map,
                    BottomBarScreen.Yardim,
                    BottomBarScreen.User
                )

                // Şu anki rota
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (screens.any { it.route == currentRoute }) {
                            BottomNavigationBar(
                                screens = screens,
                                currentRoute = currentRoute,
                                onItemSelected = { screen ->
                                    navController.navigate(screen.route) {
                                        popUpTo(BottomBarScreen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { padding ->
                    AppNavHost(
                        auth = auth,
                        navController = navController,
                        modifier = Modifier.padding(1.dp)
                    )
                }
            }
        }
    }

    private fun scheduleKandilliPolling() {
        val workReq = PeriodicWorkRequestBuilder<KandilliWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "KandilliPolling",
                ExistingPeriodicWorkPolicy.KEEP,
                workReq
            )
    }

    override fun onResume() {
        super.onResume()
        if (!isReceiverRegistered) {
            registerReceiver(
                providerChangeReceiver,
                IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
            )
            isReceiverRegistered = true
        }
    }
    override fun onPause() {
        super.onPause()
        if (isReceiverRegistered) {
            unregisterReceiver(providerChangeReceiver)
            isReceiverRegistered = false
        }
    }

}

// Alt çubuğu ayrı Composable olarak çıkardım
@Composable
fun BottomNavigationBar(
    screens: List<BottomBarScreen>,
    currentRoute: String?,
    onItemSelected: (BottomBarScreen) -> Unit
) {
    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(id = screen.iconResId),
                        contentDescription = screen.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = { onItemSelected(screen) }
            )
        }
    }
}

//// 3. MainActivity.kt içinde:
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        val auth= Firebase.auth
//        super.onCreate(savedInstanceState)
//        setContent {
//            SiginakTheme {
//                val navController = rememberNavController()
//
//                // Scaffold ve kendi BottomAppBar’ınız burada
//                Scaffold(
//                    bottomBar = { AppNavHost(auth, navController, modifier = Modifier) }
//                ) { padding ->
//                    Box(Modifier.padding(padding)) {
//                        DepremScreen()
//                    }
//                }
//            }
//        }
//    }
//}

