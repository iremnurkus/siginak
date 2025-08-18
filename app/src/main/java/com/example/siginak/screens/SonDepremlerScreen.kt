package com.example.siginak.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.siginak.communicationitems.Deprem
import com.example.siginak.communicationitems.DepremViewModel
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

@Composable
fun DepremScreen(
    navController: NavHostController,
    viewModel: DepremViewModel = viewModel()
) {
    // StateFlow → Compose State
    val depremler by viewModel.depremler.collectAsState()
    // Context for Toast
    val context = LocalContext.current

    // İlk açılışta veriyi çek
    LaunchedEffect(Unit) {
        viewModel.fetchDepremler()
    }

    AnaSayfa(
        depremler = depremler,
        onProfileClick = { /* profil işlemleri */ },
        onItemClick = { dp ->
            // location ve dateTime’ı URI uyumlu encode et
            val loc = Uri.encode(dp.location)
            val dt  = Uri.encode(dp.dateTime)
            navController.navigate(
                "deprem_detail/${dp.latitude}/${dp.longitude}/${dp.magnitude}/" +
                        "${dp.depth}/$loc/$dt"
            )
        },
        onStatusSelected = { status ->
            // Kullanıcı “İyiyim” veya “Yardıma ihtiyacım var” seçtiğinde tetiklenir
            Toast.makeText(context, "Seçtiğiniz durum: $status", Toast.LENGTH_SHORT).show()
            // TODO: Burada dilerseniz ViewModel üzerinden sunucuya bildirim yollayabilir,
            // başka bir ekrana yönlendirebilir veya loglayabilirsiniz.
        }
    )
}

@Composable
fun DepremItem(deprem: Deprem,navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal =5.dp, vertical = 5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ), onClick ={
            val encodedLocation = Uri.encode(deprem.location)
            val encodedDate    = Uri.encode(deprem.dateTime)
            navController.navigate(
                "deprem_detail/${deprem.latitude}/${deprem.longitude}/${deprem.depth}/" +
                        "${deprem.magnitude}/$encodedLocation/$encodedDate"
            )}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = deprem.location,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = deprem.dateTime,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Büyüklük: ${deprem.magnitude}",
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Derinlik: ${deprem.depth} km",
                )
            }
        }
    }
}
