package com.flosi.app.ui.screens.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flosi.app.i18n.FlosiLaunchCopy
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.HomeViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import kotlin.math.max

@Composable fun AnalyticsScreen(onBack:()->Unit){
 val vm:HomeViewModel=flosiViewModel();val state by vm.state.collectAsState();val currency=state.dashboard.baseCurrency;val lang=LocalFlosiLanguage.current
 fun c(key:String,vararg values:Pair<String,Any?>)=FlosiLaunchCopy.text(lang,key,*values)
 val income=state.dashboard.monthIncome;val expense=state.dashboard.monthExpense;val net=income-expense;val ratio=if(income>0)(expense.toFloat()/income).coerceIn(0f,1.5f) else 0f
 FlosiPage(flosiText("analytics"),c("analytics_sub"),onBack){
  if(state.dashboard.hasUnconvertedCurrencies) CardBox{Text(c("some_currencies_excluded","currencies" to state.dashboard.unconvertedCurrencies.joinToString()),color=FlosiOrange)}
  PremiumCard{
   Text(c("monthly_health"),color=Color.White.copy(alpha=.58f),fontSize=11.sp)
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(signedMoney(net,currency),color=Color.White,fontSize=29.sp,fontWeight=FontWeight.Black);Text(if(net>=0)c("above_break_even") else c("spending_above_income"),color=if(net>=0)Color(0xFF7BE6BB) else Color(0xFFFF9EA6),fontSize=10.sp,fontWeight=FontWeight.Bold)};SpendRing(ratio)}
   Spacer(Modifier.height(12.dp));Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){MiniMetric(flosiText("income"),income,currency,FlosiGreen,Modifier.weight(1f));MiniMetric(flosiText("expense"),expense,currency,Color(0xFFFF8D97),Modifier.weight(1f))}
  }
  SectionTitle(c("spending_pulse"))
  CardBox{SpendingBars(state.topCategories.take(5).map{it.categoryName to it.amount});Text(c("top_spending_distribution"),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=10.sp)}
  SectionTitle("Flosi Intelligence ✦")
  IntelligenceCard(income,expense,state.topCategories.firstOrNull()?.categoryName,lang)
  SectionTitle(localizedLegacyText("أعلى التصنيفات"))
  CardBox{if(state.topCategories.isEmpty())Text(c("add_expenses_unlock"),color=MaterialTheme.colorScheme.onSurfaceVariant) else state.topCategories.take(6).forEachIndexed{index,it->ActionRow(it.categoryName,if(index==0)c("top_this_month") else localizedLegacyText("هذا الشهر"),moneyText(it.amount,currency),listOf(FlosiOrange,FlosiBlue,FlosiPurple,FlosiGreen)[index%4])}}
 }
}

