package com.example.siginak.appNavigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.siginak.screens.*
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import com.example.siginak.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    auth: FirebaseAuth,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    /* ---- alt menüde görünecek ekranlar ---- */
    val bottomBarScreens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Map,
        BottomBarScreen.Yardim,
        BottomBarScreen.User
    )

    /* ---- aktif rota ---- */
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    /* ---- o anda bottom bar çizilecek mi? ---- */
    val showBottomBar = destination
        ?.hierarchy                 // tüm üst-alt düğümleri dolaş
        ?.any { it.route in bottomBarScreens.map { s -> s.route } }
        ?: false

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomBarScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painterResource(screen.iconResId),
                                    contentDescription = screen.title,
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .size(15.dp),
                                    tint = Color.Black
                                )
                            },
                            label     = { Text(screen.title) },
                            selected  = destination?.route == screen.route,
                            onClick   = {
                                navController.navigate(screen.route) {
                                    popUpTo(BottomBarScreen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = if (auth.currentUser != null) "main" else "giris",
            modifier = modifier.padding(innerPadding)
        ) {
            /* ---- login / kayıt rotaları (bottom bar YOK) ---- */
            composable("giris") { GirisScreen(navController) }
            composable("kayit")  { Kayit(navController) }

            /* ---- uygulama içi rotalar (bottom bar VAR) ---- */
            navigation(
                route = "main",
                startDestination = BottomBarScreen.Home.route
            ) {
                composable(BottomBarScreen.Home.route)  { DepremScreen(navController) }
                composable(BottomBarScreen.Yardim.route) { YardimScreen(navController) }
                composable(BottomBarScreen.User.route)  { KullaniciScreen(navController) }
                composable(
                    route = "deprem_detail/{lat}/{lng}/{mag}/{depth}/{place}/{timestamp}",
                    arguments = listOf(
                        navArgument("lat")      { type = NavType.FloatType },
                        navArgument("lng")      { type = NavType.FloatType },
                        navArgument("mag")      { type = NavType.FloatType },
                        navArgument("depth")    { type = NavType.FloatType },
                        navArgument("place")    { type = NavType.StringType },
                        navArgument("timestamp"){ type = NavType.StringType },
                    )
                ) { backStack ->
                    // **ANAHTAR İSİMLERİNE DİKKAT ET**
                    val lat       = backStack.arguments!!.getFloat("lat").toDouble()
                    val lng       = backStack.arguments!!.getFloat("lng").toDouble()
                    val mag       = backStack.arguments!!.getFloat("mag").toDouble()
                    val depth     = backStack.arguments!!.getFloat("depth").toDouble()
                    val place     = backStack.arguments!!.getString("place")!!
                    val timestamp = backStack.arguments!!.getString("timestamp")!!

                    DepremDetay(
                        navController,
                        latitude  = lat,
                        longitude = lng,
                        magnitude = mag,
                        depth     = depth,
                        location  = place,
                        dateTime  = timestamp
                    )
                }

                composable(BottomBarScreen.Map.route) {
                    // string.xml'deki Google Maps API anahtarını oku

                    // direkt kendi içinde arama ve marker'ları gösteren Compose fonksiyonun
//                    val apiKey = stringResource(R.string.google_maps_key)
                    SearchableMap()
                }
                composable("yeniyardim") {
                    YeniYardimScreen(navController)
                }
                // ❶ Dinamik parametreyi buraya ekleyin:
                composable(
                    route = "yardimDetay/{talepId}",
                    arguments = listOf(
                        navArgument("talepId") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("talepId")
                    YardimDetay(navController, id = id.toString())
                }
            }
        }
    }
}

//@Composable
//fun AppNavHost(auth: FirebaseAuth) {
//    val navController = rememberNavController()
//
//    val screens = listOf(
//        BottomBarScreen.Home,
//        BottomBarScreen.Map,
//        BottomBarScreen.Yardim,
//        BottomBarScreen.User,
//    )
//
//    val backStack by navController.currentBackStackEntryAsState()
//    val currentRoute = backStack?.destination?.route
//    val showBottomBar = screens.any { it.route == currentRoute }
//
//    Scaffold(
//        bottomBar = {
//            if (showBottomBar) {
//                NavigationBar {
//                    screens.forEach { screen ->
//                        NavigationBarItem(
//                            icon = {
//                                Icon(
//                                    modifier = Modifier.size(15.dp),
//                                    painter = painterResource(id = screen.iconResId),
//                                    contentDescription = screen.title
//                                )
//                            },
//                            label = { Text(screen.title) },
//                            selected = currentRoute == screen.route,
//                            onClick = {
//                                val user = FirebaseAuth.getInstance().currentUser
//                                if (screen == BottomBarScreen.User && user?.isAnonymous == true) {
//                                    navController.navigate("login_required") {
//                                        popUpTo(navController.graph.findStartDestination().id) {
//                                            saveState = true
//                                        }
//                                        launchSingleTop = true
//                                        restoreState = true
//                                    }
//                                } else {
//                                    navController.navigate(screen.route) {
//                                        popUpTo(navController.graph.findStartDestination().id) {
//                                            saveState = true
//                                        }
//                                        launchSingleTop = true
//                                        restoreState = true
//                                    }
//                                }
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    ) { padding ->
//        NavHost(
//            navController = navController,
//            startDestination = if (auth.currentUser != null) "main" else "giris",
//            modifier = Modifier.padding(padding)
//        ) {
//            composable("giris") { GirisScreen(navController) }
//            composable("kayit") { Kayit(navController) }
//            composable("login_required") { LoginRequiredScreen(navController) }
//
//            navigation(
//                route = "main",
//                startDestination = BottomBarScreen.Home.route
//            ) {
//                composable(BottomBarScreen.Home.route) { SonDepremlerScreen() }
//                composable(BottomBarScreen.Yardim.route) { YardimScreen(navController) }
//                composable(BottomBarScreen.User.route) {
//                    val user = FirebaseAuth.getInstance().currentUser
//                    if (user?.isAnonymous == true) {
//                        LaunchedEffect(Unit) {
//                            navController.navigate("login_required") {
//                                popUpTo(BottomBarScreen.User.route) { inclusive = true }
//                            }
//                        }
//                    } else {
//                        KullaniciScreen(navController)
//                    }
//                }
//                composable(BottomBarScreen.Map.route) { MapScreen() }
//                composable(BottomBarScreen.YeniYardim.route) { YeniYardimScreen(navController) }
//            }
//        }
//    }
//}