package com.example.siginak.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonColors
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.siginak.appNavigation.BottomBarScreen.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.EventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth
    val currentUser get() = auth.currentUser

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            auth.signOut()
            onComplete()
        }
    }
}

@IgnoreExtraProperties
data class UserProfile(
    val isim: String        = "",
    val soyisim: String     = "",
    val adres: String       = "",
    val yakinTels: List<String> = emptyList(),  // birden fazla yakının numarası
    val telefon: String     = "",
    val kanGrubu: String="",
    val createdAt: Timestamp? = null
)
// ViewModel: Kullanıcı ve yakın yönetimi
class KullaniciViewModel : ViewModel() {
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Ana profil
    private val _profile = mutableStateOf<UserProfile?>(null)
    val profile: State<UserProfile?> = _profile

    // Yakın profilleri
    private val _relativeProfiles = mutableStateOf<List<UserProfile>>(emptyList())
    val relativeProfiles: State<List<UserProfile>> = _relativeProfiles

    private var listener: ListenerRegistration? = null

    init {
        // Mevcut kullanıcı UID'si varsa listener'ı kur
        val uid = auth.currentUser?.uid
        if (uid != null) {
            listener = db.collection("kullanici")
                .document(uid)
                .addSnapshotListener { snap, err ->
                    if (err == null && snap != null && snap.exists()) {
                        val me = snap.toObject(UserProfile::class.java)
                        _profile.value = me

                        // Yakın telefonları güncelle
                        val phones = me?.yakinTels.orEmpty()
                        if (phones.isNotEmpty()) fetchRelativesByPhones(phones)
                        else _relativeProfiles.value = emptyList()
                    }
                }
        }
    }

    // Firestore'dan yakın profilleri çek
    private fun fetchRelativesByPhones(phones: List<String>) {
        viewModelScope.launch {
            try {
                val chunks  = phones.chunked(10)
                val results = mutableListOf<UserProfile>()
                for (chunk in chunks) {
                    val snap = db.collection("kullanici")
                        .whereIn("telefon", chunk)
                        .get()
                        .await()
                    results += snap.documents.mapNotNull { it.toObject(UserProfile::class.java) }
                }
                _relativeProfiles.value = results
            } catch (_: Exception) {
                _relativeProfiles.value = emptyList()
            }
        }
    }

    // Yakın silme
    fun removeRelative(phone: String) {
        auth.currentUser?.uid?.let { uid ->
            db.collection("kullanici")
                .document(uid)
                .update("yakinTels", FieldValue.arrayRemove(phone))
        }
    }

    // Yeni yakın ekleme
    fun addRelative(phone: String) {
        auth.currentUser?.uid?.let { uid ->
            db.collection("kullanici")
                .document(uid)
                .update("yakinTels", FieldValue.arrayUnion(phone))
        }
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }
}

// Composable: profil ve yakın yönetimi
@Composable
fun KullaniciScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    profilViewModel: KullaniciViewModel = viewModel()
) {
    val firebaseUser   = authViewModel.currentUser
    val profileState   by profilViewModel.profile
    val relatives      by profilViewModel.relativeProfiles
    var newRelativeTel by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD0D0D0))
    ) {
        // — ÜST: Profil sabit —
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Hoşgeldiniz
            val displayName = profileState?.isim
                ?: firebaseUser?.displayName
                ?: firebaseUser?.email
                ?: "Kullanıcı"
            Text(
                text = "Hoşgeldiniz, $displayName",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )
            Spacer(Modifier.height(8.dp))
            // Profil kartı
            profileState?.let { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("İsim: ${user.isim}", style = MaterialTheme.typography.bodyLarge)
                        Text("Soyisim: ${user.soyisim}", style = MaterialTheme.typography.bodyLarge)
                        Text("Adres: ${user.adres}", style = MaterialTheme.typography.bodyLarge)
                        Text("Telefon: ${user.telefon}", style = MaterialTheme.typography.bodyLarge)
                        Text("Kan Grubu: ${user.kanGrubu}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // — ORTA: Yakınlar listesi sadece burası kayar —
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Yakınlarınız",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
            Spacer(Modifier.height(8.dp))

            when {
                profileState == null -> {
                    CircularProgressIndicator()
                }
                relatives.isEmpty() -> {
                    Text("Henüz kayıtlı yakınınız yok.", color = Color.Gray)
                }
                else -> {
                    relatives.forEach { rel ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("İsim: ${rel.isim}", style = MaterialTheme.typography.bodyLarge)
                                    Text("Soyisim: ${rel.soyisim}", style = MaterialTheme.typography.bodyLarge)
                                    Text("Adres: ${rel.adres}", style = MaterialTheme.typography.bodyLarge)
                                    Text("Telefon: ${rel.telefon}", style = MaterialTheme.typography.bodyLarge)
                                    Text("Kan Grubu: ${rel.kanGrubu}", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            IconButton(
                                onClick = { profilViewModel.removeRelative(rel.telefon) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Yakını Sil")
                            }
                        }
                    }
                }
            }
        }

        // — ALTA: Yeni yakın + çıkış burada sabit —
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Yeni yakın ekle", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = newRelativeTel,
                onValueChange = { newRelativeTel = it },
                placeholder = { Text("Telefon Numarası") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.Black
                )
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val tel = newRelativeTel.trim()
                    if (tel.isNotBlank()) {
                        profilViewModel.addRelative(tel)
                        newRelativeTel = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Yakın Ekle", color = Color.White)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    authViewModel.signOut {
                        navController.navigate("giris") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Çıkış Yap", color = Color(0xFF000000))
            }
        }
    }
}
//// ViewModel: Oturum yönetimi
//class AuthViewModel : ViewModel() {
//    private val auth = Firebase.auth
//    val currentUser get() = auth.currentUser
//
//    fun signOut(onComplete: () -> Unit) {
//        viewModelScope.launch {
//            auth.signOut()
//            onComplete()
//        }
//    }
//}
//
//@Composable
//fun KullaniciScreen(
//    navController: NavController,
//    viewModel: AuthViewModel = viewModel()
//) {
//    val user = viewModel.currentUser
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Red),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = "Hoşgeldiniz, ${user?.displayName ?: user?.email ?: "Kullanıcı"}",
//                fontWeight = FontWeight.Bold,
//                color = Color.White,
//                style = MaterialTheme.typography.h6
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Button(
//                onClick = {
//                    viewModel.signOut {
//                        navController.navigate("giris") {
//                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
//                        }
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
//            ) {
//                Text(
//                    text = "Çıkış Yap",
//                    color = Color.Red,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
//        }
//    }
//}