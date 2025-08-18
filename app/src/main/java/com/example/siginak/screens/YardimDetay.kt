package com.example.siginak.screens

import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.siginak.communicationitems.yardimTalebi
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(MapsComposeExperimentalApi::class, ExperimentalMaterial3Api::class)
@Composable
fun YardimDetay(
    navController: NavController,
    id:  String
) {
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    var item by remember { mutableStateOf<yardimTalebi?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var latLng by remember { mutableStateOf<LatLng?>(null) }

    // Firebase’den veri çekme
    LaunchedEffect(id) {
        Firebase.firestore
            .collection("yardimTalebi")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                item = doc.toObject(yardimTalebi::class.java)
                isLoading = false
            }
            .addOnFailureListener {
                error = it.localizedMessage
                isLoading = false
            }
    }

    // Adresi koordinata çevirme
    LaunchedEffect(item?.address) {
        item?.address?.let { addr ->
            withContext(Dispatchers.IO) {
                geocoder.getFromLocationName(addr, 1)
            }?.firstOrNull()?.let { adr ->
                latLng = LatLng(adr.latitude, adr.longitude)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yardım Detayı") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = {
                            val db = Firebase.firestore
                            val srcRef = db.collection("yardimTalebi").document(id)
                            // 1) Mevcut veriyi oku
                            srcRef.get()
                                .addOnSuccessListener { snap ->
                                    val data = snap.data
                                    if (data != null) {
                                        // 2) Arşiv koleksiyonuna kaydet
                                        db.collection("yardimTalebiArsiv")
                                            .document(id)
                                            .set(data)
                                            .addOnSuccessListener {
                                                // 3) Eski dokümanı sil
                                                srcRef.delete()
                                                    .addOnSuccessListener {
                                                        navController.popBackStack()
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(context, "Silme hatası: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "Arşivleme hatası: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(context, "Veri bulunamadı", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Okuma hatası: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil")
                        }
                    }
                    )
                }
            ) {  padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(error!!)
                item != null -> Column(modifier = Modifier.padding(16.dp)) {
                    Text(item!!.title)
                    Spacer(Modifier.height(8.dp))
                    Text(item!!.detail,)
                    Spacer(Modifier.height(8.dp))
                    Text("Adres: ${item!!.address}")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            .format(item!!.timestamp?.toDate()),
                    )

                    Spacer(Modifier.height(16.dp))

                    // Harita
                    latLng?.let { location ->
                        val cameraState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(location, 15f)
                        }
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            cameraPositionState = cameraState
                        ) {
                            Marker(
                                state = MarkerState(position = location),
                                title = item!!.title
                            )
                        }
                    }
                }
            }
        }
    }
    }

