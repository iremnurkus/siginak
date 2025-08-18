package com.example.siginak.screens


import android.util.Log
import android.widget.Toast
import com.example.siginak.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.siginak.communicationitems.yardimTalebi
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import java.io.Serializable
import java.security.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YeniYardimScreen(navController: NavController) {
    val db = Firebase.firestore

    var id by remember{mutableStateOf("")}
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(text = "Yeni Yardım", modifier = Modifier)},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Yardım Talebi İsmini Giriniz") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    placeholder = { Text("Detay") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    placeholder = { Text("Adres") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                )
            }
            val context=LocalContext.current
            Button(onClick = {
                // Firestore örneği
                val db = FirebaseFirestore.getInstance()
                // Sayaç dokümanı
                val counterRef = db.collection("counters").document("yardimTalebi")
                val auth = Firebase.auth
                val currentUser = auth.currentUser

                db.runTransaction { tx ->
                    // 1) Sayaç değerini oku
                    val counterSnap = tx.get(counterRef)
                    val nextId = if (counterSnap.exists()) {
                        (counterSnap.getLong("current") ?: 0L) + 1L
                    } else {
                        1L
                    }

                    // 2) Sayaç dokümanını güncelle
                    tx.set(counterRef, mapOf("current" to nextId), SetOptions.merge())

                    // 3) Yeni yardimTalebi belgesini oluştur
                    val newDocRef = db.collection("yardimTalebi").document(nextId.toString())
                    val data = mapOf(
                        "id"        to nextId.toString(),
                        "title"     to title,
                        "detail"    to detail,
                        "address"   to address,
                        "timestamp" to FieldValue.serverTimestamp(),
                        "userId"    to currentUser?.uid
                    )
                    tx.set(newDocRef, data)
                }
                    .addOnSuccessListener {
                        Toast.makeText(context, "Yardım talebi başarıyla oluşturuldu. ID=$it", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }) {
                Text("Yardım Talebi Oluştur", color = Color.White)
            }

        }
    }
}