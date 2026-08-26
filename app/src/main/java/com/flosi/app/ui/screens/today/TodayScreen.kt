package com.flosi.app.ui.screens.today

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.finance.InsightSeverity
import com.flosi.app.finance.SmartInsight
import com.flosi.app.finance.SmartInsights
import com.flosi.app.i18n.FlosiLaunchCopy
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.HomeViewModel
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun TodayScreen(onActivity: () -> Unit,onNotifications: () -> Unit,onCommitments: () -> Unit) {
    val vm: HomeViewModel = flosiViewModel(); val planningVm: PlanningViewModel = flosiViewModel()
    val state by vm.state.collectAsState(); val commitments by planningVm.commitments.collectAsState(); val accounts by planningVm.accounts.collectAsState(); val prefs by planningVm.preferences.collectAsState()
    val currency=state.dashboard.baseCurrency; val netMonth=state.dashboard.monthIncome-state.dashboard.monthExpense; val safeToSpend=max(0L,state.dashboard.totalBalance-state.reservedCommitments-state.reservedGoals); val language=LocalFlosiLanguage.current
    fun c(key:String,vararg values:Pair<String,Any?>)=FlosiLaunchCopy.text(language,key,*values)
    val now=System.currentTimeMillis(); val weekEnd=now+7L*86_400_000L; val accountMap=remember(accounts){accounts.associateBy{it.id}}
    fun convertedAmount(amount:Long,accountId:Long?):Long? { val source=accountId?.let(accountMap::get)?.currency?:currency; return CurrencyConverter.convert(amount,source,currency,prefs.exchangeRates) }
    val overdue=commitments.filter{it.dueAt<now}.sortedBy{it.dueAt}; val upcoming=commitments.filter{it.dueAt>=now&&it.dueAt<=weekEnd}.sortedBy{it.dueAt}
    val overdueTotal=overdue.mapNotNull{convertedAmount(it.amount,it.accountId)}.fold(0L){a,b->runCatching{Math.addExact(a,b)}.getOrElse{Long.MAX_VALUE}}; val upcomingTotal=upcoming.mapNotNull{convertedAmount(it.amount,it.accountId)}.fold(0L){a,b->runCatching{Math.addExact(a,b)}.getOrElse{Long.MAX_VALUE}}
    val calendar=Calendar.getInstance(); val remainingDays=(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)-calendar.get(Calendar.DAY_OF_MONTH)+1).coerceAtLeast(1); val safeDaily=safeToSpend/remainingDays
    val insights=remember(state.dashboard.monthIncome,state.dashboard.monthExpense,safeToSpend,safeDaily,overdue.size,overdueTotal,upcoming.size,upcomingTotal,currency,remainingDays){SmartInsights.build(state.dashboard.monthIncome,state.dashboard.monthExpense,safeToSpend,safeDaily,overdue.size,overdueTotal,upcoming.size,upcomingTotal,currency,remainingDays)}

    LazyColumn(modifier=Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding(),contentPadding=PaddingValues(horizontal=20.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
        item{PremiumHeader(language,onNotifications)}
        if(state.dashboard.hasUnconvertedCurrencies)item{Surface(color=FlosiOrange.copy(alpha=.10f),shape=RoundedCornerShape(20.dp)){Text(c("excluded_totals","currencies" to state.dashboard.unconvertedCurrencies.joinToString()),Modifier.padding(14.dp),color=FlosiOrange,fontSize=11.sp,fontWeight=FontWeight.SemiBold)}}
        item{FlosiPulseHero(state.dashboard.totalBalance,state.dashboard.monthIncome,state.dashboard.monthExpense,netMonth,currency,language,overdue.isNotEmpty())}
        item{MoneyPulse(safeToSpend,safeDaily,remainingDays,currency,language)}
        item{Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){SnapshotCard(localizedLegacyText("المقبوض اليوم"),state.dashboard.todayIncome,true,currency,Modifier.weight(1f));SnapshotCard(localizedLegacyText("المصروف اليوم"),state.dashboard.todayExpense,false,currency,Modifier.weight(1f))}}
        item{DueSoonCard(overdue,upcoming,overdueTotal,upcomingTotal,currency,language,onCommitments)}
        if(insights.isNotEmpty()) item{SmartInsightsPanel(insights,language)}
        item{SectionTitle(localizedLegacyText("آخر الحركات"),localizedLegacyText("عرض الكل"),onActivity)}
        item{CardBox{if(state.recent.isEmpty())Text(localizedLegacyText("ماكو حركات بعد"),color=MaterialTheme.colorScheme.onSurfaceVariant) else state.recent.take(4).forEachIndexed{index,tx->val incoming=tx.kind in listOf("income","invoice_payment","debt_received");val neutral=tx.kind in listOf("transfer_in","transfer_out","goal_saving");val value=when{tx.kind=="goal_saving"->c("reserved","amount" to moneyText(tx.amount,tx.accountCurrency));neutral->moneyText(tx.amount,tx.accountCurrency);incoming->"+"+moneyText(tx.amount,tx.accountCurrency);else->"−"+moneyText(tx.amount,tx.accountCurrency)};ActionRow(tx.title,listOfNotNull(tx.categoryName,tx.accountName).joinToString(" • "),value,when{neutral->FlosiPurple;incoming->FlosiGreen;else->FlosiRed},onActivity);if(index<state.recent.take(4).lastIndex)HorizontalDivider(color=MaterialTheme.colorScheme.outline)}}}
        item{SectionTitle(localizedLegacyText("أعلى المصروفات"))}
        item{CardBox{if(state.topCategories.isEmpty())Text(localizedLegacyText("تظهر بعد تسجيل مصروفات"),color=MaterialTheme.colorScheme.onSurfaceVariant) else{val accents=listOf(FlosiOrange,FlosiBlue,FlosiPurple,FlosiGreen);state.topCategories.take(4).forEachIndexed{index,category->ActionRow(category.categoryName,localizedLegacyText("هذا الشهر"),moneyText(category.amount,currency),accents[index%accents.size])}}}}
        item{Spacer(Modifier.height(8.dp))}
    }
}

