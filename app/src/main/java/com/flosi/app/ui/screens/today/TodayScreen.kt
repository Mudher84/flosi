package com.flosi.app.ui.screens.today

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.HomeViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import kotlin.math.max

@Composable
fun TodayScreen(onActivity: () -> Unit,onNotifications: () -> Unit) {
    val vm: HomeViewModel = flosiViewModel()
    val state by vm.state.collectAsState()
    val currency=state.dashboard.baseCurrency
    val netMonth = state.dashboard.monthIncome - state.dashboard.monthExpense
    val reservedTotal = state.reservedCommitments + state.reservedGoals
    val safeToSpend = max(0L, state.dashboard.totalBalance - reservedTotal)
    val language=LocalFlosiLanguage.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(FlosiBg).statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f),horizontalAlignment = Alignment.Start) {
                    Text(localizedLegacyText("صباح الخير"), color = FlosiMuted, fontSize = 11.sp)
                    Text("Flosi", color = FlosiText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
                Surface(Modifier.size(44.dp).clickable(onClick = onNotifications),CircleShape,Color.White,shadowElevation = 1.dp) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.NotificationsNone,localizedLegacyText("الإشعارات"),tint = FlosiText) }
                }
            }
        }

        if(state.dashboard.hasUnconvertedCurrencies) item {
            Surface(color=Color(0xFFFFF6E7),shape=RoundedCornerShape(18.dp)) {
                Text(
                    if(language=="ar") "بعض العملات غير داخلة في الإجماليات: ${state.dashboard.unconvertedCurrencies.joinToString()}. أضف أسعار التحويل من الإعدادات."
                    else "Some currencies are excluded from totals: ${state.dashboard.unconvertedCurrencies.joinToString()}. Add exchange rates in settings.",
                    modifier=Modifier.padding(13.dp),color=FlosiOrange,fontSize=11.sp
                )
            }
        }

        item { BalanceHero(state.dashboard.totalBalance,state.dashboard.monthIncome,state.dashboard.monthExpense,netMonth,currency,language) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SnapshotCard(localizedLegacyText("المقبوض اليوم"), state.dashboard.todayIncome, true,currency, Modifier.weight(1f))
                SnapshotCard(localizedLegacyText("المصروف اليوم"), state.dashboard.todayExpense, false,currency, Modifier.weight(1f))
            }
        }
        item { SafeSpendCard(safeToSpend,state.reservedCommitments,state.reservedGoals,currency,language) }
        item { SmartInsightCard(state.dashboard.monthIncome, state.dashboard.monthExpense,language) }
        item { SectionTitle(localizedLegacyText("آخر الحركات"), localizedLegacyText("عرض الكل"), onActivity) }
        item {
            CardBox {
                if (state.recent.isEmpty()) Text(localizedLegacyText("ماكو حركات بعد"), color = FlosiMuted)
                else state.recent.take(4).forEachIndexed { index, tx ->
                    val incoming = tx.kind in listOf("income", "invoice_payment","debt_received")
                    val neutral = tx.kind in listOf("transfer_in","transfer_out","goal_saving")
                    val value = when {
                        tx.kind=="goal_saving" -> if(language=="ar") "حجز ${moneyText(tx.amount,tx.accountCurrency)}" else "Reserved ${moneyText(tx.amount,tx.accountCurrency)}"
                        neutral -> moneyText(tx.amount,tx.accountCurrency)
                        incoming -> "+"+moneyText(tx.amount,tx.accountCurrency)
                        else -> "−"+moneyText(tx.amount,tx.accountCurrency)
                    }
                    ActionRow(
                        title = tx.title,
                        subtitle = listOfNotNull(tx.categoryName, tx.accountName).joinToString(" • "),
                        value = value,
                        accent = when { neutral->FlosiPurple; incoming->FlosiGreen; else->FlosiRed },
                        onClick = onActivity
                    )
                    if (index < state.recent.take(4).lastIndex) HorizontalDivider(color = FlosiLine)
                }
            }
        }
        item { SectionTitle(localizedLegacyText("أعلى المصروفات")) }
        item {
            CardBox {
                if (state.topCategories.isEmpty()) Text(localizedLegacyText("تظهر بعد تسجيل مصروفات"), color = FlosiMuted)
                else {
                    val accents = listOf(FlosiOrange, FlosiBlue, FlosiPurple, FlosiGreen)
                    state.topCategories.take(4).forEachIndexed { index, category ->
                        ActionRow(category.categoryName,localizedLegacyText("هذا الشهر"),moneyText(category.amount,currency),accents[index % accents.size])
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceHero(totalBalance:Long,monthIncome:Long,monthExpense:Long,netMonth:Long,currency:String,language:String) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(FlosiHeroBrush).padding(22.dp)) {
        Box(Modifier.size(180.dp).offset(x = 92.dp, y = (-78).dp).background(Color.White.copy(alpha = .06f), CircleShape))
        Column(Modifier.fillMaxWidth(),horizontalAlignment = Alignment.Start) {
            Row(Modifier.fillMaxWidth(),verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f),horizontalAlignment = Alignment.Start) {
                    Text(localizedLegacyText("إجمالي أموالك"), color = Color.White.copy(alpha = .80f), fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(moneyText(totalBalance,currency), color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if(language=="ar") if (netMonth >= 0) "هذا الشهر موجب ${moneyText(netMonth,currency)}" else "هذا الشهر ناقص ${moneyText(netMonth,currency)}"
                        else if(netMonth>=0) "This month +${moneyText(netMonth,currency)}" else "This month −${moneyText(netMonth,currency)}",
                        color = Color.White.copy(alpha = .80f),fontSize = 10.sp
                    )
                }
                Surface(color = Color.White.copy(alpha = .15f),shape = RoundedCornerShape(50)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp),verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility,null,tint = Color.White,modifier = Modifier.size(14.dp));Spacer(Modifier.width(5.dp));Text(currency,color = Color.White,fontSize = 10.sp,fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroStat(localizedLegacyText("دخل هذا الشهر"), monthIncome,currency, Modifier.weight(1f));HeroStat(localizedLegacyText("مصروف هذا الشهر"), monthExpense,currency, Modifier.weight(1f))
            }
        }
    }
}

@Composable private fun HeroStat(label:String,value:Long,currency:String,modifier:Modifier=Modifier){
    Column(modifier.background(Color.White.copy(alpha=.13f),RoundedCornerShape(18.dp)).padding(horizontal=12.dp,vertical=10.dp),horizontalAlignment=Alignment.Start){
        Text(label,color=Color.White.copy(alpha=.72f),fontSize=10.sp);Text(moneyText(value,currency),color=Color.White,fontSize=12.sp,fontWeight=FontWeight.SemiBold)
    }
}

@Composable private fun SnapshotCard(title:String,value:Long,positive:Boolean,currency:String,modifier:Modifier=Modifier){
    val accent=if(positive)FlosiBlue else FlosiRed
    Card(modifier,shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(defaultElevation=1.dp)){
        Column(Modifier.padding(15.dp),horizontalAlignment=Alignment.Start){
            Surface(Modifier.size(36.dp),RoundedCornerShape(12.dp),accent.copy(alpha=.10f)){Box(contentAlignment=Alignment.Center){Icon(if(positive)Icons.Default.TrendingUp else Icons.Default.TrendingDown,null,tint=accent,modifier=Modifier.size(18.dp))}}
            Spacer(Modifier.height(11.dp));Text(title,color=FlosiMuted,fontSize=10.sp);Text(moneyText(value,currency),color=FlosiText,fontWeight=FontWeight.Bold,fontSize=16.sp)
        }
    }
}

@Composable private fun SafeSpendCard(safeToSpend:Long,reservedCommitments:Long,reservedGoals:Long,currency:String,language:String){
    val reservedTotal=reservedCommitments+reservedGoals
    Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(24.dp),elevation=CardDefaults.cardElevation(defaultElevation=1.dp)){
        Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
            Column(Modifier.weight(1f),horizontalAlignment=Alignment.Start){
                Text(localizedLegacyText("المتاح للصرف بأمان"),color=FlosiMuted,fontSize=11.sp);Text(moneyText(safeToSpend,currency),color=FlosiText,fontSize=20.sp,fontWeight=FontWeight.Bold)
                Text(
                    if(reservedTotal>0L) {
                        if(language=="ar") "بعد حجز ${moneyText(reservedCommitments,currency)} للالتزامات و ${moneyText(reservedGoals,currency)} للأهداف"
                        else "After reserving ${moneyText(reservedCommitments,currency)} for commitments and ${moneyText(reservedGoals,currency)} for goals"
                    } else localizedLegacyText("لا توجد مبالغ محجوزة حالياً"),
                    color=if(reservedTotal>0L)FlosiPurple else FlosiGreen,fontSize=10.sp
                )
            }
            Surface(color=FlosiPurpleSoft,shape=CircleShape,modifier=Modifier.size(48.dp)){Box(contentAlignment=Alignment.Center){Text("◎",color=FlosiPurple,fontSize=22.sp,fontWeight=FontWeight.Bold)}}
        }
    }
}

