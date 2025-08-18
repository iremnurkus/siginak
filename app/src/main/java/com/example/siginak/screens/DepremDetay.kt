package com.example.siginak.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepremDetay(
    navController: NavController,
    latitude: Double,
    longitude: Double,
    depth: Double,
    magnitude: Double?,
    location: String,
    dateTime: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deprem Detayı") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
        // bottomBar parametresini buraya eklemeyin; zaten global / parent seviyede var.
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(5.dp)
                ) {
                    Spacer(Modifier.width(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text(location, fontSize = 16.sp, color = Color.Black)
                        Text(magnitude.toString(),fontSize = 16.sp, color = Color.Black)
                    }

                    val parts = dateTime.trim().split("\\s+".toRegex())
                    val dateStr = parts.getOrNull(0) ?: "----.--.--"
                    val timeStr = parts.getOrNull(1) ?: "--:--:--"

                    Column {
                        Text(
                            text = "$timeStr",
                            fontSize = 10.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF280C06)
                        )
                        Text(
                            text = "$dateStr",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF280C06)
                        )
                    }
//                        Text(dp.dateTime, fontSize = 14.sp, color = Color(0xFF3F1408))
                }
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(latitude, longitude), 10f
                    )
                }

                Spacer(Modifier.padding(10.dp))

                GoogleMap(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(LatLng(latitude, longitude)),
                        title = "Deprem Noktası"
                    )
                }
            }
        }
    }
}