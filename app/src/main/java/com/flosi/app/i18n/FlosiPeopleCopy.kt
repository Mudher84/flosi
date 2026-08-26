package com.flosi.app.i18n

object FlosiPeopleCopy {
    private fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=mapOf("ar" to ar,"en" to en,"tr" to tr,"fr" to fr,"de" to de,"es" to es)
    private val copy=mapOf(
        "title" to s("الديوان","Diwan","Defter","Registre","Kontobuch","Libro"),
        "sub" to s("كل ما لك وما عليك في مكان واحد","Everything you are owed and owe in one place","Alacak ve borçlarının hepsi tek yerde","Tout ce qu’on vous doit et ce que vous devez au même endroit","Alles, was dir geschuldet wird und was du schuldest, an einem Ort","Todo lo que te deben y debes en un solo lugar"),
        "owed_to_you" to s("لك","Owed to you","Alacağın","On vous doit","Dir geschuldet","Te deben"),
        "you_owe" to s("عليك","You owe","Borcun","Vous devez","Du schuldest","Debes"),
        "people_count" to s("{count} أشخاص","{count} people","{count} kişi","{count} personnes","{count} Personen","{count} personas"),
        "net" to s("الصافي","Net position","Net durum","Position nette","Nettoposition","Posición neta"),
        "missing_rates" to s("لم تدخل في الإجمالي عملات بدون سعر تحويل: {currencies}","Currencies without an exchange rate were excluded: {currencies}","Döviz kuru olmayan para birimleri toplamdan çıkarıldı: {currencies}","Les devises sans taux de change ont été exclues : {currencies}","Währungen ohne Wechselkurs wurden ausgeschlossen: {currencies}","Se excluyeron monedas sin tipo de cambio: {currencies}"),
        "ledgers" to s("الحسابات الشخصية","Personal ledgers","Kişisel hesaplar","Comptes personnels","Persönliche Konten","Cuentas personales"),
        "add_person" to s("+ شخص","+ Person","+ Kişi","+ Personne","+ Person","+ Persona"),
        "no_people" to s("بعد ما ضفت أحد","No people yet","Henüz kişi eklenmedi","Aucune personne pour le moment","Noch keine Personen","Aún no hay personas"),
        "empty_sub" to s("أضف شخص حتى تسجل السلف والديون والتسديدات ويصير عندك كشف واضح لكل واحد.","Add a person to track loans, debts, repayments, and a clear statement for each one.","Borçları, alacakları ve ödemeleri takip etmek için kişi ekle; herkes için net bir dökümün olsun.","Ajoutez une personne pour suivre prêts, dettes et remboursements avec un relevé clair pour chacun.","Füge eine Person hinzu, um Darlehen, Schulden und Rückzahlungen mit einem klaren Auszug zu verfolgen.","Añade una persona para seguir préstamos, deudas y pagos con un extracto claro para cada una."),
        "add_first" to s("إضافة أول شخص","Add first person","İlk kişiyi ekle","Ajouter la première personne","Erste Person hinzufügen","Añadir primera persona"),
        "owed_value" to s("لك {amount}","Owed to you {amount}","Alacağın {amount}","On vous doit {amount}","Dir geschuldet {amount}","Te deben {amount}"),
        "owe_value" to s("عليك {amount}","You owe {amount}","Borcun {amount}","Vous devez {amount}","Du schuldest {amount}","Debes {amount}"),
        "settled" to s("الحساب مصفّى","Settled","Hesap kapandı","Réglé","Ausgeglichen","Saldado"),
        "how" to s("طريقة الحساب","How it works","Nasıl çalışır","Fonctionnement","So funktioniert es","Cómo funciona"),
        "how_body" to s("الأخضر = مبلغ لك عند شخص. الأحمر = مبلغ عليك له. التسديدات الجزئية تبقى ضمن كشف حساب الشخص حتى يصل الرصيد إلى صفر.","Green means money owed to you. Red means money you owe. Partial repayments stay in the person's statement until the balance reaches zero.","Yeşil sana olan borcu, kırmızı senin borcunu gösterir. Kısmi ödemeler bakiye sıfıra inene kadar kişi dökümünde kalır.","Vert : somme qui vous est due. Rouge : somme que vous devez. Les remboursements partiels restent dans le relevé jusqu’à ce que le solde atteigne zéro.","Grün bedeutet Geld, das dir geschuldet wird. Rot bedeutet Geld, das du schuldest. Teilzahlungen bleiben im Auszug, bis der Saldo null ist.","Verde indica dinero que te deben. Rojo indica dinero que debes. Los pagos parciales permanecen en el extracto hasta que el saldo llegue a cero.")
    )
    fun text(language:String,key:String,vararg values:Pair<String,Any?>):String{val lang=if(language in setOf("ar","en","tr","fr","de","es"))language else "ar";var v=copy.getValue(key).getValue(lang);values.forEach{(k,x)->v=v.replace("{$k}",x.toString())};return v}
}
