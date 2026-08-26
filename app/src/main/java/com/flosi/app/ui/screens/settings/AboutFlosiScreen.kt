package com.flosi.app.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flosi.app.BuildConfig
import com.flosi.app.R
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*

@Composable
fun AboutFlosiScreen(onBack: () -> Unit) {
    FlosiPage(localizedLegacyText("حول Flosi"), localizedLegacyText("الهوية، الإصدار والحقوق"), onBack = onBack) {
        CardBox {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.flosi_brand_mark),
                    contentDescription = "Flosi logo",
                    modifier = Modifier.size(92.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text("Flosi", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Manage Today. Grow Tomorrow.", color = FlosiPurple, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(5.dp))
                Text(localizedLegacyText("دبّر يومك، وابني باچر."), textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(localizedLegacyText("إدارة مالية ذكية وبسيطة."), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        CardBox {
            Metric(localizedLegacyText("الإصدار"), BuildConfig.VERSION_NAME, FlosiPurple)
            Spacer(Modifier.height(12.dp))
            Text("Developed by Yam Studio", fontWeight = FontWeight.Bold)
            Text("Wana84.com", color = FlosiPurple)
            Spacer(Modifier.height(12.dp))
            Text("© 2026 Yam Studio. All Rights Reserved.", style = MaterialTheme.typography.bodySmall)
            Text(localizedLegacyText("© 2026 Yam Studio. جميع الحقوق محفوظة."), style = MaterialTheme.typography.bodySmall)
        }
        CardBox {
            Text(localizedLegacyText("القانون والشفافية"), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(localizedLegacyText("سياسة الخصوصية • شروط الاستخدام • تراخيص المصادر المفتوحة"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
