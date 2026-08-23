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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.HomeViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import kotlin.math.max

@Composable
fun TodayScreen(
    onActivity: () -> Unit,
    onNotifications: () -> Unit
) {
    val vm: HomeViewModel = flosiViewModel()
    val state by vm.state.collectAsState()

    val netMonth = state.dashboard.monthIncome - state.dashboard.monthExpense
    val reservedTotal = state.reservedCommitments + state.reservedGoals
    val safeToSpend = max(0L, state.dashboard.totalBalance - reservedTotal)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FlosiBg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("صباح الخير", color = FlosiMuted, fontSize = 11.sp)
                    Text("فلوسي", color = FlosiText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }

                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(onClick = onNotifications),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = "الإشعارات", tint = FlosiText)
                    }
                }
            }
        }

        item {
            BalanceHero(
                totalBalance = state.dashboard.totalBalance,
                monthIncome = state.dashboard.monthIncome,
                monthExpense = state.dashboard.monthExpense,
                netMonth = netMonth
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SnapshotCard("المقبوض اليوم", state.dashboard.todayIncome, true, Modifier.weight(1f))
                SnapshotCard("المصروف اليوم", state.dashboard.todayExpense, false, Modifier.weight(1f))
            }
        }

        item {
            SafeSpendCard(
                safeToSpend = safeToSpend,
                reservedCommitments = state.reservedCommitments,
                reservedGoals = state.reservedGoals
            )
        }
        item { SmartInsightCard(state.dashboard.monthIncome, state.dashboard.monthExpense) }

        item { SectionTitle("آخر الحركات", "عرض الكل", onActivity) }

        item {
            CardBox {
                if (state.recent.isEmpty()) {
                    Text("ماكو حركات بعد", color = FlosiMuted)
                } else {
                    state.recent.take(4).forEachIndexed { index, tx ->
                        val incoming = tx.kind in listOf("income", "transfer_in", "invoice_payment")
                        ActionRow(
                            title = tx.title,
                            subtitle = listOfNotNull(tx.categoryName, tx.accountName).joinToString(" • "),
                            value = (if (incoming) "+" else "−") + moneyText(tx.amount),
                            accent = if (incoming) FlosiGreen else FlosiRed,
                            onClick = onActivity
                        )
                        if (index < state.recent.take(4).lastIndex) {
                            HorizontalDivider(color = FlosiLine)
                        }
                    }
                }
            }
        }

        item { SectionTitle("أعلى المصروفات") }

        item {
            CardBox {
                if (state.topCategories.isEmpty()) {
                    Text("تظهر بعد تسجيل مصروفات", color = FlosiMuted)
                } else {
                    val accents = listOf(FlosiOrange, FlosiBlue, FlosiPurple, FlosiGreen)
                    state.topCategories.take(4).forEachIndexed { index, category ->
                        ActionRow(
                            title = category.categoryName,
                            subtitle = "هذا الشهر",
                            value = moneyText(category.amount),
                            accent = accents[index % accents.size]
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceHero(
    totalBalance: Long,
    monthIncome: Long,
    monthExpense: Long,
    netMonth: Long
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(FlosiHeroBrush)
            .padding(22.dp)
    ) {
        Box(
            Modifier
                .size(180.dp)
                .offset(x = 92.dp, y = (-78).dp)
                .background(Color.White.copy(alpha = .06f), CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("إجمالي أموالك", color = Color.White.copy(alpha = .80f), fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(moneyText(totalBalance), color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (netMonth >= 0) "هذا الشهر موجب ${moneyText(netMonth)}"
                        else "هذا الشهر ناقص ${moneyText(netMonth)}",
                        color = Color.White.copy(alpha = .80f),
                        fontSize = 10.sp
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = .15f),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("مستقر", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroStat("دخل هذا الشهر", monthIncome, Modifier.weight(1f))
                HeroStat("مصروف هذا الشهر", monthExpense, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: Long, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = .13f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(label, color = Color.White.copy(alpha = .72f), fontSize = 10.sp)
        Text(moneyText(value), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SnapshotCard(
    title: String,
    value: Long,
    positive: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = if (positive) FlosiBlue else FlosiRed
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(15.dp), horizontalAlignment = Alignment.Start) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = .10f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (positive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(11.dp))
            Text(title, color = FlosiMuted, fontSize = 10.sp)
            Text(moneyText(value), color = FlosiText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SafeSpendCard(
    safeToSpend: Long,
    reservedCommitments: Long,
    reservedGoals: Long
) {
    val reservedTotal = reservedCommitments + reservedGoals
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text("المتاح للصرف بأمان", color = FlosiMuted, fontSize = 11.sp)
                Text(moneyText(safeToSpend), color = FlosiText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (reservedTotal > 0L) {
                        "بعد حجز ${moneyText(reservedCommitments)} للالتزامات و ${moneyText(reservedGoals)} للأهداف"
                    } else {
                        "لا توجد مبالغ محجوزة حالياً"
                    },
                    color = if (reservedTotal > 0L) FlosiPurple else FlosiGreen,
                    fontSize = 10.sp
                )
            }

            Surface(color = FlosiPurpleSoft, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("◎", color = FlosiPurple, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SmartInsightCard(monthIncome: Long, monthExpense: Long) {
    val ratio = if (monthIncome > 0L) monthExpense.toDouble() / monthIncome.toDouble() else 0.0
    val insight = when {
        monthIncome <= 0L -> "أضف دخلك حتى أحسب وضعك المالي بدقة"
        ratio < .50 -> "مصروفك مضبوط، عندك مساحة جيدة للادخار"
        ratio < .80 -> "وضعك متوازن، راقب المصاريف غير الضرورية"
        else -> "مصروفك مرتفع مقارنة بالدخل هذا الشهر"
    }

    Surface(
        color = FlosiDark,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = FlosiPurple, modifier = Modifier.size(30.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✦", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text("ملخص فلوسي", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Text(insight, color = Color.White.copy(alpha = .62f), fontSize = 10.sp)
            }
            Text("ذكي", color = Color(0xFFD7C6FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
