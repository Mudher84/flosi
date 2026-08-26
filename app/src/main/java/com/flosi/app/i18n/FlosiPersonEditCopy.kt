package com.flosi.app.i18n

object FlosiPersonEditCopy {
    private fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=mapOf("ar" to ar,"en" to en,"tr" to tr,"fr" to fr,"de" to de,"es" to es)
    private val copy=mapOf(
        "sub" to s("رصيد شخصي افتتاحي بعملة واضحة","A personal opening balance with an explicit currency","Açık para birimiyle kişisel açılış bakiyesi","Un solde d’ouverture personnel avec une devise explicite","Persönlicher Anfangssaldo mit klarer Währung","Un saldo inicial personal con moneda explícita"),
        "name" to s("الاسم","Name","Ad","Nom","Name","Nombre"),
        "phone" to s("الهاتف — اختياري","Phone — optional","Telefon — isteğe bağlı","Téléphone — facultatif","Telefon — optional","Teléfono — opcional"),
        "opening_balance" to s("الرصيد الافتتاحي","Opening balance","Açılış bakiyesi","Solde d’ouverture","Anfangssaldo","Saldo inicial"),
        "currency" to s("عملة حساب الشخص","Person balance currency","Kişi bakiyesi para birimi","Devise du solde de la personne","Währung des Personensaldos","Moneda del saldo de la persona"),
        "owed_to_me" to s("لي عنده","Owed to me","Bana borçlu","On me doit","Mir geschuldet","Me debe"),
        "i_owe" to s("عليّ له","I owe","Ona borçluyum","Je dois","Ich schulde","Le debo"),
        "currency_notice" to s("إذا ربطت حركة بهذا الشخص، يجب أن تكون الحركة من حساب بنفس العملة حتى يبقى رصيد الشخص دقيقاً ولا يتغير تاريخياً بتغير سعر الصرف.","A transaction linked to this person must use an account in the same currency so the person's balance stays exact and does not change historically with exchange rates.","Bu kişiye bağlı işlem aynı para birimindeki bir hesaptan olmalı; böylece bakiye kesin kalır ve kur değişimleri geçmişi etkilemez.","Une transaction liée à cette personne doit utiliser un compte dans la même devise afin que le solde reste exact et ne change pas avec les taux de change.","Eine mit dieser Person verknüpfte Transaktion muss ein Konto in derselben Währung verwenden, damit der Saldo exakt bleibt und sich historisch nicht durch Wechselkurse ändert.","Un movimiento vinculado a esta persona debe usar una cuenta en la misma moneda para que el saldo sea exacto y no cambie históricamente por los tipos de cambio."),
        "too_large" to s("الرصيد أكبر من الحد المسموح","Balance is too large","Bakiye izin verilen sınırı aşıyor","Le solde dépasse la limite autorisée","Der Saldo ist zu groß","El saldo supera el límite permitido")
    )
    fun text(language:String,key:String):String{val lang=if(language in setOf("ar","en","tr","fr","de","es"))language else "ar";return copy.getValue(key).getValue(lang)}
}
