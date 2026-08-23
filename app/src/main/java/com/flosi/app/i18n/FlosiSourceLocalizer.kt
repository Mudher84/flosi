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
    "صباح الخير" to "Good morning","فلوسي" to "Flosi","إجمالي أموالك" to "Total balance","المقبوض اليوم" to "Received today",
    "المصروف اليوم" to "Spent today","آخر الحركات" to "Recent transactions","عرض الكل" to "View all","ماكو حركات بعد" to "No transactions yet",
    "أعلى المصروفات" to "Top expenses","تظهر بعد تسجيل مصروفات" to "Appears after you record expenses","هذا الشهر" to "This month",
    "دخل هذا الشهر" to "Income this month","مصروف هذا الشهر" to "Expenses this month","المتاح للصرف بأمان" to "Safe to spend",
    "لا توجد مبالغ محجوزة حالياً" to "No reserved amounts right now","ملخص فلوسي" to "Flosi brief","ذكي" to "Smart",
    "أضف دخلك حتى أحسب وضعك المالي بدقة" to "Add your income so Flosi can calculate your financial position accurately",
    "مصروفك مضبوط، عندك مساحة جيدة للادخار" to "Your spending is controlled and you have good room to save",
    "وضعك متوازن، راقب المصاريف غير الضرورية" to "Your position is balanced; keep an eye on non-essential spending",
    "مصروفك مرتفع مقارنة بالدخل هذا الشهر" to "Your spending is high compared with income this month",
    "إعدادات محفوظة ومحرك تحويل واضح" to "Saved settings with a clear conversion engine","سعر يدوي" to "Manual rate",
    "لا توجد أسعار مضافة بعد" to "No exchange rates added yet","كل شيء مسجل" to "Everything is recorded",
    "صافي الحركات المعروضة" to "Net of shown transactions","متعدد العملات" to "Multiple currencies",
    "لا يتم جمع عملات مختلفة كأنها وحدة واحدة." to "Different currencies are never added as if they were one unit.",
    "السجل" to "History","ماكو حركات مطابقة" to "No matching transactions","إعداداتك ومساحتك" to "Your settings and space",
    "فلوسي الشخصي" to "Personal Flosi","أموالك" to "Your money","خطط الصرف" to "Spending plans","القادم عليك" to "Upcoming obligations",
    "بيع وقبض" to "Sales and collections","حماية" to "Protection",
    "تفاصيل الحساب" to "Account details","الرصيد الحالي" to "Current balance","تحويل من/إلى الحساب" to "Transfer from/to account",
    "إدارة التحويلات" to "Manage transfers","إضافة حساب" to "Add account","حساب أو محفظة بعملة مستقلة" to "Account or wallet with its own currency",
    "نوع الحساب" to "Account type","عملة الحساب" to "Account currency","اختيار الحساب" to "Choose account","حساباتك الحقيقية" to "Your real accounts",
    "اختيار" to "Choose","تحويل بين الحسابات" to "Transfer between accounts","يحدث الرصيدين فوراً" to "Updates both balances immediately",
    "سيصل للحساب الآخر" to "Received by destination account","حساباتك مع الآخرين" to "Your balances with other people",
    "صافي حسابات الأشخاص" to "Net people balances","أضف أول شخص" to "Add your first person","إضافة شخص" to "Add person","حساب جديد" to "New balance account",
    "اختيار شخص" to "Choose person","من دفتر الأشخاص" to "From your people directory","كشف الحساب" to "Statement","حساب الشخص" to "Person account",
    "ماكو حركات مرتبطة" to "No linked transactions","إدارة التصنيفات" to "Manage categories","أضف أو أخفِ التصنيف" to "Add or hide categories",
    "اختيار التصنيف" to "Choose category","تصنيفاتك الفعلية" to "Your actual categories","إضافة التزام" to "Add commitment",
    "يحفظ بقاعدة البيانات" to "Saved to your database","إجمالي الالتزامات" to "Total commitments","القادم" to "Upcoming",
    "ماكو التزامات بعد" to "No commitments yet","ميزانية جديدة" to "New budget","شهر تقويمي وصرف فعلي" to "Calendar month with actual spending",
    "الصرف الفعلي مقابل الحدود" to "Actual spending versus limits","الميزانيات النشطة" to "Active budgets","تحتاج انتباه" to "Needs attention",
    "ماكو ميزانيات بعد" to "No budgets yet","هدف جديد" to "New goal","ادخار محجوز ومربوط بحساب" to "Reserved savings linked to an account",
    "الأهداف والادخار المحجوز" to "Goals and reserved savings","من بياناتك الفعلية" to "From your actual data","أعلى التصنيفات" to "Top categories",
    "فواتير محفوظة وحسابات واضحة" to "Saved invoices with clear accounting","إضافة حركة" to "Add transaction","تفاصيل الحركة" to "Transaction details",
    "مركز البيانات" to "Data center","الاستيراد والتصدير" to "Import & export","النسخ الاحتياطية" to "Backups","إدارة النسخ الاحتياطية" to "Manage backups",
    "مركز الإشعارات" to "Notification center","إعدادات الإشعارات" to "Notification settings","الأمان والنسخ" to "Security & backups",
    "مستوى الحماية" to "Protection level","قفل Flosi الآن" to "Lock Flosi now","إدارة النسخ الاحتياطية المشفرة" to "Manage encrypted backups"
)

@Composable
fun localizedLegacyText(source: String): String {
    val trimmed = source.trim()
    val key = legacySourceKeys[trimmed]
    if (key != null) return flosiText(key)
    if (LocalFlosiLanguage.current == "ar") return source
    return englishLegacy[trimmed] ?: source
}
