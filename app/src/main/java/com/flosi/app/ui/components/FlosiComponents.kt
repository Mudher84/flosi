package com.flosi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.flosi.app.i18n.localizedLegacyText
import java.math.BigInteger
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

val FlosiPurple = Color(0xFF7757FF)
val FlosiPurpleDeep = Color(0xFF5134D8)
val FlosiPurpleSoft = Color(0xFFF0ECFF)
val FlosiLavender = Color(0xFFE4DBFF)
val FlosiBg = Color(0xFFF8F7FC)
val FlosiSurface = Color(0xFFFFFFFF)
val FlosiText = Color(0xFF17141F)
val FlosiMuted = Color(0xFF8E8999)
val FlosiLine = Color(0xFFEDE9F3)
val FlosiGreen = Color(0xFF20B981)
val FlosiRed = Color(0xFFF45F6B)
val FlosiOrange = Color(0xFFFF9D4D)
val FlosiBlue = Color(0xFF4D9BFF)
val FlosiDark = Color(0xFF15121C)
val FlosiGold = Color(0xFFE7B65B)

val FlosiHeroBrush = Brush.linearGradient(
    listOf(Color(0xFF9A7BFF), Color(0xFF7252F3), Color(0xFF4F34C8))
)

val FlosiPremiumBrush = Brush.linearGradient(
    listOf(Color(0xFF17121F), Color(0xFF241A35), Color(0xFF3A285B))
)

@Composable
fun FlosiPage(title: String, subtitle: String = "", onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                if (subtitle.isNotBlank()) {
                    Text(localizedLegacyText(subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(3.dp))
                }
                Text(localizedLegacyText(title), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold, fontSize = 25.sp)
            }
            if (onBack != null) {
                Spacer(Modifier.width(12.dp))
                Surface(
                    modifier = Modifier.size(44.dp).clickable(onClick = onBack),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 3.dp,
                    tonalElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, localizedLegacyText("رجوع"), tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
fun CardBox(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 17.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
fun PremiumCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp), color = FlosiDark, shadowElevation = 6.dp) {
        Box(Modifier.background(FlosiPremiumBrush)) {
            Box(Modifier.size(180.dp).offset(x = (-38).dp, y = (-72).dp).background(Color.White.copy(alpha = .035f), CircleShape))
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
        }
    }
}

@Composable
fun Metric(label: String, value: String, tone: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(localizedLegacyText(label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(3.dp))
        Text(value, color = tone, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun ActionRow(title: String, subtitle: String = "", value: String? = null, accent: Color = FlosiPurple, onClick: (() -> Unit)? = null) {
    val rowModifier = if (onClick != null) Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 9.dp) else Modifier.fillMaxWidth().padding(vertical = 9.dp)
    Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(42.dp).background(accent.copy(alpha = .10f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(10.dp).background(accent, CircleShape))
        }
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(localizedLegacyText(title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            if (subtitle.isNotBlank()) Text(localizedLegacyText(subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        if (value != null) Text(value, fontWeight = FontWeight.ExtraBold, color = accent, fontSize = 13.sp)
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(localizedLegacyText(title), modifier = Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
        if (action != null) {
            Surface(shape = RoundedCornerShape(50), color = FlosiPurpleSoft, modifier = Modifier.clickable(onClick = onAction)) {
                Text(localizedLegacyText(action), color = FlosiPurpleDeep, modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun magnitudeText(value:Long):String = if(value==Long.MIN_VALUE) BigInteger.valueOf(value).abs().toString() else kotlin.math.abs(value).toString()

fun moneyText(value: Long, currencyCode: String = "IQD"): String {
    val code = currencyCode.trim().uppercase()
    if(value==Long.MIN_VALUE) return "${magnitudeText(value)} $code"
    val magnitude=kotlin.math.abs(value)
    return runCatching {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        format.currency = Currency.getInstance(code)
        format.maximumFractionDigits = if (code in setOf("IQD","JPY","KRW")) 0 else 2
        format.minimumFractionDigits = 0
        format.format(magnitude)
    }.getOrElse { "%,d %s".format(Locale.getDefault(), magnitude, code) }
}

fun signedMoney(value: Long, currencyCode: String = "IQD"): String = (if (value >= 0) "+" else "−") + moneyText(value,currencyCode)
