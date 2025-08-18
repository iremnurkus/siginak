package com.example.siginak.screens
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.squareup.moshi.Json
import com.example.siginak.R
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import kotlin.math.roundToInt

// ——— Data modelleri ———
data class NearbyPlaceLocation(val lat: Double, val lng: Double)
data class NearbyGeometry(val location: NearbyPlaceLocation)
data class NearbyPlaceResult(
    val name: String,
    val vicinity: String?,
    val geometry: NearbyGeometry
)
data class NearbySearchResponse(
    val results: List<NearbyPlaceResult>,
    val status: String,
    @Json(name = "error_message") val errorMessage: String? = null
)

// ——— Retrofit servisi ———
interface PlacesService {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun nearbySearch(
        @Query("location") location: String,
        @Query("radius") radius: Int = 10000,
        @Query("keyword") keyword: String,
        @Query("key") apiKey: String
    ): NearbySearchResponse
}
@Composable
fun SearchableMap() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    // — İzin kontrolü —
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    // — Kullanıcı konumu —
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            LocationServices
                .getFusedLocationProviderClient(context)
                .lastLocation
                .addOnSuccessListener { loc ->
                    loc?.let { userLocation = LatLng(it.latitude, it.longitude) }
                }
        }
    }

    // — Harita ve sonuç state’leri —
    var places by remember { mutableStateOf<List<NearbyPlaceResult>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLocation ?: LatLng(39.0, 35.0),
            if (userLocation != null) 14f else 6f
        )
    }

    // — Retrofit servisi ve API anahtarı —
    val serverKey = stringResource(R.string.places_server_key)

// ① Burada Moshi’yı oluşturun:
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

// ② Sonra Retrofit’i bu Moshi ile başlatın:
    val service = remember {
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            // converter’ı Moshi örneğinizle ekleyin:
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply { level = BODY })
                    .build()
            )
            .build()
            .create(PlacesService::class.java)
    }

    // — Renkli ikonlar —
    val hospitalIcon  = rememberScaledMarkerIcon(R.drawable.redmap,   20.dp)
    val policeIcon    = rememberScaledMarkerIcon(R.drawable.bluedot,  20.dp)
    val gatheringIcon = rememberScaledMarkerIcon(R.drawable.greendot, 20.dp)
    var markerIcon by remember { mutableStateOf(hospitalIcon) }

    // — Anahtar kelime ile arama yapan lambda (Artık composable scope içinde!) —
    val performNearbySearch: (String) -> Unit = { keyword ->
        scope.launch {
            try {
                val loc = userLocation ?: LatLng(39.0, 35.0)
                val resp = service.nearbySearch(
                    location = "${loc.latitude},${loc.longitude}",
                    radius   = 10000,
                    keyword  = keyword,
                    apiKey   = serverKey
                )
                if (resp.status == "OK") {
                    places = resp.results
                    resp.results.firstOrNull()?.let {
                        cameraState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(
                                    LatLng(it.geometry.location.lat, it.geometry.location.lng),
                                    14f
                                )
                            )
                        )
                    }
                } else {
                    errorMsg = "Sunucu: ${resp.status} – ${resp.errorMessage.orEmpty()}"
                }
            } catch (e: Exception) {
//                errorMsg = "İstek hatası: ${e.message}"
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {
            places.forEach { p ->
                Marker(
                    state   = MarkerState(position = LatLng(p.geometry.location.lat, p.geometry.location.lng)),
                    title   = p.name,
                    snippet = p.vicinity.orEmpty(),
                    icon    = markerIcon
                )
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(8.dp)
        ) {
            if (!hasLocationPermission) {
                Text("Konum izni gerekli!", color = Color.Red)
            } else if (userLocation == null) {
                Text("Konum alınıyor...", color = Color.Gray)
            } else {
                errorMsg?.let { Text(it, color = Color.Red) }
            }

            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    if (!hasLocationPermission) {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        return@Button
                    }
                    markerIcon = hospitalIcon
                    performNearbySearch("hastane")
                }, Modifier.weight(1f), colors = ButtonColors(containerColor = Color.Red, contentColor = Color.White
                , disabledContentColor = Color.White,
                    disabledContainerColor = Color.Red)) { Text("Hastaneler") }

                Button(onClick = {
                    if (!hasLocationPermission) {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        return@Button
                    }
                    markerIcon = policeIcon
                    performNearbySearch("polis karakolu")
                }, Modifier.weight(1f), colors = ButtonColors(containerColor = Color.Blue, contentColor = Color.White
                    , disabledContentColor = Color.White,
                    disabledContainerColor = Color.Blue)) { Text("Polis Karakolu") }

                Button(onClick = {
                    if (!hasLocationPermission) {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        return@Button
                    }
                    markerIcon = gatheringIcon
                    uriHandler.openUri("https://www.turkiye.gov.tr/afet-ve-acil-durum-yonetimi-acil-toplanma-alani-sorgulama")
                }, Modifier.weight(1f), colors = ButtonColors(containerColor = Color.DarkGray, contentColor = Color.White
                    , disabledContentColor = Color.White,
                    disabledContainerColor = Color.DarkGray)) { Text("Toplanma Alanları") }
            }
        }
    }
}
    @Composable
    fun rememberScaledMarkerIcon(
        @DrawableRes resId: Int,
        sizeDp: Dp
    ): BitmapDescriptor {
        val context = LocalContext.current
        val density = LocalContext.current.resources.displayMetrics.density

        return remember(resId, sizeDp) {
            // dp -> px
            val sizePx = (sizeDp.value * density).roundToInt()
            // Drawable'ı Bitmap'e çevir
            val original = BitmapFactory.decodeResource(context.resources, resId)
            // Ölçeklendir
            val scaled: Bitmap = Bitmap.createScaledBitmap(original, sizePx, sizePx, true)
            // BitmapDescriptor'a dönüştür
            BitmapDescriptorFactory.fromBitmap(scaled)
        }
    }
