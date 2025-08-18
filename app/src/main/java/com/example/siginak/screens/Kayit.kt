package com.example.siginak.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Kayit(navController: NavHostController) {
    val email     = remember { mutableStateOf("") }
    val password  = remember { mutableStateOf("") }
    val firstName = remember { mutableStateOf("") }
    val lastName  = remember { mutableStateOf("") }
    val address   = remember { mutableStateOf("") }
    val bloodType = remember { mutableStateOf("") }
    val telNo     = remember { mutableStateOf("") }

    // Dinamik yakın telefon numaraları listesi
    val yakinTels = remember { mutableStateListOf<String>() }

    val context = LocalContext.current
    val auth    = FirebaseAuth.getInstance()
    val db      = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kayıt Ol",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create an account",
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp,
                modifier   = Modifier.padding(bottom = 8.dp)
            )

            // Temel alanlar
            listOf(
                "E-Posta" to email,
                "Şifre" to password,
                "İsim" to firstName,
                "Soyisim" to lastName,
                "Telefon Numarası" to telNo,
                "Adres" to address,
                "Kan Grubu" to bloodType,
            ).forEach { (label, state) ->
                val isPassword = label == "Şifre"
                OutlinedTextField(
                    value               = state.value,
                    onValueChange       = { state.value = it },
                    label               = { Text(label) },
                    singleLine          = true,
                    modifier            = Modifier.fillMaxWidth(),
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions     = if (label.contains("Telefon")) KeyboardOptions(keyboardType = KeyboardType.Phone)
                    else KeyboardOptions.Default
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Yakın Telefon Numaraları", style = MaterialTheme.typography.titleMedium)

            // Dinamik yakın numara alanları
            yakinTels.forEachIndexed { index, phone ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value         = phone,
                        onValueChange = { yeni -> yakinTels[index] = yeni },
                        label         = { Text("Yakın #${index + 1}") },
                        singleLine    = true,
                        modifier      = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    IconButton(onClick = { yakinTels.removeAt(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil")
                    }
                }
            }

            TextButton(onClick = { yakinTels.add("") }) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Yakın Ekle")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Yakın Ekle")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Input doğrulama
                    val mail = email.value.trim()
                    if (mail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                        Toast
                            .makeText(context, "Lütfen geçerli bir e-posta girin", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    auth.createUserWithEmailAndPassword(mail, password.value)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser!!.uid
                                // Firestore'a profile kaydet
                                val data = mapOf(
                                    "isim"      to firstName.value.trim(),
                                    "soyisim"   to lastName.value.trim(),
                                    "adres"     to address.value.trim(),
                                    "telefon"   to telNo.value.trim(),
                                    "kanGrubu"  to bloodType.value.trim(),
                                    "yakinTels" to yakinTels.filter { it.isNotBlank() },
                                    "createdAt" to FieldValue.serverTimestamp()
                                )
                                db.collection("kullanici")
                                    .document(uid)
                                    .set(data)
                                    .addOnSuccessListener {
                                        navController.navigate("main") {
                                            popUpTo("giris") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast
                                            .makeText(context, "Kayıt başarısız: ${e.message}", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                            } else {
                                Toast
                                    .makeText(context, "Kayıt hatası: ${task.exception?.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Kayıt Ol")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = "By clicking continue, you agree to our Terms of Service and Privacy Policy",
                fontSize  = 12.sp,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                auth.signInAnonymously()
                    .addOnSuccessListener {
                        navController.navigate("main") {
                            popUpTo("giris") { inclusive = true }
                        }
                    }
                    .addOnFailureListener {
                        Toast
                            .makeText(context, "Giriş başarısız: ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            }) {
                Text("Misafir olarak devam et")
            }
        }
    }
}

//
//    db.collection("kullanici")
//        .document(uid)   // Doküman ID’si olarak FirebaseAuth UID’sini kullan
//        .set(userData)
//        .addOnSuccessListener {
//            // Profil başarıyla yazıldı
//        }
//        .addOnFailureListener { e ->
//            // Hata yönetimi
//            Log.e("Firestore", "Profil kaydı başarısız: ${e.message}")
//        }


//fun signUp(email: String, password: String, activity: Activity) {
//    val mail = email.trim()
//    if (mail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
//        Toast.makeText(activity, "Lütfen geçerli bir e-posta girin", Toast.LENGTH_SHORT).show()
//        return
//    }
//    FirebaseAuth.getInstance()
//        .createUserWithEmailAndPassword(mail, password)
//        .addOnSuccessListener {
//            // kullanıcı zaten girişli olduğundan direkt Main’e geç
//            activity.startActivity(Intent(activity, MainActivity::class.java))
//            activity.finish()
//        }
//        .addOnFailureListener {
//            Toast.makeText(activity, it.message ?: "Kayıt hatası", Toast.LENGTH_LONG).show()
//        }
//
//}
