package com.example.siginak

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat

class ProviderChangeReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            KonumTracker.onProviderStatusChanged(isEnabled, ctx)
        }
    }
}
object KonumTracker {
    private var startTime: Long = 0
    private var handler: Handler? = null
    private const val DELAY = 10 * 60 * 1000L

    fun onProviderStatusChanged(enabled: Boolean, ctx: Context) {
        if (enabled) {
            // Açıldıysa
            startTime = System.currentTimeMillis()
            handler = Handler(Looper.getMainLooper()).apply {
                postDelayed({
                    // 10dk doldu, hâlâ açık mı kontrol et
                    val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        sendWarningNotification(ctx)
                    }
                }, DELAY)
            }
        } else {
            // Kapatıldıysa, iptal et
            handler?.removeCallbacksAndMessages(null)
        }
    }

    private fun sendWarningNotification(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel("LOC_WARN", "Konum Uyarıları", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val notif = NotificationCompat.Builder(ctx, "LOC_WARN")
            .setSmallIcon(R.drawable.notificationn)
            .setContentTitle("Konum Süre Aşıldı")
            .setContentText("Konum servisiniz 10 dakikadır açık kalıyor.")
            .setAutoCancel(true)
            .build()
        nm.notify(1001, notif)
    }
}
