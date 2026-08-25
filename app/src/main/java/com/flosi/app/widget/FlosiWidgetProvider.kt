package com.flosi.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.flosi.app.MainActivity
import com.flosi.app.R
import com.flosi.app.data.local.FlosiDatabase
import com.flosi.app.data.repository.FinanceRepository
import com.flosi.app.settings.FlosiPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class FlosiWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        super.onUpdate(context, manager, ids)
        val appContext=context.applicationContext
        CoroutineScope(SupervisorJob()+Dispatchers.IO).launch {
            val preferences=FlosiPreferences(appContext)
            val prefs=runCatching{preferences.state.first()}.getOrNull()
            val arabic=(prefs?.language?:"ar")=="ar"
            val dashboard=runCatching{FinanceRepository(FlosiDatabase.get(appContext),preferences).dashboard.first()}.getOrNull()
            ids.forEach{id->manager.updateAppWidget(id,render(appContext,arabic,dashboard?.totalBalance,dashboard?.todayExpense,dashboard?.baseCurrency?:prefs?.currency?:"IQD"))}
        }
    }

    private fun render(context:Context,arabic:Boolean,total:Long?,todayExpense:Long?,currency:String):RemoteViews{
        val locale=if(arabic)Locale("ar","IQ") else Locale.US
        val nf=NumberFormat.getNumberInstance(locale)
        val views=RemoteViews(context.packageName,R.layout.flosi_widget)
        views.setTextViewText(R.id.widget_title,"Flosi")
        views.setTextViewText(R.id.widget_balance_label,if(arabic)"إجمالي أموالك" else "Total balance")
        views.setTextViewText(R.id.widget_balance,total?.let{"${nf.format(it)} $currency"}?:"—")
        views.setTextViewText(R.id.widget_today,if(arabic)"مصروف اليوم: ${todayExpense?.let(nf::format)?:"—"} $currency" else "Spent today: ${todayExpense?.let(nf::format)?:"—"} $currency")
        val intent=Intent(context,MainActivity::class.java)
        val pending=PendingIntent.getActivity(context,8101,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_root,pending)
        return views
    }
}
