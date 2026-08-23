package com.flosi.app.i18n

import androidx.compose.runtime.Composable

private val legacySourceKeys = mapOf(
    "اليوم" to "today","الحركات" to "activity","الأشخاص" to "people","أنا" to "me","إضافة" to "add","رجوع" to "back",
    "حفظ" to "save","إلغاء" to "cancel","تعديل" to "edit","حذف" to "delete","الحسابات والمحافظ" to "accounts",
    "أموالك موزعة بوضوح" to "accounts_sub","+ حساب" to "add_account","إجمالي السيولة" to "total_liquidity",
    "الحسابات المصرفية المتصلة" to "connected_banks","المزامنة المصرفية" to "bank_sync","مزامنة الحركات تلقائياً" to "auto_transactions",
    "إضافة الراتب تلقائياً" to "salary_auto","مراجعة قبل الإضافة" to "review_before_add","الأمان" to "security",
    "حماية الدخول والبيانات المالية" to "security_sub","PIN من 6 أرقام" to "pin_6","بصمة الإصبع" to "fingerprint","بصمة الوجه" to "face",
    "القفل التلقائي" to "auto_lock","حماية الشاشة" to "screen_protection","اللغة والعملة" to "language_currency","اللغة" to "language",
    "عملة التقارير الأساسية" to "base_currency","أسعار التحويل" to "exchange_rates","الأسعار المحفوظة" to "saved_rates",
    "حفظ سعر التحويل" to "save_rate","الدخل" to "income","المصروف" to "expense","راتب" to "salary","المبلغ" to "amount",
    "الحساب" to "account","التصنيف" to "category","البيان" to "description","التاريخ" to "date","التقارير" to "reports",
    "الأهداف" to "goals","الميزانيات" to "budgets","الالتزامات" to "commitments","الفواتير" to "invoices","التحليلات" to "analytics",
    "الإعدادات" to "settings","البيانات والتصدير" to "data_center","الإشعارات" to "notifications","بحث" to "search",
    "لا توجد بيانات بعد" to "no_data","تأكيد" to "confirm","تم" to "done","مصرف" to "bank","بنك" to "bank","نقد" to "cash",
    "كاش" to "cash","محفظة" to "wallet","تحويل" to "transfer","رسوم" to "fee","الرسوم" to "fee","دخل الشهر" to "monthly_income",
    "مصروف الشهر" to "monthly_expense","الصافي" to "net","الادخار" to "savings","الخصوصية والأمان" to "privacy_security"
)

private val englishLegacy = mapOf(
    "صباح الخير" to "Good morning",
    "فلوسي" to "Flosi",
    "إجمالي أموالك" to "Total balance",
    "المقبوض اليوم" to "Received today",
    "المصروف اليوم" to "Spent today",
    "آخر الحركات" to "Recent transactions",
    "عرض الكل" to "View all",
    "ماكو حركات بعد" to "No transactions yet",
    "أعلى المصروفات" to "Top expenses",
    "تظهر بعد تسجيل مصروفات" to "Appears after you record expenses",
    "هذا الشهر" to "This month",
    "دخل هذا الشهر" to "Income this month",
    "مصروف هذا الشهر" to "Expenses this month",
    "المتاح للصرف بأمان" to "Safe to spend",
    "لا توجد مبالغ محجوزة حالياً" to "No reserved amounts right now",
    "ملخص فلوسي" to "Flosi brief",
    "ذكي" to "Smart",
    "أضف دخلك حتى أحسب وضعك المالي بدقة" to "Add your income so Flosi can calculate your financial position accurately",
    "مصروفك مضبوط، عندك مساحة جيدة للادخار" to "Your spending is controlled and you have good room to save",
    "وضعك متوازن، راقب المصاريف غير الضرورية" to "Your position is balanced; keep an eye on non-essential spending",
    "مصروفك مرتفع مقارنة بالدخل هذا الشهر" to "Your spending is high compared with income this month",
    "إعدادات محفوظة ومحرك تحويل واضح" to "Saved settings with a clear conversion engine",
    "سعر يدوي" to "Manual rate",
    "لا توجد أسعار مضافة بعد" to "No exchange rates added yet"
)

@Composable
fun localizedLegacyText(source: String): String {
    val trimmed = source.trim()
    val key = legacySourceKeys[trimmed]
    if (key != null) return flosiText(key)
    if (LocalFlosiLanguage.current == "ar") return source
    return englishLegacy[trimmed] ?: source
}
