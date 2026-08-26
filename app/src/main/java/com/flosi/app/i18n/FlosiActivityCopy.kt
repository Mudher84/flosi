package com.flosi.app.i18n

object FlosiActivityCopy {
    private fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=mapOf("ar" to ar,"en" to en,"tr" to tr,"fr" to fr,"de" to de,"es" to es)
    private val copy=mapOf(
        "search" to s("ابحث بالحركة أو الشخص أو الحساب...","Search transaction, person, or account…","İşlem, kişi veya hesap ara…","Rechercher une transaction, une personne ou un compte…","Transaktion, Person oder Konto suchen…","Buscar movimiento, persona o cuenta…"),
        "all" to s("الكل","All","Tümü","Tout","Alle","Todo"),
        "empty_sub" to s("جرّب تغيّر الفلتر أو أضف أول حركة مالية.","Try another filter or add your first transaction.","Başka bir filtre dene veya ilk işlemini ekle.","Essayez un autre filtre ou ajoutez votre première transaction.","Probiere einen anderen Filter oder füge deine erste Transaktion hinzu.","Prueba otro filtro o añade tu primer movimiento."),
        "reserved" to s("حجز {amount}","Reserved {amount}","Ayrıldı {amount}","Réservé {amount}","Reserviert {amount}","Reservado {amount}"),
        "capture_seconds" to s("سجّلها خلال ثواني","Capture it in seconds","Saniyeler içinde kaydet","Enregistrez-la en quelques secondes","In Sekunden erfassen","Regístralo en segundos"),
        "enter_amount" to s("اكتب المبلغ","Enter amount","Tutarı gir","Saisir le montant","Betrag eingeben","Introduce el importe"),
        "loan" to s("سلفة","Loan","Borç verme","Prêt","Darlehen","Préstamo"),
        "received" to s("استلام","Received","Alınan","Reçu","Erhalten","Recibido"),
        "optional_note" to s("ملاحظة اختيارية","Optional note","İsteğe bağlı not","Note facultative","Optionale Notiz","Nota opcional"),
        "source" to s("من وين؟","Source","Kaynak","Source","Quelle","Origen"),
        "no_account" to s("لا يوجد حساب. أضف حساباً أولاً.","No account exists. Add an account first.","Hesap yok. Önce bir hesap ekle.","Aucun compte. Ajoutez d’abord un compte.","Kein Konto vorhanden. Füge zuerst ein Konto hinzu.","No existe ninguna cuenta. Añade una primero."),
        "add_account" to s("إضافة حساب","Add account","Hesap ekle","Ajouter un compte","Konto hinzufügen","Añadir cuenta"),
        "all_accounts" to s("كل الحسابات","All accounts","Tüm hesaplar","Tous les comptes","Alle Konten","Todas las cuentas"),
        "details" to s("التفاصيل","Details","Ayrıntılar","Détails","Details","Detalles"),
        "person_optional" to s("الشخص — اختياري","Person — optional","Kişi — isteğe bağlı","Personne — facultatif","Person — optional","Persona — opcional"),
        "none" to s("بدون","None","Yok","Aucun","Keine","Ninguna"),
        "view_all" to s("عرض الكل","View all","Tümünü gör","Voir tout","Alle anzeigen","Ver todo"),
        "all_categories" to s("كل التصنيفات","All categories","Tüm kategoriler","Toutes les catégories","Alle Kategorien","Todas las categorías"),
        "save_transaction" to s("حفظ الحركة","Save transaction","İşlemi kaydet","Enregistrer la transaction","Transaktion speichern","Guardar movimiento")
    )
    fun text(language:String,key:String,vararg values:Pair<String,Any?>):String{
        val lang=if(language in setOf("ar","en","tr","fr","de","es")) language else "ar"
        var v=copy.getValue(key).getValue(lang)
        values.forEach{(k,x)->v=v.replace("{$k}",x.toString())}
        return v
    }
}
