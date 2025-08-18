package com.example.siginak.appNavigation

import android.R
import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.siginak.communicationitems.Deprem
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import androidx.core.app.NotificationCompat
import android.app.NotificationManager

data class LocationProps(
    @SerializedName("epiCenter") val epiCenter: City
)
data class City(
    @SerializedName("name") val name: String?
)
interface KandilliService {
    @GET("deprem/kandilli/live")
    suspend fun getLive(): Deprem
}

object ApiClient {
    val service: KandilliService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.orhanaydogdu.com.tr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KandilliService::class.java)
    }
}

class DepremWorker(
    ctx: Context, wp: WorkerParameters
): CoroutineWorker(ctx, wp) {
    companion object {
        private const val CHANNEL_ID = "EQ_CHANNEL"
    }

    override suspend fun doWork(): Result {
        return try {
            // tek nesne al
            val latest = ApiClient.service.getLive()
            val prefs  = applicationContext
                .getSharedPreferences("eq_prefs", Context.MODE_PRIVATE)
            val last   = prefs.getString("last_date", null)

            if (last != latest.dateTime) {
                prefs.edit()
                    .putString("last_date", latest.dateTime)
                    .apply()
                showNotification(latest)
            }
            Result.success()
        } catch (e: Exception) {
            // istek veya parsing hatası olursa retry edelim
            Result.retry()
        }
    }
    private fun showNotification(eq: Deprem) {
        val ch = "eq_ch"
        val nm = applicationContext
            .getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(ch,"Deprem Bildirimleri",
                    NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_overlay)  // mutilate var olan bir resource
            .setContentTitle("Yeni Deprem: M${eq.magnitude}")
            .setContentText("${eq.location} • ${eq.dateTime}")
            .setAutoCancel(true)
            .build()

    }
}

