package com.flosi.app.i18n

object FlosiLaunchCopy {
    private fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=mapOf("ar" to ar,"en" to en,"tr" to tr,"fr" to fr,"de" to de,"es" to es)

    private val copy = mapOf(
        "welcome_to" to s("أهلاً بك في","Welcome to","Hoş geldin","Bienvenue dans","Willkommen bei","Bienvenido a"),
        "excluded_totals" to s("بعض العملات غير داخلة في الإجماليات: {currencies}. أضف أسعار التحويل من الإعدادات.","Some currencies are excluded from totals: {currencies}. Add exchange rates in settings.","Bazı para birimleri toplamlara dahil değil: {currencies}. Ayarlardan döviz kurlarını ekle.","Certaines devises sont exclues des totaux : {currencies}. Ajoutez les taux de change dans les paramètres.","Einige Währungen sind von den Summen ausgeschlossen: {currencies}. Füge Wechselkurse in den Einstellungen hinzu.","Algunas monedas están excluidas de los totales: {currencies}. Añade los tipos de cambio en ajustes."),
        "reserved" to s("حجز {amount}","Reserved {amount}","Ayrıldı {amount}","Réservé {amount}","Reserviert {amount}","Reservado {amount}"),
        "monthly_net_up" to s("▲ صافي الشهر {amount}","▲ Monthly net {amount}","▲ Aylık net {amount}","▲ Net mensuel {amount}","▲ Monatsnetto {amount}","▲ Neto mensual {amount}"),
        "monthly_net_down" to s("▼ صافي الشهر {amount}","▼ Monthly net {amount}","▼ Aylık net {amount}","▼ Net mensuel {amount}","▼ Monatsnetto {amount}","▼ Neto mensual {amount}"),
        "safe_zone" to s("مساحتك الآمنة","Your safe zone","Güvenli alanın","Votre zone sûre","Deine sichere Zone","Tu zona segura"),
        "safe_daily" to s("حوالي {amount} يومياً • {days} يوم متبقي","About {amount} daily • {days} days left","Günde yaklaşık {amount} • {days} gün kaldı","Environ {amount} par jour • {days} jours restants","Etwa {amount} täglich • {days} Tage übrig","Unos {amount} al día • quedan {days} días"),
        "upcoming_dues" to s("الاستحقاقات القريبة","Upcoming dues","Yaklaşan ödemeler","Échéances à venir","Anstehende Zahlungen","Próximos pagos"),
        "seven_days" to s("7 أيام","7 days","7 gün","7 jours","7 Tage","7 días"),
        "all_clear" to s("كلشي هادئ ✦ ما عندك استحقاقات قريبة","All clear ✦ No upcoming dues","Her şey yolunda ✦ Yaklaşan ödeme yok","Tout est calme ✦ Aucune échéance proche","Alles ruhig ✦ Keine anstehenden Zahlungen","Todo tranquilo ✦ No hay pagos próximos"),
        "overdue_count" to s("متأخر ({count})","Overdue ({count})","Gecikmiş ({count})","En retard ({count})","Überfällig ({count})","Vencidos ({count})"),
        "coming_count" to s("قريب ({count})","Coming up ({count})","Yakında ({count})","À venir ({count})","Demnächst ({count})","Próximos ({count})"),
        "smart_insights" to s("✦ لمحات Flosi الذكية","✦ Flosi Smart Insights","✦ Flosi Akıllı İçgörüler","✦ Analyses intelligentes Flosi","✦ Flosi Smart Insights","✦ Insights inteligentes de Flosi"),

        "liquid_wealth" to s("ثروتك السائلة","Liquid wealth","Likid varlığın","Patrimoine liquide","Liquides Vermögen","Patrimonio líquido"),
        "accounts_included" to s("{count} حساب داخل الإجمالي","{count} accounts included","Toplama dahil {count} hesap","{count} comptes inclus","{count} Konten einbezogen","{count} cuentas incluidas"),
        "excluded" to s("غير محتسب: {currencies}","Excluded: {currencies}","Hariç: {currencies}","Exclu : {currencies}","Ausgeschlossen: {currencies}","Excluido: {currencies}"),
        "no_accounts" to s("بعد ما عندك حسابات","No accounts yet","Henüz hesabın yok","Aucun compte pour l’instant","Noch keine Konten","Aún no hay cuentas"),
        "add_first_account_sub" to s("أضف أول حساب حتى يبدأ Flosi يحسب رصيدك وصرفك بصورة صحيحة.","Add your first account so Flosi can track balances and spending correctly.","Flosi bakiyelerini ve harcamalarını doğru takip etsin diye ilk hesabını ekle.","Ajoutez votre premier compte pour que Flosi suive correctement soldes et dépenses.","Füge dein erstes Konto hinzu, damit Flosi Salden und Ausgaben korrekt verfolgen kann.","Añade tu primera cuenta para que Flosi controle correctamente saldos y gastos."),
        "add_first_account" to s("إضافة أول حساب","Add first account","İlk hesabı ekle","Ajouter le premier compte","Erstes Konto hinzufügen","Añadir primera cuenta"),
        "bank_connection" to s("الربط المصرفي","Bank connection","Banka bağlantısı","Connexion bancaire","Bankverbindung","Conexión bancaria"),
        "bank_secure_sync" to s("مزامنة آمنة عند توفر API رسمي","Secure sync when an official API is available","Resmî API varsa güvenli senkronizasyon","Synchronisation sécurisée avec une API officielle","Sichere Synchronisierung bei offizieller API","Sincronización segura con una API oficial"),
        "connect_bank_account" to s("ربط حساب مصرفي","Connect bank account","Banka hesabını bağla","Connecter un compte bancaire","Bankkonto verbinden","Conectar cuenta bancaria"),
        "bank_password_notice" to s("Flosi ما يطلب ولا يخزن كلمة مرور حسابك المصرفي.","Flosi never asks for or stores your bank password.","Flosi banka şifreni asla istemez veya saklamaz.","Flosi ne demande ni ne stocke jamais votre mot de passe bancaire.","Flosi fragt nie nach deinem Bankpasswort und speichert es nicht.","Flosi nunca solicita ni guarda tu contraseña bancaria."),
        "connect_bank" to s("ربط المصرف","Connect bank","Bankayı bağla","Connecter la banque","Bank verbinden","Conectar banco"),
        "bank_live_notice" to s("الربط الحي يتم فقط عبر API / OAuth رسمي من المصرف أو مزود Open Banking معتمد. Flosi لا يطلب ولا يخزن كلمة مرور حسابك المصرفي.","Live connection is available only through an official bank API/OAuth or an approved Open Banking provider. Flosi never asks for or stores your bank password.","Canlı bağlantı yalnızca resmî banka API/OAuth veya onaylı Open Banking sağlayıcısıyla yapılır. Flosi banka şifreni istemez ya da saklamaz.","La connexion en direct passe uniquement par l’API/OAuth officielle de la banque ou un fournisseur Open Banking agréé. Flosi ne demande ni ne stocke votre mot de passe bancaire.","Live-Verbindungen erfolgen nur über offizielle Bank-API/OAuth oder einen zugelassenen Open-Banking-Anbieter. Flosi fragt nie nach deinem Bankpasswort und speichert es nicht.","La conexión en vivo solo se realiza mediante API/OAuth oficial del banco o un proveedor Open Banking aprobado. Flosi nunca solicita ni guarda tu contraseña bancaria."),
        "bank_import_notice" to s("بعد تفعيل قناة رسمية، يقرأ Flosi الحركات الجديدة فقط ويمنع التكرار.","Once an official connection is configured, Flosi imports only new transactions and prevents duplicates.","Resmî bağlantı kurulduğunda Flosi yalnızca yeni işlemleri alır ve tekrarları önler.","Une fois la connexion officielle configurée, Flosi importe uniquement les nouvelles transactions et évite les doublons.","Nach Einrichtung einer offiziellen Verbindung importiert Flosi nur neue Transaktionen und verhindert Duplikate.","Cuando se configura una conexión oficial, Flosi importa solo transacciones nuevas y evita duplicados."),
        "ok" to s("حسناً","OK","Tamam","OK","OK","Aceptar"),

        "analytics_sub" to s("افهم فلوسك، مو بس راقبها","Understand your money, not just track it","Paranı sadece takip etme, anla","Comprenez votre argent, ne vous contentez pas de le suivre","Verstehe dein Geld, statt es nur zu verfolgen","Entiende tu dinero, no solo lo registres"),
        "some_currencies_excluded" to s("بعض العملات غير محتسبة: {currencies}","Some currencies are excluded: {currencies}","Bazı para birimleri hariç: {currencies}","Certaines devises sont exclues : {currencies}","Einige Währungen sind ausgeschlossen: {currencies}","Algunas monedas están excluidas: {currencies}"),
        "monthly_health" to s("صحة الشهر","Monthly health","Aylık sağlık","Santé mensuelle","Monatliche Gesundheit","Salud mensual"),
        "above_break_even" to s("أنت فوق نقطة التعادل","You're above break-even","Başabaş noktasının üzerindesin","Vous êtes au-dessus du seuil de rentabilité","Du liegst über dem Break-even","Estás por encima del punto de equilibrio"),
        "spending_above_income" to s("مصروفك أعلى من دخلك","Spending is above income","Harcamaların gelirinden yüksek","Les dépenses dépassent les revenus","Ausgaben liegen über dem Einkommen","Los gastos superan los ingresos"),
        "spending_pulse" to s("نبض المصروف","Spending pulse","Harcama nabzı","Rythme des dépenses","Ausgabenpuls","Pulso de gasto"),
        "top_spending_distribution" to s("يعرض توزيع أعلى مصروفات هذا الشهر","Distribution of your top spending this month","Bu ayın en yüksek harcamalarının dağılımını gösterir","Répartition de vos principales dépenses du mois","Verteilung deiner höchsten Ausgaben in diesem Monat","Distribución de tus principales gastos de este mes"),
        "add_expenses_unlock" to s("سجّل مصروفات حتى تظهر التحليلات","Add expenses to unlock analytics","Analizleri görmek için harcama ekle","Ajoutez des dépenses pour débloquer les analyses","Füge Ausgaben hinzu, um Analysen freizuschalten","Añade gastos para activar los análisis"),
        "top_this_month" to s("الأعلى هذا الشهر","Top this month","Bu ayın en yükseği","Le plus élevé ce mois-ci","Höchster Wert diesen Monat","Mayor este mes"),

        "budgets_sub" to s("خلي الصرف تحت السيطرة","Keep spending under control","Harcamayı kontrol altında tut","Gardez vos dépenses sous contrôle","Behalte deine Ausgaben im Griff","Mantén el gasto bajo control"),
        "budget_health" to s("حالة ميزانياتك","Budget health","Bütçe durumu","Santé du budget","Budgetstatus","Estado del presupuesto"),
        "budgets_within_limits" to s("ميزانيات ضمن الحدود","budgets within limits","bütçe sınırlar içinde","budgets dans les limites","Budgets innerhalb der Grenzen","presupuestos dentro de los límites"),
        "healthy" to s("سليم","Healthy","Sağlıklı","Sain","Gesund","Saludable"),
        "watch" to s("تنبيه","Watch","Dikkat","Attention","Beobachten","Atención"),
        "over" to s("متجاوز","Over","Aşım","Dépassé","Überschritten","Excedido"),
        "add_budget" to s("+ ميزانية","+ Budget","+ Bütçe","+ Budget","+ Budget","+ Presupuesto"),
        "create_first_budget" to s("ابدأ أول ميزانية","Create your first budget","İlk bütçeni oluştur","Créez votre premier budget","Erstelle dein erstes Budget","Crea tu primer presupuesto"),
        "budget_empty_sub" to s("حدد سقف للصرف وFlosi يراقب النسبة وينبهك قبل لا تتجاوز الحد.","Set a spending limit and Flosi will track progress and warn you before you go over.","Bir harcama limiti belirle; Flosi ilerlemeyi izler ve aşmadan önce uyarır.","Fixez une limite de dépenses : Flosi suit la progression et vous avertit avant le dépassement.","Lege ein Ausgabenlimit fest; Flosi verfolgt den Fortschritt und warnt dich vor Überschreitungen.","Define un límite de gasto y Flosi seguirá el progreso y te avisará antes de superarlo."),
        "create_budget" to s("إنشاء ميزانية","Create budget","Bütçe oluştur","Créer un budget","Budget erstellen","Crear presupuesto"),
        "all_expenses" to s("كل المصروفات","All expenses","Tüm giderler","Toutes les dépenses","Alle Ausgaben","Todos los gastos"),
        "spent" to s("المصروف","Spent","Harcanan","Dépensé","Ausgegeben","Gastado"),
        "remaining" to s("المتبقي","Remaining","Kalan","Restant","Verbleibend","Restante"),
        "budget_over_msg" to s("تجاوزت الحد. الأفضل تخفف الصرف بهالتصنيف لباقي الشهر.","You've exceeded the limit. Consider easing spending in this category.","Limiti aştın. Ayın geri kalanında bu kategoride harcamayı azalt.","Vous avez dépassé la limite. Réduisez les dépenses dans cette catégorie pour le reste du mois.","Du hast das Limit überschritten. Reduziere die Ausgaben in dieser Kategorie für den Rest des Monats.","Has superado el límite. Reduce el gasto en esta categoría el resto del mes."),
        "budget_warning_msg" to s("وصلت لمنطقة التنبيه. بعدك تگدر تلحق الميزانية.","You've reached the warning zone. There's still time to recover.","Uyarı bölgesine ulaştın. Bütçeyi hâlâ toparlayabilirsin.","Vous avez atteint la zone d’alerte. Il est encore temps de rééquilibrer le budget.","Du hast die Warnzone erreicht. Es ist noch Zeit, das Budget wieder einzufangen.","Has llegado a la zona de alerta. Aún puedes recuperar el presupuesto."),
        "budget_good_msg" to s("وضعك زين، بعدك ضمن الخطة.","You're on track and still within plan.","İyi gidiyorsun, hâlâ plan dahilindesin.","Vous êtes sur la bonne voie et dans le plan.","Du liegst gut im Plan.","Vas bien y sigues dentro del plan."),
        "excluded_currencies" to s("حركات غير محسوبة بعملات: {currencies}","Excluded currencies: {currencies}","Hariç tutulan para birimleri: {currencies}","Devises exclues : {currencies}","Ausgeschlossene Währungen: {currencies}","Monedas excluidas: {currencies}")
    )

    fun text(language:String,key:String,vararg values:Pair<String,Any?>):String {
        val lang = if (language in setOf("ar","en","tr","fr","de","es")) language else "ar"
        var value = copy[key]?.get(lang) ?: error("Missing launch copy: $lang/$key")
        values.forEach { (name,replacement) -> value = value.replace("{$name}", replacement.toString()) }
        return value
    }
}
