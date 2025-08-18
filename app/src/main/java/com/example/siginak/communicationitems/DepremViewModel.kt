package com.example.siginak.communicationitems

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.IOException

class DepremViewModel : ViewModel() {
    private val _depremler = MutableStateFlow<List<Deprem>>(emptyList())
    val depremler: StateFlow<List<Deprem>> = _depremler

    fun fetchDepremler() {
        viewModelScope.launch(Dispatchers.IO) {
            val url = "http://www.koeri.boun.edu.tr/scripts/lst6.asp"
            try {
                Log.d("DEPREM_FETCH", "Bağlantı denemesi: $url")
                val connection = Jsoup.connect(url)
                    .timeout(5000)
                    .ignoreContentType(true)

                val response = connection.execute()
                Log.d("DEPREM_FETCH", "HTTP Durumu: ${response.statusCode()} ${response.statusMessage()}")
                Log.d("DEPREM_FETCH", "Headers: ${response.headers()}")

                val doc = response.parse()
                val rawText = doc.select("pre").text()
                Log.d("RAW_DATA", rawText.take(500))  // Gelen veri önizlemesi

                val lines = rawText
                    .split("\n")
                    .drop(6)

                val parsed = lines.mapNotNull { parseLine(it) }
                Log.d("DATA_LOADED", "Deprem sayısı: ${parsed.size}")

                _depremler.update { parsed }
            } catch (e: IOException) {
                Log.e("API_ERROR", "IO Hatası: ${e.message}", e)
                _depremler.update { emptyList() }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Beklenmeyen Hata: ${e.message}", e)
                _depremler.update { emptyList() }
            }
        }
    }

    private fun parseLine(line: String): Deprem? {
        val parts = line.trim().split("\\s+".toRegex())
        return try {
            if (parts.size >= 8) {
                val dateTime  = "${parts[0]} ${parts[1]}"
                val latitude  = parts[2].toDoubleOrNull() ?: return null
                val longitude = parts[3].toDoubleOrNull() ?: return null
                val depth     = parts[4].toDoubleOrNull() ?: 0.0
                val magnitude = parts[6].toDoubleOrNull() ?: 0.0

                /* 7. indeks yalnızca harf ise (MW / ML / MD …) büyüklük tipidir. */
                val locStart  = if (parts[7].matches(Regex("[A-Z]{1,3}"))) 8 else 7

                val rawTokens = parts.drop(locStart)
                    .filterNot { token ->
                        token.equals("İlksel", ignoreCase = true) ||
                                token.matches(Regex("^[-.:]+$"))           // --, .-, -.-, ::: vb.
                    }

                var location = rawTokens.joinToString(" ")
                    .replaceFirst(Regex("^[\\p{Punct}]+"), "")     // baştaki tüm noktalama bloğu
                    .trim()

                if (location.isEmpty()) location = "Bilinmeyen Yer"
                Deprem(dateTime, latitude, longitude, depth, magnitude, location)
            } else null
        } catch (_: Exception) {
            null
        }
    }
}