//@Composable
//fun HospitalMapScreen(viewModel: HospitalMapViewModel = viewModel()) {
//    // collectAsState() ile gelen State<T>'i value ile açıyoruz
//    val userLocState = viewModel.userLocation.collectAsState()
//    val hospitalsState = viewModel.hospitals.collectAsState()
//    val userLoc = userLocState.value
//    val hospitals = hospitalsState.value
//
//    // Türkiye'nin merkez koordinatları (Ankara yakını)
//    val turkeyCenter = LatLng(39.9334, 32.8597)
//
//    val cameraState = rememberCameraPositionState {
//        position = if (userLoc != null) {
//            // Kullanıcı konumu varsa ona odaklan
//            CameraPosition.fromLatLngZoom(userLoc, 14f)
//        } else {
//            // Kullanıcı konumu yoksa Türkiye'yi göster
//            CameraPosition.fromLatLngZoom(turkeyCenter, 6f)
//        }
//    }
//    val context = LocalContext.current
//
//    // Hastane ikonu için BitmapDescriptor oluştur
//    val hospitalIcon = remember {
//        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
//    }
//
//    // İzin kontrolü ve isteme
//    val locationPermissions = arrayOf(
//        Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_COARSE_LOCATION
//    )
//
//    val permissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
//            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
//            viewModel.loadUserLocation()
//        } else {
//            Log.w("MapScreen", "Konum izni reddedildi")
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        // İzin kontrolü yap
//        val hasLocationPermission = locationPermissions.any { permission ->
//            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
//        }
//
//        if (hasLocationPermission) {
//            viewModel.loadUserLocation()
//        } else {
//            permissionLauncher.launch(locationPermissions)
//        }
//    }
//
//    Box(Modifier.fillMaxSize()) {
//        GoogleMap(Modifier.fillMaxSize(),
//            cameraPositionState = cameraState,
//            properties = MapProperties(isMyLocationEnabled = true)
//        ) {
//            // Kullanıcı konumu marker'ı
//            userLoc?.let { location ->
//                Marker(
//                    state = MarkerState(position = location),
//                    title = "Konumunuz",
//                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
//                )
//            }
//
//            // Hastane marker'ları
//            hospitals.forEach { h: Hospital ->
//                Marker(
//                    state = MarkerState(position = LatLng(h.lat, h.lng)),
//                    title = h.name,
//                    snippet = "Hastane - ${String.format("%.2f km", h.distance)}",
//                    icon = hospitalIcon
//                )
//            }
//        }
//        Button(
//            onClick = {
//                val url =
//                    "https://www.turkiye.gov.tr/afet-ve-acil-durum-yonetimi-acil-toplanma-alani-sorgulama"
//                // 1) Dış tarayıcıda açmak için:
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                context.startActivity(intent)
//                // — veya —
//                // 2) Chrome Custom Tab ile açmak için:
//                // val customTabs = CustomTabsIntent.Builder().build()
//                // customTabs.launchUrl(context, Uri.parse(url))
//            }
//        ) { Text("Acil Toplanma Alanı Sorgula") }
//    }
//}
//
//class HospitalMapViewModel(application: Application) : AndroidViewModel(application) {
//    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)
//    private val placesClient by lazy { Places.createClient(application) }
//
//    private val _userLocation = MutableStateFlow<LatLng?>(null)
//    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()
//    private val _hospitals = MutableStateFlow<List<Hospital>>(emptyList())
//    val hospitals: StateFlow<List<Hospital>> = _hospitals.asStateFlow()
//
//    init {
//        try {
//            if (!Places.isInitialized()) {
//                Places.initialize(application, application.getString(R.string.google_maps_key))
//            }
//        } catch (e: Exception) {
//            Log.e("VM", "Places initialization error: ${e.message}")
//        }
//    }
//
//    fun loadUserLocation() {
//        try {
//            if (ActivityCompat.checkSelfPermission(
//                    getApplication(),
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(
//                    getApplication(),
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                Log.w("VM", "Konum izni yok")
//                return
//            }
//
//            fusedClient.lastLocation.addOnSuccessListener { loc ->
//                if (loc == null) {
//                    Log.w("VM", "Konum alınamadı")
//                } else {
//                    val userLatLng = LatLng(loc.latitude, loc.longitude)
//                    _userLocation.value = userLatLng
//                    searchNearbyHospitals(userLatLng)
//                }
//            }.addOnFailureListener { exception ->
//                Log.e("VM", "Konum alma hatası: ${exception.message}")
//            }
//        } catch (e: Exception) {
//            Log.e("VM", "loadUserLocation error: ${e.message}")
//        }
//    }
//
//    private fun searchNearbyHospitals(userLocation: LatLng) {
//        try {
//            // Nearby Search için request oluştur
//            val placeFields = listOf(
//                Place.Field.ID,
//                Place.Field.NAME,
//                Place.Field.LAT_LNG,
//                Place.Field.TYPES,
//                Place.Field.RATING,
//                Place.Field.ADDRESS
//            )
//
//            // Yakınlık araması yap (5km yarıçap)
//            val searchByTextRequest = SearchByTextRequest.builder(
//                "hospital near me",
//                placeFields
//            )
//                .setLocationBias(
//                    CircularBounds.newInstance(userLocation, 5000.0) // 5km yarıçap
//                )
//                .setMaxResultCount(20)
//                .build()
//
//            placesClient.searchByText(searchByTextRequest)
//                .addOnSuccessListener { response ->
//                    Log.d("VM", "Bulunan yer sayısı: ${response.places.size}")
//
//                    val hospitalList = response.places.mapNotNull { place ->
//                        place.latLng?.let { latLng ->
//                            val distance = calculateDistance(userLocation, latLng)
//                            Hospital(
//                                name = place.name ?: "Bilinmeyen Hastane",
//                                lat = latLng.latitude,
//                                lng = latLng.longitude,
//                                distance = distance,
//                                address = place.address ?: "",
//                                rating = place.rating ?: 0.0
//                            )
//                        }
//                    }.sortedBy { it.distance } // Mesafeye göre sırala
//
//                    _hospitals.value = hospitalList
//                    Log.d("VM", "Hastane listesi güncellendi: ${hospitalList.size} hastane")
//                }
//                .addOnFailureListener { exception ->
//                    Log.e("VM", "Hastane arama hatası: ${exception.message}")
//                }
//        } catch (e: Exception) {
//            Log.e("VM", "searchNearbyHospitals error: ${e.message}")
//        }
//    }
//
//    private fun calculateDistance(start: LatLng, end: LatLng): Double {
//        val results = FloatArray(1)
//        Location.distanceBetween(
//            start.latitude, start.longitude,
//            end.latitude, end.longitude,
//            results
//        )
//        return (results[0] / 1000.0) // km cinsinden
//    }
//
//    companion object {
//        fun provideFactory(application: Application): ViewModelProvider.Factory {
//            return object : ViewModelProvider.Factory {
//                @Suppress("UNCHECKED_CAST")
//                override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                    return HospitalMapViewModel(application) as T
//                }
//            }
//        }
//    }
//}
//
//data class Hospital(
//    val name: String,
//    val lat: Double,
//    val lng: Double,
//    val distance: Double = 0.0,
//    val address: String = "",
//    val rating: Double = 0.0
//)

//
//@Composable
//fun MapWithButtons() {
//    val context = LocalContext.current
//
//    // Kamera başlangıç konumu (örnek: Türkiye merkezi)
//    val turkeyLatLng = LatLng(39.0, 35.0)
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(turkeyLatLng, 5f)
//    }
//
//    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Button(modifier = Modifier.weight(1f), onClick = {
//                // e-Devlet Toplanma Alanları
//                val url = "https://www.turkiye.gov.tr/afet-ve-acil-durum-toplanma-alani-sorgulama"
//                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
//            }) {
//                Text("Toplanma \nAlanları")
//            }
//
//            Button(modifier = Modifier.weight(1f), onClick = {
//                // Hastaneler
//                val uri = Uri.parse("geo:0,0?q=hastaneler")
//                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
//                    setPackage("com.google.android.apps.maps")
//                }
//                // Google Maps yüklü değilse web üzerinden aç
//                if (intent.resolveActivity(context.packageManager) != null) {
//                    context.startActivity(intent)
//                } else {
//                    context.startActivity(
//                        Intent(
//                            Intent.ACTION_VIEW,
//                            Uri.parse("https://www.google.com/maps/search/?api=1&query=hastaneler")
//                        )
//                    )
//                }
//            }) {
//                Text("Hastaneler")
//            }
//
//            Button(modifier = Modifier.weight(1f), onClick = {
//                // Polis Karakolları
//                val uri = Uri.parse("geo:0,0?q=polis+karakolu")
//                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
//                    setPackage("com.google.android.apps.maps")
//                }
//                if (intent.resolveActivity(context.packageManager) != null) {
//                    context.startActivity(intent)
//                } else {
//                    context.startActivity(
//                        Intent(
//                            Intent.ACTION_VIEW,
//                            Uri.parse("https://www.google.com/maps/search/?api=1&query=polis+karakolu")
//                        )
//                    )
//                }
//            }) {
//                Text("Karakol")
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Harita önizlemesi
//        GoogleMap(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(300.dp),
//            cameraPositionState = cameraPositionState,
//            properties = MapProperties(isMyLocationEnabled = false)
//        )
//    }
//}


//@Composable
//fun MapScreen() {
//    val context = LocalContext.current
//    val url = "https://www.turkiye.gov.tr/afet-ve-acil-durum-yonetimi-acil-toplanma-alani-sorgulama"
//    Button(onClick = {
//        // 1) Dış tarayıcıda açmak için:
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//        context.startActivity(intent)
//        // — veya —
//        // 2) Chrome Custom Tab ile açmak için:
//        // val customTabs = CustomTabsIntent.Builder().build()
//        // customTabs.launchUrl(context, Uri.parse(url))
//    }) {
//        Text("Acil Toplanma Alanı Sorgula")
//    }
//}

@Composable
fun OpenEdevletButton() {
    val context = LocalContext.current
    val url = "https://www.turkiye.gov.tr/afet-ve-acil-durum-yonetimi-acil-toplanma-alani-sorgulama"
        // 1) Dış tarayıcıda açmak için:
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
        // — veya —
        // 2) Chrome Custom Tab ile açmak için:
        // val customTabs = CustomTabsIntent.Builder().build()
        // customTabs.launchUrl(context, Uri.parse(url))

}


data class AddressItem(
    val title: String,
    val address: String,
    val latLng: LatLng? = null
)

//@Composable
//fun MapScreen(addressItems: List<AddressItem>) {
//    val cameraState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(
//            LatLng(41.015137, 28.979530), 10f
//        )
//    }
//
//    GoogleMap(
//        modifier = Modifier.fillMaxSize(),
//        cameraPositionState = cameraState
//    ) {
//        addressItems.forEach { item ->
//            item.latLng?.let { loc ->
//                Marker(
//                    state = MarkerState(position = loc),
//                    title = item.title,
//                    snippet = item.address,
//                    icon = BitmapDescriptorFactory.fromResource(R.drawable.my_marker_icon)
//                )
//            }
//        }
//    }
//}