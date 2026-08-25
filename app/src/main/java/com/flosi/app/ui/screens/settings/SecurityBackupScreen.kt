package com.flosi.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences
import kotlinx.coroutines.launch

@Composable
fun SecurityBackupScreen(onBack:()->Unit,onBackups:()->Unit){
    val prefs=rememberFlosiPreferences();val state by prefs.state.collectAsState(initial=FlosiPreferencesState());val scope=rememberCoroutineScope();val lang=LocalFlosiLanguage.current
    fun s(ar:String,en:String)=if(lang=="ar")ar else en
    FlosiPage(s("الأمان والخصوصية","Security & privacy"),s("حماية أموالك وبياناتك","Protect your money and data"),onBack){
        PremiumCard{
            Text(s("درع Flosi","Flosi Shield"),color=androidx.compose.ui.graphics.Color.White.copy(alpha=.6f),fontWeight=FontWeight.Bold)
            Text(if(state.biometricLock||state.hideRecents)s("الحماية مفعّلة","Protection is active") else s("فعّل طبقات الحماية الأساسية","Enable essential protection"),color=androidx.compose.ui.graphics.Color.White,fontWeight=FontWeight.Black,style=MaterialTheme.typography.headlineSmall)
            Text(s("البصمة، إخفاء المعاينات، والنسخ المشفّر كلها مستقلة وتقدر تتحكم بيها.","Biometrics, private previews, and encrypted backups are independent controls."),color=androidx.compose.ui.graphics.Color.White.copy(alpha=.65f))
        }
        CardBox{
            SecurityToggle(s("قفل بالبصمة","Biometric lock"),s("يطلب التحقق عند فتح Flosi","Require authentication when opening Flosi"),state.biometricLock){scope.launch{prefs.setBiometric(it)}}
            HorizontalDivider(color=MaterialTheme.colorScheme.outline)
            SecurityToggle(s("حماية الشاشة والمعاينات","Protect screen & previews"),s("يمنع لقطات الشاشة ويخفي محتوى Flosi من Recent Apps","Blocks screenshots and hides Flosi content from Recent Apps"),state.hideRecents){scope.launch{prefs.setHideRecents(it)}}
            HorizontalDivider(color=MaterialTheme.colorScheme.outline)
            SecurityToggle(s("ملخص يومي ذكي","Smart daily summary"),s("يعرض مصروف اليوم والاستحقاقات القريبة فقط","Shows today's spending and upcoming dues only"),state.dailySummaryEnabled){scope.launch{prefs.setDailySummary(it)}}
        }
        SectionTitle(s("النسخ الاحتياطي","Backup"))
        Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),shape=RoundedCornerShape(24.dp),onClick=onBackups){Column(Modifier.padding(18.dp)){Text(s("نسخة Flosi المشفّرة","Encrypted Flosi backup"),fontWeight=FontWeight.ExtraBold);Spacer(Modifier.height(5.dp));Text(s("AES-GCM + فحص سلامة قبل الاسترجاع + اختيار Google Drive من نافذة ملفات أندرويد.","AES-GCM + integrity verification before restore + Google Drive through Android's file picker."),color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(10.dp));Text(s("إدارة النسخ الاحتياطية ←","Manage backups →"),color=FlosiPurple,fontWeight=FontWeight.Bold)}}
        CardBox{Text(s("نصيحة خصوصية: فعّل حماية الشاشة إذا تستخدم Flosi بأماكن عامة أو جهازك يعرض معاينات التطبيقات الأخيرة.","Privacy tip: enable screen protection if you use Flosi in public or your device shows recent-app previews."),color=MaterialTheme.colorScheme.onSurfaceVariant)}
    }
}

@Composable private fun SecurityToggle(title:String,subtitle:String,checked:Boolean,onChange:(Boolean)->Unit){
    Row(Modifier.fillMaxWidth().padding(vertical=4.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){
        Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.ExtraBold);Text(subtitle,color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)}
        Switch(checked=checked,onCheckedChange=onChange)
    }
}
