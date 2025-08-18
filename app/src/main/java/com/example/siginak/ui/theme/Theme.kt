package com.example.siginak.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siginak.R

private val AppShapes = Shapes(
    small  = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large  = RoundedCornerShape(0.dp)
)

private val LightColors = lightColorScheme(
    primary     = Black,
    onPrimary   = White,
    background  = White,
    surface     = White,
    onSurface   = TextPrimary,
    outline     = Placeholder,
    // gerekirse diğer renkler de buraya
)

// 3. Latin-Extended desteği için FontFamily:
private val AppFontFamily = FontFamily(
    Font(R.font.noto_sans, weight = FontWeight.Normal),
    Font(R.font.noto_sans,    weight = FontWeight.Bold)
)

// 4. Tüm metin stillerinde bu fontu kullan:
private val AppTypography = Typography(
    displayLarge  = TextStyle(fontFamily = AppFontFamily, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = AppFontFamily, fontSize = 45.sp),
    displaySmall  = TextStyle(fontFamily = AppFontFamily, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = AppFontFamily, fontSize = 32.sp),
    headlineMedium= TextStyle(fontFamily = AppFontFamily, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = AppFontFamily, fontSize = 24.sp),
    titleLarge    = TextStyle(fontFamily = AppFontFamily, fontSize = 22.sp),
    titleMedium   = TextStyle(fontFamily = AppFontFamily, fontSize = 16.sp),
    titleSmall    = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp),
    bodyLarge     = TextStyle(fontFamily = AppFontFamily, fontSize = 16.sp),
    bodyMedium    = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp),
    bodySmall     = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp),
    labelLarge    = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp),
    labelMedium   = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp),
    labelSmall    = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp),
)

@Composable
fun SiginakTheme(content: @Composable ()->Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(), // veya dynamicDarkColorScheme()
        typography  = Typography,
        shapes      = AppShapes,
        content     = content
    )
}