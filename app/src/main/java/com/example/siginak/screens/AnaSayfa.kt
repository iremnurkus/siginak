package com.example.siginak.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.CalendarContract
import android.telephony.SmsManager
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siginak.communicationitems.Deprem
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.siginak.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.graphics.scale

import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

//git branch-pr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaSayfa(
    depremler: List<Deprem>,
    onProfileClick: () -> Unit,
    onItemClick: (Deprem) -> Unit,
    onStatusSelected: (String) -> Unit
) {
    val context = LocalContext.current

    // 1) Durum dialog’u
    var showStatusDialog by remember { mutableStateOf(false) }
    // 2) Başlangıç uyarısı dialog’u
    var showWarningDialog by remember { mutableStateOf(true) }

    // Uyarı dialog’u (uygulama açılır açılmaz)
    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            title = { Text("Uyarı") },
            text = {
                Text(
                    "Uygulamadan ayrılırken konum hizmetlerini kapatmanız tavsiye edilir, cihazınızın pilinin daha hızlı tükenmesine sebep olacaktır."
                )
            },
            confirmButton = {
                TextButton(onClick = { showWarningDialog = false }) {
                    Text("Tamam")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Ana içerik ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // 1) Harita
            DepremMapCard(depremler, context)

            Spacer(Modifier.height(8.dp))

            // 2) Son Depremler başlığı
            Text(
                "Son Depremler",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 5.dp)
            )

            // 3) Depremler listesi
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(depremler) { dp ->
                        // … Mevcut item içeriğiniz …
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFF222444)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(dp) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp)
                                    .height(50.dp)
                            ) {
                                MagnitudeBadge(dp.magnitude, modifier = Modifier.padding(3.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        dp.location,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                }
                                val parts = dp.dateTime.trim().split("\\s+".toRegex())
                                val dateStr = parts.getOrNull(0) ?: "----.--.--"
                                val timeStr = parts.getOrNull(1) ?: "--:--:--"
                                Column {
                                    Text(
                                        text = timeStr,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp,
                                        lineHeight = 16.sp
                                    )
                                    Text(
                                        text = dateStr,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(2.dp))
        }

        // --- Floating Action Button: Durum Bildir ---
        FloatingActionButton(
            onClick = { showStatusDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Info, // Ünlem ikonlu buton
                contentDescription = "Durum Bildir",
                tint = Color.Black,
            )
        }
    }

    // Durum seçimi için dialog
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Lüten bildirmek istediğin durumunu seç?")},
            confirmButton = {
                TextButton(onClick = {
                    onStatusSelected("İyiyim")
                    showStatusDialog = false
                }) {
                    Text("İyiyim")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onStatusSelected("Yardıma ihtiyacım var")
                    showStatusDialog = false
                }) {
                    Text("Yardıma ihtiyacım var")
                }
            }
        )
    }
}


/* 1. Büyüklüğe göre yeşil (#43A047) → kırmızı (#E53935) renk üret */
private fun magnitudeColor(mag: Double): Color {
    // 2.0 → tam yeşil, 8.0+ → tam kırmızı
    val t = ((mag - 2.0) / 6.0).coerceIn(0.0, 1.0).toFloat()
    return lerp(Color(0xFF43A047), Color(0xFFE53935), t)
}

/* 2. Dairesel rozet içinde büyüklük yazan Composable */
@Composable
fun MagnitudeBadge(
    magnitude: Double,
    modifier: Modifier = Modifier
) {
    val bg = remember(magnitude) { magnitudeColor(magnitude) }

    Surface(
        shape = CircleShape,
        color = bg,
        contentColor = Color.White,
        modifier = modifier.size(28.dp)      // dilediğiniz boyut
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text  = String.format("%.1f", magnitude),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TimeMagnitudeChartLast10(
    data: List<Deprem>,
    modifier: Modifier = Modifier
) {
    val display = data.take(10).reversed()
    if (display.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Grafik için veri yok", fontSize = 14.sp)
        }
        return
    }
    val maxMag = display.maxOf { it.magnitude }.takeIf { it > 0.0 } ?: 1.0

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val w = size.width
            val h = size.height
            val count = display.size
            val dx = if (count > 1) w / (count - 1) else w
            // Text paint
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 8.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
            }

            // Çizgi ve noktalar
            for (i in 0 until count - 1) {
                val x1 = i * dx
                val y1 = h - (display[i].magnitude / maxMag).toFloat() * h
                val x2 = (i + 1) * dx
                val y2 = h - (display[i + 1].magnitude / maxMag).toFloat() * h
                drawLine(
                    color = Color(0xFF670000),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 2.dp.toPx()
                )
            }
            display.forEachIndexed { i, dp ->
                val x = i * dx
                val y = h - (dp.magnitude / maxMag).toFloat() * h
                drawCircle(
                    color = Color(0xFFFF8C00),
                    center = Offset(x, y),
                    radius = 2.dp.toPx()
                )
                // Y ekseninde noktanın büyüklüğünü yaz
                drawContext.canvas.nativeCanvas.drawText(
                    dp.magnitude.toString(),
                    x,
                    y - 3.dp.toPx(),
                    paint
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Alt satırda sadece saat kısmı
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            display.forEach { dp ->
                Text(
                    text = dp.dateTime
                        .substringAfter(' ')
                        .substring(0, 5), // örn. "15:06:05"
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun DepremMapCard(depremler: List<Deprem>,context: Context) {
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(38.0, 35.0), 5f)
    }
    val orig = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.redmap
    )
// dp → px dönüşümü
    val sizeDp = 25.dp
    val sizePx = 25

// ölçekleme
    val scaled = orig.scale(sizePx, sizePx, false)
    val descriptor = BitmapDescriptorFactory.fromBitmap(scaled)
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            depremler
                .filter { it.magnitude >= 2.5 }
                .forEach { dp ->
                    Marker(
                        state = MarkerState(LatLng(dp.latitude, dp.longitude)),
                        title = dp.location,
                        snippet = "M${dp.magnitude}",
                        icon = descriptor     // artık istediğiniz boyutta
                    )
                }
        }
    }
}

class StatusRepository(
    private val firestore: FirebaseFirestore,
    private val smsManager: SmsManager
) {
    suspend fun reportStatus(
        uid: String,
        status: String,
        contacts: List<String>
    ) {
        // 1) Firestore’a kaydet
        val reportData = mapOf(
            "status"    to status,
            "timestamp" to FieldValue.serverTimestamp()
        )
        firestore.collection("kullanici")
            .document(uid)
            .collection("status_reports")
            .add(reportData)
            .await()

        // 2) SMS gönder
        for (phone in contacts) {
            smsManager.sendTextMessage(
                phone,
                null,
                "Durum güncellemesi: $status",
                null,
                null
            )
        }
    }
}
