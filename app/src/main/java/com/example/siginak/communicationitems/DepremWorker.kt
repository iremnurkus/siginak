package com.example.siginak.communicationitems

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.io.IOException
import java.util.Date

// Earthquake.kt
data class Earthquake(
    val id: String,
    val mag: Double,
    val title: String,
    val date: Long,
    val geojson: GeoJson
)
data class GeoJson(val coordinates: List<Double>)

// KandilliApi.kt
interface KandilliApi {
    @GET("kandilli/live") // örnek JSON endpoint
    suspend fun getLatest(): List<Earthquake>
}

object RetrofitClient {
    private val moshi = Moshi.Builder().build()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.orhanaydogdu.com.tr/deprem/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    val api = retrofit.create(KandilliApi::class.java)
}
// EarthquakeViewModel.kt
class EarthquakeViewModel : ViewModel() {
    private val _quakes = MutableStateFlow<List<Earthquake>>(emptyList())
    val quakes: StateFlow<List<Earthquake>> = _quakes.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            runCatching { RetrofitClient.api.getLatest() }
                .onSuccess { _quakes.value = it }
                .onFailure { /* hata yönetimi */ }
        }
    }
}
// KandilliWorker.kt
class KandilliWorker(
    ctx: Context, params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    private val prefs = ctx.getSharedPreferences("deprem_prefs", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result {
        val last = prefs.getLong("last_time", 0L)
        val list = RetrofitClient.api.getLatest()
        val newQuakes = list.filter { it.date > last }

        if (newQuakes.isNotEmpty()) {
            prefs.edit().putLong("last_time", newQuakes.maxOf { it.date }).apply()
            newQuakes.forEach { sendNotification(it) }
        }
        return Result.success()
    }

    private fun sendNotification(q: Earthquake) {
        val nm = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(
                    "EQ", "Deprem Bildirimleri",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val notif = NotificationCompat.Builder(applicationContext, "EQ")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("M${q.mag} • ${q.title}")
            .setContentText(Date(q.date).toString())
            .setAutoCancel(true)
            .build()

        nm.notify(q.id.hashCode(), notif)
    }
}
