package com.flosi.app.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

object FlosiI18n {
    private fun m(vararg p:Pair<String,String>)=mapOf(*p)

    private val en=m(
        "today" to "Today","activity" to "Transactions","people" to "People","me" to "Me","add" to "Add","back" to "Back","save" to "Save","cancel" to "Cancel","edit" to "Edit","delete" to "Delete","enabled" to "Enabled","disabled" to "Disabled","optional" to "Optional",
        "accounts" to "Accounts & wallets","accounts_sub" to "Your money, clearly organized","add_account" to "+ Account","total_liquidity" to "Total liquidity","connected_banks" to "Connected bank accounts","bank_sync" to "Bank sync","bank_sync_sub" to "Automatic bank feeds are optional and require a supported bank connection.","salary_auto" to "Auto-add salary","review_before_add" to "Review before adding","auto_transactions" to "Sync transactions automatically",
        "security" to "Security","security_sub" to "Protect access and financial data","pin_6" to "6-digit PIN","fingerprint" to "Fingerprint","face" to "Face unlock","auto_lock" to "Auto lock","screen_protection" to "Screen protection",
        "language_currency" to "Language & currency","language" to "Language","base_currency" to "Base reporting currency","exchange_rates" to "Exchange rates","saved_rates" to "Saved rates","save_rate" to "Save exchange rate",
        "income" to "Income","expense" to "Expense","salary" to "Salary","amount" to "Amount","account" to "Account","category" to "Category","description" to "Description","date" to "Date","reports" to "Reports","goals" to "Goals","budgets" to "Budgets","commitments" to "Commitments","invoices" to "Invoices","analytics" to "Analytics","settings" to "Settings","data_center" to "Data & export","notifications" to "Notifications","search" to "Search","no_data" to "No data yet","loading" to "Loading…","confirm" to "Confirm","done" to "Done",
        "bank" to "Bank","cash" to "Cash","wallet" to "Wallet","transfer" to "Transfer","fee" to "Fee","monthly_income" to "Monthly income","monthly_expense" to "Monthly expense","net" to "Net","savings" to "Savings","privacy_security" to "Privacy & security","choose_language" to "Choose app language","all_languages_ready" to "Flosi is available in six fully supported launch languages."
    )

    private val ar=m(
        "today" to "اليوم","activity" to "الحركات","people" to "الأشخاص","me" to "أنا","add" to "إضافة","back" to "رجوع","save" to "حفظ","cancel" to "إلغاء","edit" to "تعديل","delete" to "حذف","enabled" to "مفعّل","disabled" to "غير مفعّل","optional" to "اختياري",
        "accounts" to "الحسابات والمحافظ","accounts_sub" to "أموالك موزعة بوضوح","add_account" to "+ حساب","total_liquidity" to "إجمالي السيولة","connected_banks" to "الحسابات المصرفية المتصلة","bank_sync" to "المزامنة المصرفية","bank_sync_sub" to "المزامنة المصرفية اختيارية وتتطلب ربطاً بمصرف مدعوم.","salary_auto" to "إضافة الراتب تلقائياً","review_before_add" to "مراجعة قبل الإضافة","auto_transactions" to "مزامنة الحركات تلقائياً",
        "security" to "الأمان","security_sub" to "حماية الدخول والبيانات المالية","pin_6" to "PIN من 6 أرقام","fingerprint" to "بصمة الإصبع","face" to "بصمة الوجه","auto_lock" to "القفل التلقائي","screen_protection" to "حماية الشاشة",
        "language_currency" to "اللغة والعملة","language" to "اللغة","base_currency" to "عملة التقارير الأساسية","exchange_rates" to "أسعار التحويل","saved_rates" to "الأسعار المحفوظة","save_rate" to "حفظ سعر التحويل",
        "income" to "الدخل","expense" to "المصروف","salary" to "راتب","amount" to "المبلغ","account" to "الحساب","category" to "التصنيف","description" to "البيان","date" to "التاريخ","reports" to "التقارير","goals" to "الأهداف","budgets" to "الميزانيات","commitments" to "الالتزامات","invoices" to "الفواتير","analytics" to "التحليلات","settings" to "الإعدادات","data_center" to "البيانات والتصدير","notifications" to "الإشعارات","search" to "بحث","no_data" to "لا توجد بيانات بعد","loading" to "جارٍ التحميل…","confirm" to "تأكيد","done" to "تم",
        "bank" to "مصرف","cash" to "نقد","wallet" to "محفظة","transfer" to "تحويل","fee" to "الرسوم","monthly_income" to "دخل الشهر","monthly_expense" to "مصروف الشهر","net" to "الصافي","savings" to "الادخار","privacy_security" to "الخصوصية والأمان","choose_language" to "اختر لغة التطبيق","all_languages_ready" to "Flosi متوفر بست لغات أساسية مدعومة بالكامل."
    )

