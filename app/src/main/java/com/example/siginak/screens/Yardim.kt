package com.example.siginak.screens
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.siginak.appNavigation.BottomBarScreen
import com.example.siginak.communicationitems.yardimTalebi
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.google.firebase.Timestamp
import java.nio.file.WatchEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun YardimScreen(navController: NavController) {
    val context = LocalContext.current
    val helpRequestsRef = Firebase.firestore.collection("yardimTalebi")
    var requests by remember { mutableStateOf<List<yardimTalebi>>(emptyList()) }

    DisposableEffect(Unit) {
        val registration = helpRequestsRef.addSnapshotListener { snap, e ->
            if (e != null) {
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            requests = snap
                ?.documents
                ?.mapNotNull { doc ->
//                    val ts = doc.getTimestamp("timestamp") ?: return@mapNotNull null
                    yardimTalebi(
                        id        = doc.id,
                        title     = doc.getString("title")   ?: return@mapNotNull null,
                        detail    = doc.getString("detail")  ?: "",
                        address   = doc.getString("address") ?: "",
                        timestamp=  doc.getTimestamp("timestamp")
                    )
                }
                .orEmpty()
        }
        onDispose { registration.remove() }
    }

    Scaffold(
        bottomBar = {
            // alt kısımda sabit bir buton
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = { navController.navigate(BottomBarScreen.YeniYardim.route) },
                    colors = ButtonColors(containerColor = Color.White, contentColor = Color.Black,
                        disabledContainerColor = Color.White, disabledContentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Yardım Talebi Oluştur")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            items(requests) { item ->
                RequestCard(item) {
                    navController.navigate("yardimDetay/${item.id}")
                }
            }
        }
    }
}


@Composable
fun RequestCard(item: yardimTalebi, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ), onClick = onClick
    ) {
        Text(item.title,
            modifier = Modifier.padding( 5.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            item.detail,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding( 5.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(item.address,
            modifier = Modifier.padding( 5.dp),
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(4.dp))
        Text(
            modifier = Modifier.padding( 5.dp),
            text = sdf.format(item.timestamp?.toDate()),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// Örnek kullanım:
//@Composable
//fun PreviewHelpRequestScreen() {
//    val sample = List(3) { HelpRequest("Yardım Talebi", "Yardım türü", "adres kısa tarif") }
//    HelpRequestScreen(requests = sample) {}
//}


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun YardimScreen(onBack: () -> Unit) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Yeni Yardım Talebi") },
//                navigationIcon = {
//                    IconButton(onClick = { onBack() }) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Geri"
//                        )
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("Burada form olacak.")
//        }
//    }
//}