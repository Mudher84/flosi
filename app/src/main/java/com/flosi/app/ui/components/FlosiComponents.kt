package com.flosi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

val FlosiPurple = Color(0xFF8A5CF6)
val FlosiPurpleDeep = Color(0xFF6E3EE8)
val FlosiPurpleSoft = Color(0xFFF3EEFF)
val FlosiLavender = Color(0xFFE9DEFF)
val FlosiBg = Color(0xFFF7F7FA)
val FlosiSurface = Color(0xFFFFFFFF)
val FlosiText = Color(0xFF18151F)
val FlosiMuted = Color(0xFF8B8792)
val FlosiLine = Color(0xFFF0EDF4)
val FlosiGreen = Color(0xFF31C68B)
val FlosiRed = Color(0xFFFF6B72)
val FlosiOrange = Color(0xFFFF8B4A)
val FlosiBlue = Color(0xFF3FA7F5)
val FlosiDark = Color(0xFF17151D)

val FlosiHeroBrush = Brush.linearGradient(listOf(Color(0xFFA86BFF),Color(0xFF8C5AF3),Color(0xFF6D3CE4)))

@Composable
fun FlosiPage(title: String,subtitle: String = "",onBack: (() -> Unit)? = null,content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(FlosiBg).statusBarsPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f),horizontalAlignment = Alignment.Start) {
                if (subtitle.isNotBlank()) { Text(text = subtitle,color = FlosiMuted,fontSize = 11.sp,fontWeight = FontWeight.Medium);Spacer(Modifier.height(2.dp)) }
                Text(text = title,color = FlosiText,fontWeight = FontWeight.Bold,fontSize = 23.sp)
            }
            if (onBack != null) {
                Spacer(Modifier.width(12.dp))
                Surface(modifier = Modifier.size(42.dp).clickable(onClick = onBack),shape = CircleShape,color = Color.White,shadowElevation = 1.dp) {
                    Box(contentAlignment = Alignment.Center) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,contentDescription = "رجوع",tint = FlosiText) }
                }
            }
        }
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp).padding(bottom = 18.dp),verticalArrangement = Arrangement.spacedBy(12.dp),content = content)
    }
}

@Composable
fun CardBox(modifier: Modifier = Modifier,content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth(),colors = CardDefaults.cardColors(containerColor = FlosiSurface),shape = RoundedCornerShape(24.dp),elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),verticalArrangement = Arrangement.spacedBy(9.dp),content = content)
    }
}

@Composable
fun Metric(label: String,value: String,tone: Color = FlosiText) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, color = FlosiMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(value, color = tone, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActionRow(title: String,subtitle: String = "",value: String? = null,accent: Color = FlosiPurple,onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(38.dp).background(accent.copy(alpha = .11f), RoundedCornerShape(12.dp)),contentAlignment = Alignment.Center) { Box(Modifier.size(9.dp).background(accent, CircleShape)) }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f),horizontalAlignment = Alignment.Start) {
            Text(title, fontWeight = FontWeight.SemiBold, color = FlosiText, fontSize = 14.sp)
            if (subtitle.isNotBlank()) Text(subtitle, color = FlosiMuted, fontSize = 11.sp)
        }
        if (value != null) Text(value, fontWeight = FontWeight.Bold, color = accent, fontSize = 13.sp)
    }
}

@Composable
fun SectionTitle(title: String,action: String? = null,onAction: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp),verticalAlignment = Alignment.CenterVertically) {
        Text(title,modifier = Modifier.weight(1f),fontSize = 17.sp,fontWeight = FontWeight.Bold,color = FlosiText)
        if (action != null) Text(action,color = FlosiPurple,modifier = Modifier.clickable(onClick = onAction),fontSize = 12.sp,fontWeight = FontWeight.SemiBold)
    }
}

fun moneyText(value: Long, currencyCode: String = "IQD"): String {
    val code=currencyCode.trim().uppercase()
    return runCatching {
        val format=NumberFormat.getCurrencyInstance(Locale.US)
        format.currency=Currency.getInstance(code)
        format.maximumFractionDigits=if(code in setOf("IQD","JPY","KRW")) 0 else 2
        format.minimumFractionDigits=0
        format.format(abs(value))
    }.getOrElse { "%,d %s".format(Locale.US,abs(value),code) }
}

fun signedMoney(value: Long, currencyCode: String = "IQD"): String =
    (if (value >= 0) "+" else "−") + moneyText(value,currencyCode)