@Composable private fun SpendRing(ratio:Float){val animated by animateFloatAsState(ratio.coerceIn(0f,1f),animationSpec=tween(900),label="spendRing");Box(Modifier.size(82.dp),contentAlignment=Alignment.Center){Canvas(Modifier.fillMaxSize()){drawArc(Color.White.copy(alpha=.10f),-90f,360f,false,style=Stroke(8.dp.toPx(),cap=StrokeCap.Round));drawArc(if(ratio<.7f)FlosiGreen else if(ratio<1f)FlosiOrange else FlosiRed,-90f,360f*animated,false,style=Stroke(8.dp.toPx(),cap=StrokeCap.Round))};Text("${(ratio*100).toInt()}%",color=Color.White,fontWeight=FontWeight.Black,fontSize=14.sp)}}
@Composable private fun MiniMetric(label:String,value:Long,currency:String,accent:Color,modifier:Modifier){Column(modifier){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(7.dp).background(accent,CircleShape));Spacer(Modifier.width(6.dp));Text(label,color=Color.White.copy(alpha=.55f),fontSize=9.sp)};Text(moneyText(value,currency),color=Color.White,fontWeight=FontWeight.Bold,fontSize=14.sp)}}
@Composable private fun SpendingBars(items:List<Pair<String,Long>>){val maxValue=max(1L,items.maxOfOrNull{it.second}?:1L);Column(verticalArrangement=Arrangement.spacedBy(12.dp)){if(items.isEmpty())Text("—",color=MaterialTheme.colorScheme.onSurfaceVariant);items.forEachIndexed{index,(name,value)->val target=(value.toFloat()/maxValue).coerceIn(.04f,1f);val progress by animateFloatAsState(target,tween(800+index*100),label="bar$index");Column{Row(Modifier.fillMaxWidth()){Text(name,Modifier.weight(1f),fontSize=10.sp,fontWeight=FontWeight.SemiBold);Text("${(target*100).toInt()}%",fontSize=9.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Spacer(Modifier.height(5.dp));Canvas(Modifier.fillMaxWidth().height(9.dp)){drawRoundRect(Color(0xFFEDE9F2),size=size,cornerRadius=androidx.compose.ui.geometry.CornerRadius(size.height/2));drawRoundRect(listOf(FlosiPurple,FlosiBlue,FlosiOrange,FlosiGreen)[index%4],size=Size(size.width*progress,size.height),cornerRadius=androidx.compose.ui.geometry.CornerRadius(size.height/2))}}}}}
@Composable private fun IntelligenceCard(income:Long,expense:Long,top:String?,lang:String){val percent=if(income>0)(expense*100/income).toInt() else 0;val text=when(lang){
 "ar"->when{income<=0->"أضف دخلك الشهري حتى أگدر أقيس صحتك المالية وأعطيك توقعات أدق.";expense>income->"مصروفك تجاوز دخلك هذا الشهر. راجع ${top?:"أعلى التصنيفات"} أولاً وخفف الالتزامات غير الضرورية.";percent>=70->"استهلكت $percent% من دخل الشهر. بعدك ضمن السيطرة، بس راقب ${top?:"أعلى مصروف"}.";else->"استهلكت $percent% من دخلك. وضعك مريح حالياً، واستمرار نفس النسق يعطيك مساحة ادخار أفضل."}
 "tr"->when{income<=0->"Finansal sağlığını ve daha iyi tahminleri görmek için aylık gelirini ekle.";expense>income->"Bu ay harcamaların gelirini geçti. Önce ${top?:"en yüksek kategorilerini"} gözden geçir.";percent>=70->"Aylık gelirinin %$percent kadarını kullandın. Kontrol hâlâ sende; ${top?:"en yüksek harcamanı"} izle.";else->"Gelirinin %$percent kadarını kullandın. Şu anda rahat bir alandasın ve tasarruf için yerin var."}
 "fr"->when{income<=0->"Ajoutez vos revenus mensuels pour mesurer votre santé financière et améliorer les prévisions.";expense>income->"Vos dépenses dépassent vos revenus ce mois-ci. Examinez d’abord ${top?:"vos principales catégories"}.";percent>=70->"Vous avez utilisé $percent% de vos revenus mensuels. La situation reste gérable, mais surveillez ${top?:"vos principales dépenses"}.";else->"Vous avez utilisé $percent% de vos revenus. Votre situation est confortable avec une marge d’épargne."}
 "de"->when{income<=0->"Füge dein Monatseinkommen hinzu, um finanzielle Gesundheit und bessere Prognosen zu sehen.";expense>income->"Deine Ausgaben liegen diesen Monat über dem Einkommen. Prüfe zuerst ${top?:"deine Top-Kategorien"}.";percent>=70->"Du hast $percent% deines Monatseinkommens verbraucht. Noch ist es gut steuerbar; beobachte ${top?:"deine höchsten Ausgaben"}.";else->"Du hast $percent% deines Einkommens verbraucht. Deine Lage ist derzeit komfortabel und bietet Sparspielraum."}
 "es"->when{income<=0->"Añade tus ingresos mensuales para medir tu salud financiera y obtener mejores previsiones.";expense>income->"Tus gastos superaron tus ingresos este mes. Revisa primero ${top?:"tus principales categorías"}.";percent>=70->"Has usado $percent% de tus ingresos mensuales. Sigue siendo manejable, pero vigila ${top?:"tus mayores gastos"}.";else->"Has usado $percent% de tus ingresos. Estás en una zona cómoda con margen para ahorrar."}
 else->when{income<=0->"Add monthly income to unlock financial health and better forecasts.";expense>income->"Spending has passed income this month. Review ${top?:"your top categories"} first.";percent>=70->"You've used $percent% of monthly income. Still manageable, but watch ${top?:"your top spending"}.";else->"You've used $percent% of income. You're in a comfortable zone with room to save."}
};Surface(color=FlosiPurpleSoft,shape=RoundedCornerShape(26.dp)){Row(Modifier.padding(18.dp)){Box(Modifier.size(42.dp).background(Color.White.copy(alpha=.75f),CircleShape),contentAlignment=Alignment.Center){Text("✦",color=FlosiPurple,fontSize=18.sp,fontWeight=FontWeight.Black)};Spacer(Modifier.width(12.dp));Text(text,Modifier.weight(1f),color=MaterialTheme.colorScheme.onSurface,fontSize=11.sp,lineHeight=17.sp)}}}
