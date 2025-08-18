package com.example.siginak.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

@Composable
fun GirisScreen(navController: NavController) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val auth = remember { Firebase.auth }
    var user by remember { mutableStateOf(auth.currentUser) }

    // Debug modda emulator kullan (reCAPTCHA sorununu önlemek için)
    LaunchedEffect(Unit) {
        if (BuildConfig.DEBUG) {
            try {
                auth.useEmulator("10.0.2.2", 9099)
            } catch (e: Exception) {
                // Emulator zaten ayarlanmışsa hata verebilir, göz ardı et
                Log.d("Auth", "Emulator zaten ayarlanmış veya mevcut değil")
            }
        }
    }

    LaunchedEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            user = it.currentUser
        }
        auth.addAuthStateListener(listener)
    }

    // Oturum açıksa hemen yönlendir
    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Giriş Yap",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hata mesajı göster
        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        TextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = "" // Hata mesajını temizle
            },
            label = { Text("E-posta") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = "" // Hata mesajını temizle
            },
            label = { Text("Şifre") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = if (passwordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(
                        text = if (passwordVisible) "🙈" else "👁️",
                        modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "E-posta ve şifre alanları boş olamaz"
                    return@Button
                }

                isLoading = true
                errorMessage = ""

                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        isLoading = false
                        // Yönlendirme LaunchedEffect'te hallediliyor
                    }
                    .addOnFailureListener { exception ->
                        isLoading = false
                        errorMessage = when (exception) {
                            is FirebaseAuthInvalidUserException ->
                                "Bu e-posta adresi kayıtlı değil"
                            is FirebaseAuthInvalidCredentialsException ->
                                "E-posta veya şifre hatalı"
                            is FirebaseAuthUserCollisionException ->
                                "Bu e-posta zaten kullanımda"
                            else -> {
                                Log.e("Auth", "Giriş hatası: ${exception.message}")
                                "${exception.message}" +
                                        "\nGiriş yapılırken bir hata oluştu. Lütfen tekrar deneyin."
                            }
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Giriş Yap")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navController.navigate("kayit")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(text = "Kayıt Ol")
        }
    }
}

fun SignIn(
    email: String,
    password: String,
    activity: Activity,
    context: Context
) {
    val mAuth = FirebaseAuth.getInstance()
    mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener({ task ->
            if (task.isSuccessful()) {
                // Giriş başarılı
                val user: FirebaseUser? = mAuth.getCurrentUser()
                // user.getEmail(), user.getUid() gibi bilgilere erişebilirsin
                val intent = Intent(activity, MainActivity::class.java)
                activity.startActivity(intent)
            } else {
                // Giriş başarısız
                val e: Exception? = task.getException()
                Toast.makeText(context,"${e?.message}", Toast.LENGTH_LONG).show()
                // Hata mesajı gösterebilirsin
            }
        })
}