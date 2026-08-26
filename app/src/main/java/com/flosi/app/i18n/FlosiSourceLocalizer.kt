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

private fun l(en:String,tr:String,fr:String,de:String,es:String)=mapOf("en" to en,"tr" to tr,"fr" to fr,"de" to de,"es" to es)

private val legacyCopy = mapOf(
    "صباح الخير" to l("Good morning","Günaydın","Bonjour","Guten Morgen","Buenos días"),
    "فلوسي" to l("Flosi","Flosi","Flosi","Flosi","Flosi"),
    "إجمالي أموالك" to l("Total balance","Toplam bakiye","Solde total","Gesamtsaldo","Saldo total"),
    "المقبوض اليوم" to l("Received today","Bugün alınan","Reçu aujourd’hui","Heute erhalten","Recibido hoy"),
    "المصروف اليوم" to l("Spent today","Bugün harcanan","Dépensé aujourd’hui","Heute ausgegeben","Gastado hoy"),
    "آخر الحركات" to l("Recent transactions","Son işlemler","Transactions récentes","Letzte Transaktionen","Movimientos recientes"),
    "عرض الكل" to l("View all","Tümünü gör","Voir tout","Alle anzeigen","Ver todo"),
    "ماكو حركات بعد" to l("No transactions yet","Henüz işlem yok","Aucune transaction pour le moment","Noch keine Transaktionen","Aún no hay movimientos"),
    "أعلى المصروفات" to l("Top expenses","En yüksek giderler","Principales dépenses","Höchste Ausgaben","Principales gastos"),
    "تظهر بعد تسجيل مصروفات" to l("Appears after you record expenses","Gider kaydettikten sonra görünür","Apparaît après l’enregistrement de dépenses","Erscheint nach dem Erfassen von Ausgaben","Aparece después de registrar gastos"),
    "هذا الشهر" to l("This month","Bu ay","Ce mois-ci","Diesen Monat","Este mes"),
    "دخل هذا الشهر" to l("Income this month","Bu ayki gelir","Revenus ce mois-ci","Einnahmen diesen Monat","Ingresos este mes"),
    "مصروف هذا الشهر" to l("Expenses this month","Bu ayki giderler","Dépenses ce mois-ci","Ausgaben diesen Monat","Gastos este mes"),
    "المتاح للصرف بأمان" to l("Safe to spend","Güvenle harcanabilir","Disponible à dépenser en sécurité","Sicher verfügbar","Disponible para gastar con seguridad"),
    "لا توجد مبالغ محجوزة حالياً" to l("No reserved amounts right now","Şu anda ayrılmış tutar yok","Aucun montant réservé actuellement","Derzeit keine reservierten Beträge","No hay importes reservados ahora"),
    "ملخص فلوسي" to l("Flosi brief","Flosi özeti","Résumé Flosi","Flosi-Überblick","Resumen de Flosi"),
    "ذكي" to l("Smart","Akıllı","Intelligent","Smart","Inteligente"),
    "أضف دخلك حتى أحسب وضعك المالي بدقة" to l("Add your income so Flosi can calculate your financial position accurately","Flosi finansal durumunu doğru hesaplasın diye gelirini ekle","Ajoutez vos revenus pour que Flosi calcule précisément votre situation financière","Füge dein Einkommen hinzu, damit Flosi deine finanzielle Lage genau berechnen kann","Añade tus ingresos para que Flosi calcule con precisión tu situación financiera"),
    "مصروفك مضبوط، عندك مساحة جيدة للادخار" to l("Your spending is controlled and you have good room to save","Harcamaların kontrollü; tasarruf için iyi alanın var","Vos dépenses sont maîtrisées et vous avez une bonne marge d’épargne","Deine Ausgaben sind im Griff und du hast guten Spielraum zum Sparen","Tus gastos están controlados y tienes buen margen para ahorrar"),
    "وضعك متوازن، راقب المصاريف غير الضرورية" to l("Your position is balanced; keep an eye on non-essential spending","Durumun dengeli; gereksiz harcamaları izle","Votre situation est équilibrée ; surveillez les dépenses non essentielles","Deine Lage ist ausgeglichen; behalte unnötige Ausgaben im Blick","Tu situación está equilibrada; vigila los gastos no esenciales"),
    "مصروفك مرتفع مقارنة بالدخل هذا الشهر" to l("Your spending is high compared with income this month","Bu ay harcamaların gelire göre yüksek","Vos dépenses sont élevées par rapport aux revenus ce mois-ci","Deine Ausgaben sind diesen Monat im Verhältnis zum Einkommen hoch","Tus gastos son altos en comparación con los ingresos este mes"),
    "إعدادات محفوظة ومحرك تحويل واضح" to l("Saved settings with a clear conversion engine","Kayıtlı ayarlar ve net bir dönüşüm motoru","Paramètres enregistrés avec un moteur de conversion clair","Gespeicherte Einstellungen mit klarer Umrechnungslogik","Ajustes guardados con un motor de conversión claro"),
    "سعر يدوي" to l("Manual rate","Manuel kur","Taux manuel","Manueller Kurs","Tipo manual"),
    "لا توجد أسعار مضافة بعد" to l("No exchange rates added yet","Henüz döviz kuru eklenmedi","Aucun taux de change ajouté","Noch keine Wechselkurse hinzugefügt","Aún no hay tipos de cambio añadidos"),
    "كل شيء مسجل" to l("Everything is recorded","Her şey kayıtlı","Tout est enregistré","Alles ist erfasst","Todo está registrado"),
    "صافي الحركات المعروضة" to l("Net of shown transactions","Gösterilen işlemlerin neti","Net des transactions affichées","Netto der angezeigten Transaktionen","Neto de los movimientos mostrados"),
    "متعدد العملات" to l("Multiple currencies","Çoklu para birimi","Plusieurs devises","Mehrere Währungen","Varias monedas"),
    "لا يتم جمع عملات مختلفة كأنها وحدة واحدة." to l("Different currencies are never added as if they were one unit.","Farklı para birimleri tek birimmiş gibi toplanmaz.","Les devises différentes ne sont jamais additionnées comme une seule unité.","Unterschiedliche Währungen werden niemals wie eine einzige Einheit addiert.","Las distintas monedas nunca se suman como si fueran una sola unidad."),
    "السجل" to l("History","Geçmiş","Historique","Verlauf","Historial"),
    "ماكو حركات مطابقة" to l("No matching transactions","Eşleşen işlem yok","Aucune transaction correspondante","Keine passenden Transaktionen","No hay movimientos coincidentes"),
    "إعداداتك ومساحتك" to l("Your settings and space","Ayarların ve alanın","Vos paramètres et votre espace","Deine Einstellungen und dein Bereich","Tus ajustes y tu espacio"),
    "فلوسي الشخصي" to l("Personal Flosi","Kişisel Flosi","Flosi personnel","Persönliches Flosi","Flosi personal"),
    "أموالك" to l("Your money","Paran","Votre argent","Dein Geld","Tu dinero"),
    "خطط الصرف" to l("Spending plans","Harcama planları","Plans de dépenses","Ausgabenpläne","Planes de gasto"),
    "القادم عليك" to l("Upcoming obligations","Yaklaşan yükümlülükler","Obligations à venir","Anstehende Verpflichtungen","Obligaciones próximas"),
    "بيع وقبض" to l("Sales and collections","Satışlar ve tahsilatlar","Ventes et encaissements","Verkäufe und Zahlungseingänge","Ventas y cobros"),
    "حماية" to l("Protection","Koruma","Protection","Schutz","Protección"),
    "تفاصيل الحساب" to l("Account details","Hesap ayrıntıları","Détails du compte","Kontodetails","Detalles de la cuenta"),
    "الرصيد الحالي" to l("Current balance","Mevcut bakiye","Solde actuel","Aktueller Saldo","Saldo actual"),
    "تحويل من/إلى الحساب" to l("Transfer from/to account","Hesaptan/hesaba transfer","Virement depuis/vers le compte","Überweisung vom/zum Konto","Transferir desde/hacia la cuenta"),
    "إدارة التحويلات" to l("Manage transfers","Transferleri yönet","Gérer les virements","Überweisungen verwalten","Gestionar transferencias"),
    "إضافة حساب" to l("Add account","Hesap ekle","Ajouter un compte","Konto hinzufügen","Añadir cuenta"),
    "حساب أو محفظة بعملة مستقلة" to l("Account or wallet with its own currency","Kendi para birimi olan hesap veya cüzdan","Compte ou portefeuille avec sa propre devise","Konto oder Wallet mit eigener Währung","Cuenta o billetera con su propia moneda"),
    "نوع الحساب" to l("Account type","Hesap türü","Type de compte","Kontotyp","Tipo de cuenta"),
    "عملة الحساب" to l("Account currency","Hesap para birimi","Devise du compte","Kontowährung","Moneda de la cuenta"),
    "اختيار الحساب" to l("Choose account","Hesap seç","Choisir un compte","Konto auswählen","Elegir cuenta"),
    "حساباتك الحقيقية" to l("Your real accounts","Gerçek hesapların","Vos comptes réels","Deine echten Konten","Tus cuentas reales"),
    "اختيار" to l("Choose","Seç","Choisir","Auswählen","Elegir"),
    "تحويل بين الحسابات" to l("Transfer between accounts","Hesaplar arası transfer","Virement entre comptes","Überweisung zwischen Konten","Transferencia entre cuentas"),
    "يحدث الرصيدين فوراً" to l("Updates both balances immediately","Her iki bakiyeyi anında günceller","Met à jour les deux soldes immédiatement","Aktualisiert beide Salden sofort","Actualiza ambos saldos al instante"),
    "سيصل للحساب الآخر" to l("Received by destination account","Hedef hesaba ulaşacak","Reçu par le compte destinataire","Wird dem Zielkonto gutgeschrieben","Llegará a la cuenta de destino"),
    "حساباتك مع الآخرين" to l("Your balances with other people","Diğer kişilerle bakiyelerin","Vos soldes avec d’autres personnes","Deine Salden mit anderen Personen","Tus saldos con otras personas"),
    "صافي حسابات الأشخاص" to l("Net people balances","Kişi bakiyelerinin neti","Solde net des personnes","Netto-Personensalden","Saldo neto de personas"),
    "أضف أول شخص" to l("Add your first person","İlk kişiyi ekle","Ajouter votre première personne","Erste Person hinzufügen","Añade tu primera persona"),
    "إضافة شخص" to l("Add person","Kişi ekle","Ajouter une personne","Person hinzufügen","Añadir persona"),
    "حساب جديد" to l("New balance account","Yeni bakiye hesabı","Nouveau compte de solde","Neues Saldokonto","Nueva cuenta de saldo"),
    "اختيار شخص" to l("Choose person","Kişi seç","Choisir une personne","Person auswählen","Elegir persona"),
    "من دفتر الأشخاص" to l("From your people directory","Kişi dizininden","Depuis votre répertoire de personnes","Aus deinem Personenverzeichnis","Desde tu directorio de personas"),
    "كشف الحساب" to l("Statement","Hesap dökümü","Relevé","Kontoauszug","Extracto"),
    "حساب الشخص" to l("Person account","Kişi hesabı","Compte de la personne","Personenkonto","Cuenta de la persona"),
    "ماكو حركات مرتبطة" to l("No linked transactions","Bağlı işlem yok","Aucune transaction liée","Keine verknüpften Transaktionen","No hay movimientos vinculados"),
    "إدارة التصنيفات" to l("Manage categories","Kategorileri yönet","Gérer les catégories","Kategorien verwalten","Gestionar categorías"),
    "أضف أو أخفِ التصنيف" to l("Add or hide categories","Kategori ekle veya gizle","Ajouter ou masquer des catégories","Kategorien hinzufügen oder ausblenden","Añadir u ocultar categorías"),
    "اختيار التصنيف" to l("Choose category","Kategori seç","Choisir une catégorie","Kategorie auswählen","Elegir categoría"),
    "تصنيفاتك الفعلية" to l("Your actual categories","Gerçek kategorilerin","Vos catégories réelles","Deine tatsächlichen Kategorien","Tus categorías reales"),
    "إضافة التزام" to l("Add commitment","Yükümlülük ekle","Ajouter un engagement","Verpflichtung hinzufügen","Añadir compromiso"),
    "يحفظ بقاعدة البيانات" to l("Saved to your database","Veritabanına kaydedilir","Enregistré dans votre base de données","In deiner Datenbank gespeichert","Guardado en tu base de datos"),
    "إجمالي الالتزامات" to l("Total commitments","Toplam yükümlülükler","Total des engagements","Gesamtverpflichtungen","Total de compromisos"),
    "القادم" to l("Upcoming","Yaklaşan","À venir","Anstehend","Próximo"),
    "ماكو التزامات بعد" to l("No commitments yet","Henüz yükümlülük yok","Aucun engagement pour le moment","Noch keine Verpflichtungen","Aún no hay compromisos"),
    "ميزانية جديدة" to l("New budget","Yeni bütçe","Nouveau budget","Neues Budget","Nuevo presupuesto"),
    "شهر تقويمي وصرف فعلي" to l("Calendar month with actual spending","Takvim ayı ve gerçek harcama","Mois calendaire avec dépenses réelles","Kalendermonat mit tatsächlichen Ausgaben","Mes calendario con gasto real"),
    "الصرف الفعلي مقابل الحدود" to l("Actual spending versus limits","Gerçek harcama ve limitler","Dépenses réelles par rapport aux limites","Tatsächliche Ausgaben gegenüber Limits","Gasto real frente a límites"),
    "الميزانيات النشطة" to l("Active budgets","Aktif bütçeler","Budgets actifs","Aktive Budgets","Presupuestos activos"),
    "تحتاج انتباه" to l("Needs attention","Dikkat gerekiyor","Nécessite votre attention","Braucht Aufmerksamkeit","Necesita atención"),
    "ماكو ميزانيات بعد" to l("No budgets yet","Henüz bütçe yok","Aucun budget pour le moment","Noch keine Budgets","Aún no hay presupuestos"),
    "هدف جديد" to l("New goal","Yeni hedef","Nouvel objectif","Neues Ziel","Nueva meta"),
    "ادخار محجوز ومربوط بحساب" to l("Reserved savings linked to an account","Bir hesaba bağlı ayrılmış tasarruf","Épargne réservée liée à un compte","Reservierte Ersparnisse mit einem Konto verknüpft","Ahorros reservados vinculados a una cuenta"),
    "الأهداف والادخار المحجوز" to l("Goals and reserved savings","Hedefler ve ayrılmış tasarruflar","Objectifs et épargne réservée","Ziele und reservierte Ersparnisse","Metas y ahorros reservados"),
    "من بياناتك الفعلية" to l("From your actual data","Gerçek verilerinden","À partir de vos données réelles","Aus deinen tatsächlichen Daten","A partir de tus datos reales"),
    "أعلى التصنيفات" to l("Top categories","En yüksek kategoriler","Principales catégories","Top-Kategorien","Principales categorías"),
    "فواتير محفوظة وحسابات واضحة" to l("Saved invoices with clear accounting","Net muhasebeyle kayıtlı faturalar","Factures enregistrées avec une comptabilité claire","Gespeicherte Rechnungen mit klarer Abrechnung","Facturas guardadas con contabilidad clara"),
    "إضافة حركة" to l("Add transaction","İşlem ekle","Ajouter une transaction","Transaktion hinzufügen","Añadir movimiento"),
    "تفاصيل الحركة" to l("Transaction details","İşlem ayrıntıları","Détails de la transaction","Transaktionsdetails","Detalles del movimiento"),
    "مركز البيانات" to l("Data center","Veri merkezi","Centre de données","Datenzentrum","Centro de datos"),
    "الاستيراد والتصدير" to l("Import & export","İçe ve dışa aktarma","Importation et exportation","Import & Export","Importar y exportar"),
    "النسخ الاحتياطية" to l("Backups","Yedekler","Sauvegardes","Backups","Copias de seguridad"),
    "إدارة النسخ الاحتياطية" to l("Manage backups","Yedekleri yönet","Gérer les sauvegardes","Backups verwalten","Gestionar copias de seguridad"),
    "مركز الإشعارات" to l("Notification center","Bildirim merkezi","Centre de notifications","Benachrichtigungszentrale","Centro de notificaciones"),
    "إعدادات الإشعارات" to l("Notification settings","Bildirim ayarları","Paramètres des notifications","Benachrichtigungseinstellungen","Ajustes de notificaciones"),
    "الأمان والنسخ" to l("Security & backups","Güvenlik ve yedekler","Sécurité et sauvegardes","Sicherheit & Backups","Seguridad y copias"),
    "مستوى الحماية" to l("Protection level","Koruma seviyesi","Niveau de protection","Schutzniveau","Nivel de protección"),
    "قفل Flosi الآن" to l("Lock Flosi now","Flosi’yi şimdi kilitle","Verrouiller Flosi maintenant","Flosi jetzt sperren","Bloquear Flosi ahora"),
    "إدارة النسخ الاحتياطية المشفرة" to l("Manage encrypted backups","Şifreli yedekleri yönet","Gérer les sauvegardes chiffrées","Verschlüsselte Backups verwalten","Gestionar copias cifradas")
)

internal fun legacyMissingTranslations(language:String):Set<String> =
    if(language=="ar") emptySet() else legacyCopy.filterValues { language !in it }.keys

@Composable
fun localizedLegacyText(source: String): String {
    val trimmed = source.trim()
    val key = legacySourceKeys[trimmed]
    if (key != null) return flosiText(key)
    val language = LocalFlosiLanguage.current
    if (language == "ar") return source
    return legacyCopy[trimmed]?.get(language) ?: legacyCopy[trimmed]?.get("en") ?: source
}