@Composable private fun PremiumHeader(language:String,onNotifications:()->Unit){
    val context=LocalContext.current
    val owner=remember{context.getSharedPreferences("flosi_subscription",0).getString("server_role","")=="OWNER"}
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
        Column(Modifier.weight(1f),horizontalAlignment=Alignment.Start){
            Text(FlosiLaunchCopy.text(language,"welcome_to"),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=11.sp,fontWeight=FontWeight.Medium)
            Row(verticalAlignment=Alignment.CenterVertically){
                Text("Flosi",color=MaterialTheme.colorScheme.onBackground,fontSize=30.sp,fontWeight=FontWeight.Black,letterSpacing=(-.8).sp)
                if(owner){Spacer(Modifier.width(8.dp));Surface(color=FlosiPurpleSoft,shape=RoundedCornerShape(50)){Text("OWNER",Modifier.padding(horizontal=8.dp,vertical=4.dp),color=FlosiPurpleDeep,fontSize=8.sp,fontWeight=FontWeight.Black)}}
            }
        }
        Surface(Modifier.size(48.dp).clickable(onClick=onNotifications),CircleShape,MaterialTheme.colorScheme.surface,shadowElevation=4.dp,tonalElevation=1.dp){Box(contentAlignment=Alignment.Center){Icon(Icons.Default.NotificationsNone,localizedLegacyText("الإشعارات"),tint=MaterialTheme.colorScheme.onSurface)}}
    }
}

