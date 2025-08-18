package com.example.siginak.appNavigation

import com.example.siginak.R

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val iconResId: Int // Store resource ID instead of the actual icon
) {

    object Home : BottomBarScreen(
        route = "home",
        title = "Ana Sayfa",
        iconResId = R.drawable.main

    )

    object Yardim : BottomBarScreen(
        route = "yardim",
        title = "Yardim",
        iconResId = R.drawable.aid
    )

    object User : BottomBarScreen(
        route = "user",
        title = "Kullanici",
        iconResId = R.drawable.user
        )
    object Map : BottomBarScreen(
        route = "map",
        title = "Harita",
        iconResId = R.drawable.map2
    )
    object YeniYardim : BottomBarScreen(
        route = "yeniyardim",
        title = "YeniYardim",
        iconResId = R.drawable.map2
    )
    object YardimDetay : BottomBarScreen(
        route = "yardimDetay",
        title = "YardimDetay",
        iconResId = R.drawable.map2
    )
}