    private val tr=m(
        "today" to "Bugün","activity" to "İşlemler","people" to "Kişiler","me" to "Ben","add" to "Ekle","back" to "Geri","save" to "Kaydet","cancel" to "İptal","edit" to "Düzenle","delete" to "Sil","enabled" to "Etkin","disabled" to "Devre dışı","optional" to "İsteğe bağlı",
        "accounts" to "Hesaplar ve cüzdanlar","accounts_sub" to "Paran net ve düzenli","add_account" to "+ Hesap","total_liquidity" to "Toplam likidite","connected_banks" to "Bağlı banka hesapları","bank_sync" to "Banka senkronizasyonu","bank_sync_sub" to "Otomatik banka akışları isteğe bağlıdır ve desteklenen bir banka bağlantısı gerektirir.","salary_auto" to "Maaşı otomatik ekle","review_before_add" to "Eklemeden önce incele","auto_transactions" to "İşlemleri otomatik eşitle",
        "security" to "Güvenlik","security_sub" to "Erişimi ve finansal verileri koru","pin_6" to "6 haneli PIN","fingerprint" to "Parmak izi","face" to "Yüz ile kilit açma","auto_lock" to "Otomatik kilit","screen_protection" to "Ekran koruması",
        "language_currency" to "Dil ve para birimi","language" to "Dil","base_currency" to "Temel raporlama para birimi","exchange_rates" to "Döviz kurları","saved_rates" to "Kayıtlı kurlar","save_rate" to "Döviz kurunu kaydet",
        "income" to "Gelir","expense" to "Gider","salary" to "Maaş","amount" to "Tutar","account" to "Hesap","category" to "Kategori","description" to "Açıklama","date" to "Tarih","reports" to "Raporlar","goals" to "Hedefler","budgets" to "Bütçeler","commitments" to "Yükümlülükler","invoices" to "Faturalar","analytics" to "Analizler","settings" to "Ayarlar","data_center" to "Veriler ve dışa aktarma","notifications" to "Bildirimler","search" to "Ara","no_data" to "Henüz veri yok","loading" to "Yükleniyor…","confirm" to "Onayla","done" to "Tamamlandı",
        "bank" to "Banka","cash" to "Nakit","wallet" to "Cüzdan","transfer" to "Transfer","fee" to "Ücret","monthly_income" to "Aylık gelir","monthly_expense" to "Aylık gider","net" to "Net","savings" to "Tasarruf","privacy_security" to "Gizlilik ve güvenlik","choose_language" to "Uygulama dilini seçin","all_languages_ready" to "Flosi altı başlangıç dilini tam olarak destekler."
    )

