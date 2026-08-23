package com.flosi.app.ui.screens.planning

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun BudgetsScreen(onBack:()->Unit,onDetail:()->Unit){
    val vm:PlanningViewModel=flosiViewModel();val items by vm.budgetProgress.collectAsState();val categories by vm.categories.collectAsState();val lang=LocalFlosiLanguage.current
    val categoryNames=remember(categories){categories.associate{it.id to it.name}};val overCount=items.count{it.isOver};val warningCount=items.count{!it.isOver&&it.warningReached}

    FlosiPage(flosiText("budgets"),localizedLegacyText("الصرف الفعلي مقابل الحدود"),onBack){
        CardBox{
            Metric(localizedLegacyText("الميزانيات النشطة"),items.size.toString(),FlosiPurple)
            Metric(localizedLegacyText("تحتاج انتباه"),(overCount+warningCount).toString(),if(overCount>0)FlosiRed else FlosiOrange)
        }
        SectionTitle(flosiText("budgets"),if(lang=="ar")"+ ميزانية" else "+ Budget",onDetail)
        if(items.isEmpty()) CardBox{Text(localizedLegacyText("ماكو ميزانيات بعد"),color=FlosiMuted)}
        items.forEach{item->
            val budget=item.budget;val tone=when{item.isOver->FlosiRed;item.warningReached->FlosiOrange;else->FlosiPurple}
            val category=budget.categoryId?.let(categoryNames::get)?:if(lang=="ar")"كل المصروفات" else "All expenses";val percent=item.usagePercent.coerceAtLeast(0f)
            CardBox{
                ActionRow(budget.title,"$category • ${if(lang=="ar")"تنبيه" else "Alert"} ${budget.warningPercent}%","${percent.toInt()}%",tone)
                LinearProgressIndicator(progress={ (percent/100f).coerceIn(0f,1f) },color=tone)
                Text(if(lang=="ar")"المصروف ${moneyText(item.spent,budget.currency)} من ${moneyText(budget.limitAmount,budget.currency)}" else "Spent ${moneyText(item.spent,budget.currency)} of ${moneyText(budget.limitAmount,budget.currency)}",color=FlosiText)
                Text(
                    when{
                        item.isOver->if(lang=="ar")"تجاوزت الحد بـ ${moneyText(item.overAmount,budget.currency)}" else "Over limit by ${moneyText(item.overAmount,budget.currency)}"
                        item.warningReached->if(lang=="ar")"تنبيه: وصلت إلى ${percent.toInt()}% من الحد" else "Alert: ${percent.toInt()}% of the limit reached"
                        else->if(lang=="ar")"المتبقي ${moneyText(item.remaining,budget.currency)}" else "Remaining ${moneyText(item.remaining,budget.currency)}"
                    },color=tone
                )
                if(item.missingCurrencies.isNotEmpty()) Text(if(lang=="ar")"لم تُحسب حركات بعملات: ${item.missingCurrencies.joinToString()} — أضف أسعار التحويل" else "Transactions in ${item.missingCurrencies.joinToString()} were excluded — add exchange rates",color=FlosiOrange)
            }
        }
    }
}
