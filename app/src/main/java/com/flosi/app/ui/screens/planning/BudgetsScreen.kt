package com.flosi.app.ui.screens.planning

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun BudgetsScreen(onBack:()->Unit,onDetail:()->Unit){
    val vm:PlanningViewModel=flosiViewModel()
    val items by vm.budgetProgress.collectAsState()
    val categories by vm.categories.collectAsState()
    val categoryNames=remember(categories){categories.associate{it.id to it.name}}
    val overCount=items.count{it.isOver}
    val warningCount=items.count{!it.isOver&&it.warningReached}

    FlosiPage("الميزانيات","الصرف الفعلي مقابل الحدود",onBack){
        CardBox{
            Metric("الميزانيات النشطة",items.size.toString(),FlosiPurple)
            Metric("تحتاج انتباه",(overCount+warningCount).toString(),if(overCount>0)FlosiRed else FlosiOrange)
        }

        SectionTitle("ميزانياتك","+ ميزانية",onDetail)

        if(items.isEmpty()){
            CardBox{Text("ماكو ميزانيات بعد",color=FlosiMuted)}
        }

        items.forEach{item->
            val budget=item.budget
            val tone=when{
                item.isOver->FlosiRed
                item.warningReached->FlosiOrange
                else->FlosiPurple
            }
            val category=budget.categoryId?.let(categoryNames::get)?:"كل المصروفات"
            val percent=item.usagePercent.coerceAtLeast(0f)

            CardBox{
                ActionRow(
                    title=budget.title,
                    subtitle="$category • تنبيه ${budget.warningPercent}%",
                    value="${percent.toInt()}%",
                    accent=tone,
                    onClick=onDetail
                )
                LinearProgressIndicator(
                    progress={ (percent/100f).coerceIn(0f,1f) },
                    color=tone
                )
                Text(
                    "المصروف ${moneyText(item.spent,budget.currency)} من ${moneyText(budget.limitAmount,budget.currency)}",
                    color=FlosiText
                )
                Text(
                    when{
                        item.isOver->"تجاوزت الحد بـ ${moneyText(item.overAmount,budget.currency)}"
                        item.warningReached->"تنبيه: وصلت إلى ${percent.toInt()}% من الحد"
                        else->"المتبقي ${moneyText(item.remaining,budget.currency)}"
                    },
                    color=tone
                )
                if(item.missingCurrencies.isNotEmpty()){
                    Text(
                        "لم تُحسب حركات بعملات: ${item.missingCurrencies.joinToString()} — أضف أسعار التحويل",
                        color=FlosiOrange
                    )
                }
            }
        }
    }
}