    private val fr=m(
        "today" to "Aujourd’hui","activity" to "Transactions","people" to "Personnes","me" to "Moi","add" to "Ajouter","back" to "Retour","save" to "Enregistrer","cancel" to "Annuler","edit" to "Modifier","delete" to "Supprimer","enabled" to "Activé","disabled" to "Désactivé","optional" to "Facultatif",
        "accounts" to "Comptes et portefeuilles","accounts_sub" to "Votre argent, clairement organisé","add_account" to "+ Compte","total_liquidity" to "Liquidités totales","connected_banks" to "Comptes bancaires connectés","bank_sync" to "Synchronisation bancaire","bank_sync_sub" to "Les flux bancaires automatiques sont facultatifs et nécessitent une banque prise en charge.","salary_auto" to "Ajouter le salaire automatiquement","review_before_add" to "Vérifier avant d’ajouter","auto_transactions" to "Synchroniser automatiquement les transactions",
        "security" to "Sécurité","security_sub" to "Protéger l’accès et les données financières","pin_6" to "Code PIN à 6 chiffres","fingerprint" to "Empreinte digitale","face" to "Déverrouillage facial","auto_lock" to "Verrouillage automatique","screen_protection" to "Protection de l’écran",
        "language_currency" to "Langue et devise","language" to "Langue","base_currency" to "Devise de base des rapports","exchange_rates" to "Taux de change","saved_rates" to "Taux enregistrés","save_rate" to "Enregistrer le taux de change",
        "income" to "Revenus","expense" to "Dépenses","salary" to "Salaire","amount" to "Montant","account" to "Compte","category" to "Catégorie","description" to "Description","date" to "Date","reports" to "Rapports","goals" to "Objectifs","budgets" to "Budgets","commitments" to "Engagements","invoices" to "Factures","analytics" to "Analyses","settings" to "Paramètres","data_center" to "Données et exportation","notifications" to "Notifications","search" to "Rechercher","no_data" to "Aucune donnée pour le moment","loading" to "Chargement…","confirm" to "Confirmer","done" to "Terminé",
        "bank" to "Banque","cash" to "Espèces","wallet" to "Portefeuille","transfer" to "Virement","fee" to "Frais","monthly_income" to "Revenus mensuels","monthly_expense" to "Dépenses mensuelles","net" to "Net","savings" to "Épargne","privacy_security" to "Confidentialité et sécurité","choose_language" to "Choisir la langue de l’application","all_languages_ready" to "Flosi prend entièrement en charge six langues de lancement."
    )

    private val de=m(
        "today" to "Heute","activity" to "Transaktionen","people" to "Personen","me" to "Ich","add" to "Hinzufügen","back" to "Zurück","save" to "Speichern","cancel" to "Abbrechen","edit" to "Bearbeiten","delete" to "Löschen","enabled" to "Aktiviert","disabled" to "Deaktiviert","optional" to "Optional",
        "accounts" to "Konten & Wallets","accounts_sub" to "Dein Geld, klar organisiert","add_account" to "+ Konto","total_liquidity" to "Gesamtliquidität","connected_banks" to "Verbundene Bankkonten","bank_sync" to "Banksynchronisierung","bank_sync_sub" to "Automatische Bankdaten sind optional und erfordern eine unterstützte Bankverbindung.","salary_auto" to "Gehalt automatisch hinzufügen","review_before_add" to "Vor dem Hinzufügen prüfen","auto_transactions" to "Transaktionen automatisch synchronisieren",
        "security" to "Sicherheit","security_sub" to "Zugriff und Finanzdaten schützen","pin_6" to "6-stellige PIN","fingerprint" to "Fingerabdruck","face" to "Gesichtsentsperrung","auto_lock" to "Automatische Sperre","screen_protection" to "Bildschirmschutz",
        "language_currency" to "Sprache & Währung","language" to "Sprache","base_currency" to "Basiswährung für Berichte","exchange_rates" to "Wechselkurse","saved_rates" to "Gespeicherte Kurse","save_rate" to "Wechselkurs speichern",
        "income" to "Einnahmen","expense" to "Ausgaben","salary" to "Gehalt","amount" to "Betrag","account" to "Konto","category" to "Kategorie","description" to "Beschreibung","date" to "Datum","reports" to "Berichte","goals" to "Ziele","budgets" to "Budgets","commitments" to "Verpflichtungen","invoices" to "Rechnungen","analytics" to "Analysen","settings" to "Einstellungen","data_center" to "Daten & Export","notifications" to "Benachrichtigungen","search" to "Suchen","no_data" to "Noch keine Daten","loading" to "Wird geladen…","confirm" to "Bestätigen","done" to "Fertig",
        "bank" to "Bank","cash" to "Bargeld","wallet" to "Wallet","transfer" to "Überweisung","fee" to "Gebühr","monthly_income" to "Monatliche Einnahmen","monthly_expense" to "Monatliche Ausgaben","net" to "Netto","savings" to "Ersparnisse","privacy_security" to "Datenschutz & Sicherheit","choose_language" to "App-Sprache auswählen","all_languages_ready" to "Flosi unterstützt sechs Startsprachen vollständig."
    )