@Composable private fun SmartInsightCard(monthIncome:Long,monthExpense:Long,language:String){
    val ratio=if(monthIncome>0L)monthExpense.toDouble()/monthIncome.toDouble() else 0.0
    val insight=if(language=="ar") when{monthIncome<=0L->"أضف دخلك حتى أحسب وضعك المالي بدقة";ratio<.50->"مصروفك مضبوط، عندك مساحة جيدة للادخار";ratio<.80->"وضعك متوازن، راقب المصاريف غير الضرورية";else->"مصروفك مرتفع مقارنة بالدخل هذا الشهر"}
    else when{monthIncome<=0L->"Add your income so Flosi can calculate your financial position accurately";ratio<.50->"Your spending is controlled and you have good room to save";ratio<.80->"Your position is balanced; keep an eye on non-essential spending";else->"Your spending is high compared with income this month"}
    Surface(color=FlosiDark,shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){
        Row(Modifier.padding(horizontal=15.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically){
            Surface(shape=CircleShape,color=FlosiPurple,modifier=Modifier.size(30.dp)){Box(contentAlignment=Alignment.Center){Text("✦",color=Color.White,fontSize=12.sp)}}
            Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f),horizontalAlignment=Alignment.Start){Text(localizedLegacyText("ملخص فلوسي"),color=Color.White,fontWeight=FontWeight.SemiBold,fontSize=12.sp);Text(insight,color=Color.White.copy(alpha=.62f),fontSize=10.sp)}
            Text(localizedLegacyText("ذكي"),color=Color(0xFFD7C6FF),fontSize=11.sp,fontWeight=FontWeight.Bold)
        }
    }
}
