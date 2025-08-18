package com.example.siginak.appNavigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.internal.isLiveLiteralsEnabled
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.siginak.screens.DepremScreen
import com.example.siginak.screens.GirisScreen
import com.example.siginak.screens.Kayit
import com.example.siginak.screens.YardimScreen
import com.example.siginak.screens.KullaniciScreen
import com.example.siginak.screens.YeniYardimScreen
//
//@Composable
//fun BottomNavGraph(navController: NavHostController) {
//    NavHost(
//        navController = navController,
//        startDestination = BottomBarScreen.Home.route
//    ) {
//        composable(route = BottomBarScreen.Home.route) {
//            DepremScreen(navController)
//        }
//        composable(route = BottomBarScreen.Yardim.route) {
//            YardimScreen(navController)
//        }
//        composable(route = BottomBarScreen.User.route) {
//            KullaniciScreen(navController)
//        }
//        // navigation/AppNavHost.kt içindeki Map rotası
//        composable(BottomBarScreen.Map.route) {
//            val vm: HospitalMapViewModel = viewModel()
//            val context = LocalContext.current
//            LaunchedEffect(Unit) {
//                // Konum izni kontrolü
//                if (ContextCompat.checkSelfPermission(
//                        context,
//                        Manifest.permission.ACCESS_FINE_LOCATION
//                    ) == PackageManager.PERMISSION_GRANTED
//                ) {
//                    vm.loadUserLocation()
//                    vm.fetchNearbyHospitals()
//                }
//            }
//            HospitalMapScreen(vm)
//        }
//
//        composable(route = BottomBarScreen.YeniYardim.route) {
//            YeniYardimScreen(navController)
//        }
//
//    }
//}