@Composable private fun FlosiPulseHero(totalBalance:Long,monthIncome:Long,monthExpense:Long,netMonth:Long,currency:String,language:String,hasOverdue:Boolean){
    val ratio=if(monthIncome>0) monthExpense.toDouble()/monthIncome.toDouble() else 0.0
    val status=when{hasOverdue||netMonth<0->"critical";ratio>=.8->"warning";monthIncome>0&&ratio<=.55->"great";else->"stable"}
    val accent=when(status){"critical"->Color(0xFFFF9EA6);"warning"->Color(0xFFFFCA7A);"great"->Color(0xFF7BE6BB);else->Color(0xFFC7B8FF)}
    val statusText=when(language){
        "ar"->when(status){"critical"->"يحتاج انتباه";"warning"->"راقب الصرف";"great"->"وضع ممتاز";else->"وضع مستقر"}
        "tr"->when(status){"critical"->"Dikkat gerekli";"warning"->"Harcamayı izle";"great"->"Harika durum";else->"Dengeli durum"}
        "fr"->when(status){"critical"->"Attention requise";"warning"->"Surveillez les dépenses";"great"->"Excellente santé";else->"Situation stable"}
        "de"->when(status){"critical"->"Aufmerksamkeit nötig";"warning"->"Ausgaben beobachten";"great"->"Sehr gute Lage";else->"Stabile Lage"}
        "es"->when(status){"critical"->"Requiere atención";"warning"->"Vigila tus gastos";"great"->"Excelente estado";else->"Estado estable"}
        else->when(status){"critical"->"Needs attention";"warning"->"Watch spending";"great"->"Excellent health";else->"Stable position"}
    }
    val infinite=rememberInfiniteTransition(label="flosiPulse")
    val pulse by infinite.animateFloat(1f,1.16f,infiniteRepeatable(tween(if(status=="critical")900 else 1700,easing=FastOutSlowInEasing),RepeatMode.Reverse),label="pulse")
    val glow by infinite.animateFloat(.28f,.62f,infiniteRepeatable(tween(1500,easing=FastOutSlowInEasing),RepeatMode.Reverse),label="glow")
    var started by remember{mutableStateOf(false)}
    LaunchedEffect(totalBalance){started=true}
    val animatedBalance by animateFloatAsState(if(started)totalBalance.toFloat() else 0f,tween(900,easing=FastOutSlowInEasing),label="balanceCount")

    PremiumCard{
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
            Column(Modifier.weight(1f),horizontalAlignment=Alignment.Start){
                Text(localizedLegacyText("إجمالي أموالك"),color=Color.White.copy(alpha=.60f),fontSize=11.sp)
                Spacer(Modifier.height(5.dp))
                Text(moneyText(animatedBalance.toLong(),currency),color=Color.White,fontSize=34.sp,fontWeight=FontWeight.Black,letterSpacing=(-.7).sp)
                Spacer(Modifier.height(8.dp))
                Surface(color=accent.copy(alpha=.14f),shape=RoundedCornerShape(50)){
                    Row(Modifier.padding(horizontal=10.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically){
                        Box(Modifier.size(7.dp).scale(pulse).alpha(glow).background(accent,CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(statusText,color=accent,fontSize=10.sp,fontWeight=FontWeight.ExtraBold)
                    }
                }
            }
            Box(Modifier.size(78.dp),contentAlignment=Alignment.Center){
                Box(Modifier.size(68.dp).scale(pulse).alpha(.18f).background(accent,CircleShape))
                Box(Modifier.size(48.dp).background(accent.copy(alpha=.18f),CircleShape),contentAlignment=Alignment.Center){Text("✦",color=accent,fontSize=22.sp,fontWeight=FontWeight.Black)}
            }
        }
        Spacer(Modifier.height(13.dp))
        Surface(color=Color.White.copy(alpha=.06f),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){
            Row(Modifier.padding(horizontal=12.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically){
                Icon(Icons.Default.Visibility,null,tint=Color.White.copy(alpha=.70f),modifier=Modifier.size(14.dp));Spacer(Modifier.width(6.dp))
                Text(FlosiLaunchCopy.text(language,if(netMonth>=0)"monthly_net_up" else "monthly_net_down","amount" to moneyText(netMonth,currency)),Modifier.weight(1f),color=Color.White.copy(alpha=.78f),fontSize=10.sp,fontWeight=FontWeight.SemiBold)
                Text(currency,color=Color.White.copy(alpha=.70f),fontSize=9.sp,fontWeight=FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(11.dp));HorizontalDivider(color=Color.White.copy(alpha=.08f))
        Row(Modifier.fillMaxWidth().padding(top=7.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)){HeroStat(localizedLegacyText("دخل هذا الشهر"),monthIncome,currency,FlosiGreen,Modifier.weight(1f));HeroStat(localizedLegacyText("مصروف هذا الشهر"),monthExpense,currency,Color(0xFFFF8D97),Modifier.weight(1f))}
    }
}

@Composable private fun HeroStat(label:String,value:Long,currency:String,accent:Color,modifier:Modifier=Modifier){Column(modifier,horizontalAlignment=Alignment.Start){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(7.dp).background(accent,CircleShape));Spacer(Modifier.width(6.dp));Text(label,color=Color.White.copy(alpha=.55f),fontSize=9.sp)};Spacer(Modifier.height(3.dp));Text(moneyText(value,currency),color=Color.White,fontSize=14.sp,fontWeight=FontWeight.Bold)}}
@Composable private fun MoneyPulse(safeToSpend:Long,safeDaily:Long,days:Int,currency:String,language:String){Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),shape=RoundedCornerShape(28.dp),elevation=CardDefaults.cardElevation(defaultElevation=2.dp)){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(54.dp).background(FlosiPurpleSoft,CircleShape),contentAlignment=Alignment.Center){Text("◎",color=FlosiPurple,fontSize=25.sp,fontWeight=FontWeight.Black)};Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f),horizontalAlignment=Alignment.Start){Text(FlosiLaunchCopy.text(language,"safe_zone"),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=10.sp,fontWeight=FontWeight.SemiBold);Text(moneyText(safeToSpend,currency),color=MaterialTheme.colorScheme.onSurface,fontSize=21.sp,fontWeight=FontWeight.Black);Text(FlosiLaunchCopy.text(language,"safe_daily","amount" to moneyText(safeDaily,currency),"days" to days),color=FlosiPurple,fontSize=10.sp,fontWeight=FontWeight.Bold)}}}}
@Composable private fun SnapshotCard(title:String,value:Long,positive:Boolean,currency:String,modifier:Modifier=Modifier){val accent=if(positive)FlosiGreen else FlosiRed;Card(modifier,shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),elevation=CardDefaults.cardElevation(defaultElevation=2.dp)){Column(Modifier.padding(16.dp),horizontalAlignment=Alignment.Start){Surface(Modifier.size(38.dp),RoundedCornerShape(13.dp),accent.copy(alpha=.10f)){Box(contentAlignment=Alignment.Center){Icon(if(positive)Icons.Default.TrendingUp else Icons.Default.TrendingDown,null,tint=accent,modifier=Modifier.size(18.dp))}};Spacer(Modifier.height(12.dp));Text(title,color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=10.sp);Text(moneyText(value,currency),color=MaterialTheme.colorScheme.onSurface,fontWeight=FontWeight.Black,fontSize=17.sp)}}}
@Composable private fun DueSoonCard(overdue:List<com.flosi.app.data.local.entity.CommitmentEntity>,upcoming:List<com.flosi.app.data.local.entity.CommitmentEntity>,overdueTotal:Long,upcomingTotal:Long,currency:String,language:String,onOpen:()->Unit){val fmt=remember(language){SimpleDateFormat(if(language=="ar")"dd/MM" else "MMM d",Locale.getDefault())};Card(modifier=Modifier.fillMaxWidth().clickable(onClick=onOpen),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),shape=RoundedCornerShape(28.dp),elevation=CardDefaults.cardElevation(defaultElevation=2.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(FlosiLaunchCopy.text(language,"upcoming_dues"),fontSize=16.sp,fontWeight=FontWeight.ExtraBold,modifier=Modifier.weight(1f));Surface(color=FlosiPurpleSoft,shape=RoundedCornerShape(50)){Text(FlosiLaunchCopy.text(language,"seven_days"),Modifier.padding(horizontal=9.dp,vertical=5.dp),color=FlosiPurpleDeep,fontSize=9.sp,fontWeight=FontWeight.Bold)}};if(overdue.isEmpty()&&upcoming.isEmpty())Text(FlosiLaunchCopy.text(language,"all_clear"),color=FlosiGreen,fontSize=11.sp);if(overdue.isNotEmpty())ActionRow(FlosiLaunchCopy.text(language,"overdue_count","count" to overdue.size),"",moneyText(overdueTotal,currency),FlosiRed);if(upcoming.isNotEmpty())ActionRow(FlosiLaunchCopy.text(language,"coming_count","count" to upcoming.size),"",moneyText(upcomingTotal,currency),FlosiOrange);(overdue+upcoming).take(2).forEach{item->Text("${item.title} • ${fmt.format(Date(item.dueAt))}",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=10.sp)}}}}
@Composable private fun SmartInsightsPanel(insights:List<SmartInsight>,language:String){Column(verticalArrangement=Arrangement.spacedBy(9.dp)){Text(FlosiLaunchCopy.text(language,"smart_insights"),fontSize=16.sp,fontWeight=FontWeight.ExtraBold);insights.forEach{insight->val accent=when(insight.severity){InsightSeverity.POSITIVE->FlosiGreen;InsightSeverity.INFO->FlosiPurple;InsightSeverity.WARNING->FlosiOrange;InsightSeverity.CRITICAL->FlosiRed};Surface(color=accent.copy(alpha=.09f),shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(15.dp),verticalAlignment=Alignment.Top){Box(Modifier.size(34.dp).background(accent.copy(alpha=.14f),CircleShape),contentAlignment=Alignment.Center){Text("✦",color=accent,fontWeight=FontWeight.Black)};Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f),horizontalAlignment=Alignment.Start){Text(insight.title(language),fontWeight=FontWeight.ExtraBold,fontSize=12.sp);Spacer(Modifier.height(3.dp));Text(insight.body(language),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=10.sp,lineHeight=15.sp)}}}}}}