    private val es=m(
        "today" to "Hoy","activity" to "Movimientos","people" to "Personas","me" to "Yo","add" to "Añadir","back" to "Atrás","save" to "Guardar","cancel" to "Cancelar","edit" to "Editar","delete" to "Eliminar","enabled" to "Activado","disabled" to "Desactivado","optional" to "Opcional",
        "accounts" to "Cuentas y billeteras","accounts_sub" to "Tu dinero, claramente organizado","add_account" to "+ Cuenta","total_liquidity" to "Liquidez total","connected_banks" to "Cuentas bancarias conectadas","bank_sync" to "Sincronización bancaria","bank_sync_sub" to "Los movimientos bancarios automáticos son opcionales y requieren una conexión bancaria compatible.","salary_auto" to "Añadir salario automáticamente","review_before_add" to "Revisar antes de añadir","auto_transactions" to "Sincronizar transacciones automáticamente",
        "security" to "Seguridad","security_sub" to "Protege el acceso y los datos financieros","pin_6" to "PIN de 6 dígitos","fingerprint" to "Huella digital","face" to "Desbloqueo facial","auto_lock" to "Bloqueo automático","screen_protection" to "Protección de pantalla",
        "language_currency" to "Idioma y moneda","language" to "Idioma","base_currency" to "Moneda base para informes","exchange_rates" to "Tipos de cambio","saved_rates" to "Tipos guardados","save_rate" to "Guardar tipo de cambio",
        "income" to "Ingresos","expense" to "Gastos","salary" to "Salario","amount" to "Importe","account" to "Cuenta","category" to "Categoría","description" to "Descripción","date" to "Fecha","reports" to "Informes","goals" to "Metas","budgets" to "Presupuestos","commitments" to "Compromisos","invoices" to "Facturas","analytics" to "Análisis","settings" to "Ajustes","data_center" to "Datos y exportación","notifications" to "Notificaciones","search" to "Buscar","no_data" to "Aún no hay datos","loading" to "Cargando…","confirm" to "Confirmar","done" to "Listo",
        "bank" to "Banco","cash" to "Efectivo","wallet" to "Billetera","transfer" to "Transferencia","fee" to "Comisión","monthly_income" to "Ingresos mensuales","monthly_expense" to "Gastos mensuales","net" to "Neto","savings" to "Ahorros","privacy_security" to "Privacidad y seguridad","choose_language" to "Elegir idioma de la aplicación","all_languages_ready" to "Flosi admite completamente seis idiomas de lanzamiento."
    )

    private val maps=mapOf("ar" to ar,"en" to en,"tr" to tr,"fr" to fr,"de" to de,"es" to es)
    val requiredKeys=en.keys
    fun missingKeys(language:String)=requiredKeys-(maps[language]?.keys?:emptySet())
    fun text(language:String?,key:String):String{
        val code=if(FlosiLocales.isSupported(language))language!! else "ar"
        return maps.getValue(code)[key]?:error("Missing Flosi translation: $code/$key")
    }
}

val LocalFlosiLanguage=staticCompositionLocalOf{"ar"}
@Composable fun flosiText(key:String)=FlosiI18n.text(LocalFlosiLanguage.current,key)
