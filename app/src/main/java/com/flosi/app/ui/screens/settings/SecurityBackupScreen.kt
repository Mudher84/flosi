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
    fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=when(lang){"ar"->ar;"tr"->tr;"fr"->fr;"de"->de;"es"->es;else->en}
    FlosiPage(s("الأمان والخصوصية","Security & privacy","Güvenlik ve gizlilik","Sécurité et confidentialité","Sicherheit & Datenschutz","Seguridad y privacidad"),s("حماية أموالك وبياناتك","Protect your money and data","Paranı ve verilerini koru","Protégez votre argent et vos données","Schütze dein Geld und deine Daten","Protege tu dinero y tus datos"),onBack){
        PremiumCard{
            Text(s("درع Flosi","Flosi Shield","Flosi Kalkanı","Bouclier Flosi","Flosi-Schutz","Escudo Flosi"),color=androidx.compose.ui.graphics.Color.White.copy(alpha=.6f),fontWeight=FontWeight.Bold)
            Text(if(state.biometricLock||state.hideRecents)s("الحماية مفعّلة","Protection is active","Koruma etkin","La protection est active","Schutz ist aktiv","La protección está activa") else s("فعّل طبقات الحماية الأساسية","Enable essential protection","Temel korumayı etkinleştir","Activez la protection essentielle","Aktiviere den Basisschutz","Activa la protección esencial"),color=androidx.compose.ui.graphics.Color.White,fontWeight=FontWeight.Black,style=MaterialTheme.typography.headlineSmall)
            Text(s("البصمة، إخفاء المعاينات، والنسخ المشفّر كلها مستقلة وتقدر تتحكم بيها.","Biometrics, private previews, and encrypted backups are independent controls.","Biyometri, gizli önizlemeler ve şifreli yedekler birbirinden bağımsızdır.","La biométrie, les aperçus privés et les sauvegardes chiffrées sont des contrôles indépendants.","Biometrie, private Vorschauen und verschlüsselte Backups lassen sich unabhängig steuern.","La biometría, las vistas privadas y las copias cifradas se controlan por separado."),color=androidx.compose.ui.graphics.Color.White.copy(alpha=.65f))
        }
        CardBox{
            SecurityToggle(s("قفل بالبصمة","Biometric lock","Biyometrik kilit","Verrouillage biométrique","Biometrische Sperre","Bloqueo biométrico"),s("يطلب التحقق عند فتح Flosi","Require authentication when opening Flosi","Flosi açılırken kimlik doğrulama ister","Demande une authentification à l’ouverture de Flosi","Verlangt beim Öffnen von Flosi eine Authentifizierung","Solicita autenticación al abrir Flosi"),state.biometricLock){scope.launch{prefs.setBiometric(it)}}
            HorizontalDivider(color=MaterialTheme.colorScheme.outline)
            SecurityToggle(s("حماية الشاشة والمعاينات","Protect screen & previews","Ekranı ve önizlemeleri koru","Protéger l’écran et les aperçus","Bildschirm & Vorschauen schützen","Proteger pantalla y vistas previas"),s("يمنع لقطات الشاشة ويخفي محتوى Flosi من Recent Apps","Blocks screenshots and hides Flosi content from Recent Apps","Ekran görüntülerini engeller ve Flosi içeriğini son uygulamalardan gizler","Bloque les captures d’écran et masque Flosi dans les apps récentes","Blockiert Screenshots und blendet Flosi in den letzten Apps aus","Bloquea capturas y oculta Flosi de las apps recientes"),state.hideRecents){scope.launch{prefs.setHideRecents(it)}}
            HorizontalDivider(color=MaterialTheme.colorScheme.outline)
            SecurityToggle(s("ملخص يومي ذكي","Smart daily summary","Akıllı günlük özet","Résumé quotidien intelligent","Intelligente Tagesübersicht","Resumen diario inteligente"),s("يعرض مصروف اليوم والاستحقاقات القريبة فقط","Shows today's spending and upcoming dues only","Yalnızca bugünkü harcamaları ve yaklaşan ödemeleri gösterir","Affiche uniquement les dépenses du jour et les échéances proches","Zeigt nur heutige Ausgaben und anstehende Fälligkeiten","Muestra solo el gasto de hoy y próximos vencimientos"),state.dailySummaryEnabled){scope.launch{prefs.setDailySummary(it)}}
        }
        SectionTitle(s("النسخ الاحتياطي","Backup","Yedekleme","Sauvegarde","Backup","Copia de seguridad"))
        Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),shape=RoundedCornerShape(24.dp),onClick=onBackups){Column(Modifier.padding(18.dp)){Text(s("نسخة Flosi المشفّرة","Encrypted Flosi backup","Şifreli Flosi yedeği","Sauvegarde Flosi chiffrée","Verschlüsseltes Flosi-Backup","Copia cifrada de Flosi"),fontWeight=FontWeight.ExtraBold);Spacer(Modifier.height(5.dp));Text(s("AES-GCM + فحص سلامة قبل الاسترجاع + اختيار Google Drive من نافذة ملفات أندرويد.","AES-GCM + integrity verification before restore + Google Drive through Android's file picker.","AES-GCM + geri yüklemeden önce bütünlük kontrolü + Android dosya seçicisinden Google Drive.","AES-GCM + vérification d’intégrité avant restauration + Google Drive via le sélecteur de fichiers Android.","AES-GCM + Integritätsprüfung vor Wiederherstellung + Google Drive über die Android-Dateiauswahl.","AES-GCM + verificación de integridad antes de restaurar + Google Drive desde el selector de archivos de Android."),color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(10.dp));Text(s("إدارة النسخ الاحتياطية ←","Manage backups →","Yedekleri yönet →","Gérer les sauvegardes →","Backups verwalten →","Gestionar copias →"),color=FlosiPurple,fontWeight=FontWeight.Bold)}}
        CardBox{Text(s("نصيحة خصوصية: فعّل حماية الشاشة إذا تستخدم Flosi بأماكن عامة أو جهازك يعرض معاينات التطبيقات الأخيرة.","Privacy tip: enable screen protection if you use Flosi in public or your device shows recent-app previews.","Gizlilik ipucu: Flosi’yi halka açık yerlerde kullanıyorsan veya cihazın son uygulama önizlemelerini gösteriyorsa ekran korumasını etkinleştir.","Conseil de confidentialité : activez la protection d’écran si vous utilisez Flosi en public ou si votre appareil affiche les aperçus récents.","Datenschutztipp: Aktiviere den Bildschirmschutz, wenn du Flosi öffentlich nutzt oder dein Gerät Vorschauen letzter Apps zeigt.","Consejo de privacidad: activa la protección de pantalla si usas Flosi en público o tu dispositivo muestra vistas de apps recientes."),color=MaterialTheme.colorScheme.onSurfaceVariant)}
    }
}

@Composable private fun SecurityToggle(title:String,subtitle:String,checked:Boolean,onChange:(Boolean)->Unit){
    Row(Modifier.fillMaxWidth().padding(vertical=4.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){
        Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.ExtraBold);Text(subtitle,color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)}
        Switch(checked=checked,onCheckedChange=onChange)
    }
}
