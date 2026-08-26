package com.flosi.app.i18n

object FlosiTransactionDetailCopy {
    private fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=mapOf("ar" to ar,"en" to en,"tr" to tr,"fr" to fr,"de" to de,"es" to es)
    private val copy=mapOf(
        "saved_transaction" to s("حركة محفوظة","Saved transaction","Kayıtlı işlem","Transaction enregistrée","Gespeicherte Transaktion","Movimiento guardado"),
        "not_found" to s("الحركة غير موجودة","Transaction not found","İşlem bulunamadı","Transaction introuvable","Transaktion nicht gefunden","Movimiento no encontrado"),
        "linked_person" to s("الشخص المرتبط","Linked person","Bağlı kişi","Personne liée","Verknüpfte Person","Persona vinculada"),
        "no_person" to s("بدون شخص","No person","Kişi yok","Aucune personne","Keine Person","Sin persona"),
        "no_category" to s("بدون تصنيف","No category","Kategori yok","Aucune catégorie","Keine Kategorie","Sin categoría"),
        "note" to s("ملاحظة","Note","Not","Note","Notiz","Nota"),
        "delete_transaction" to s("حذف الحركة","Delete transaction","İşlemi sil","Supprimer la transaction","Transaktion löschen","Eliminar movimiento"),
        "delete_question" to s("حذف الحركة؟","Delete transaction?","İşlem silinsin mi?","Supprimer la transaction ?","Transaktion löschen?","¿Eliminar movimiento?"),
        "delete_explain" to s("سيتم عكس أثرها المحاسبي. التحويل يحذف طرفيه ورسومه معاً، أما دفعات الفواتير والالتزامات المرتبطة فلا يمكن حذفها منفردة.","Its accounting effect will be reversed. Transfers remove both sides and their fee together; linked invoice and commitment payments cannot be detached.","Muhasebe etkisi geri alınır. Transferlerde iki taraf ve ücret birlikte silinir; bağlı fatura ve yükümlülük ödemeleri ayrı silinemez.","Son effet comptable sera annulé. Les virements suppriment les deux côtés et les frais ; les paiements liés aux factures et engagements ne peuvent pas être détachés.","Die Buchungswirkung wird rückgängig gemacht. Bei Überweisungen werden beide Seiten und die Gebühr gemeinsam gelöscht; verknüpfte Rechnungs- und Verpflichtungszahlungen können nicht einzeln entfernt werden.","Se revertirá su efecto contable. Las transferencias eliminan ambos lados y la comisión; los pagos vinculados a facturas y compromisos no pueden separarse."),
        "could_not_delete" to s("تعذر حذف الحركة","Could not delete transaction","İşlem silinemedi","Impossible de supprimer la transaction","Transaktion konnte nicht gelöscht werden","No se pudo eliminar el movimiento"),
        "income" to s("دخل","Income","Gelir","Revenu","Einnahme","Ingreso"),
        "expense" to s("مصروف","Expense","Gider","Dépense","Ausgabe","Gasto"),
        "debt_given" to s("سلفة ممنوحة","Loan given","Verilen borç","Prêt accordé","Verliehen","Préstamo otorgado"),
        "debt_received" to s("دين مستلم","Debt received","Alınan borç","Dette reçue","Erhaltene Schuld","Deuda recibida"),
        "transfer_in" to s("تحويل وارد","Transfer in","Gelen transfer","Virement entrant","Eingehende Überweisung","Transferencia entrante"),
        "transfer_out" to s("تحويل صادر","Transfer out","Giden transfer","Virement sortant","Ausgehende Überweisung","Transferencia saliente"),
        "invoice_payment" to s("دفعة فاتورة","Invoice payment","Fatura ödemesi","Paiement de facture","Rechnungszahlung","Pago de factura"),
        "goal_saving" to s("ادخار هدف","Goal saving","Hedef tasarrufu","Épargne d’objectif","Zielsparen","Ahorro para meta")
    )
    fun text(language:String,key:String):String{val lang=if(language in setOf("ar","en","tr","fr","de","es"))language else "ar";return copy.getValue(key).getValue(lang)}
}
