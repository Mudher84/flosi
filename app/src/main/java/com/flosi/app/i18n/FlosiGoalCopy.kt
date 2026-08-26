package com.flosi.app.i18n

object FlosiGoalCopy {
    private fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=mapOf("ar" to ar,"en" to en,"tr" to tr,"fr" to fr,"de" to de,"es" to es)
    private val copy=mapOf(
        "sub" to s("خلّي الادخار يصير مرئي","Make progress visible","İlerlemeyi görünür kıl","Rendez vos progrès visibles","Mach Fortschritt sichtbar","Haz visible tu progreso"),
        "journey" to s("رحلة الادخار","Savings journey","Tasarruf yolculuğu","Parcours d’épargne","Sparreise","Camino de ahorro"),
        "completed" to s("أهداف مكتملة","goals completed","tamamlanan hedefler","objectifs atteints","Ziele abgeschlossen","metas completadas"),
        "active" to s("نشط","Active","Aktif","Actif","Aktiv","Activo"),
        "done" to s("مكتمل","Done","Tamamlandı","Terminé","Erledigt","Completado"),
        "add_goal" to s("+ هدف","+ Goal","+ Hedef","+ Objectif","+ Ziel","+ Meta"),
        "start_first" to s("ابدأ أول هدف","Start your first goal","İlk hedefini başlat","Commencez votre premier objectif","Starte dein erstes Ziel","Empieza tu primera meta"),
        "empty_sub" to s("حدد شي تريد توصله وخلي Flosi يحول الادخار من فكرة إلى تقدم تشوفه كل يوم.","Pick something you want to reach and let Flosi turn saving into visible progress.","Ulaşmak istediğin bir şeyi seç; Flosi tasarrufu her gün görebileceğin ilerlemeye dönüştürsün.","Choisissez un objectif et laissez Flosi transformer l’épargne en progrès visible chaque jour.","Wähle etwas, das du erreichen möchtest, und lass Flosi Sparen in sichtbaren Fortschritt verwandeln.","Elige algo que quieras alcanzar y deja que Flosi convierta el ahorro en progreso visible."),
        "create" to s("إنشاء هدف","Create goal","Hedef oluştur","Créer un objectif","Ziel erstellen","Crear meta"),
        "not_linked" to s("غير مربوط بحساب","Not linked to an account","Bir hesaba bağlı değil","Non lié à un compte","Nicht mit einem Konto verknüpft","No vinculada a una cuenta"),
        "saved" to s("المحفوظ","Saved","Birikmiş","Épargné","Gespart","Ahorrado"),
        "remaining" to s("المتبقي","Remaining","Kalan","Restant","Verbleibend","Restante"),
        "congrats" to s("مبروك ✦ الهدف مكتمل","Congrats ✦ Goal completed","Tebrikler ✦ Hedef tamamlandı","Félicitations ✦ Objectif atteint","Glückwunsch ✦ Ziel erreicht","Felicidades ✦ Meta completada"),
        "very_close" to s("قريب جداً ✦ بقى أقل من ربع الهدف","Very close ✦ less than a quarter left","Çok yakın ✦ hedefin dörtte birinden az kaldı","Très proche ✦ moins d’un quart reste","Fast geschafft ✦ weniger als ein Viertel bleibt","Muy cerca ✦ queda menos de una cuarta parte"),
        "halfway" to s("تجاوزت النص، كمّل بنفس النسق","Past halfway — keep the pace","Yarıyı geçtin — aynı tempoda devam et","Vous avez dépassé la moitié — gardez le rythme","Über die Hälfte geschafft — weiter so","Superaste la mitad — mantén el ritmo"),
        "small_step" to s("كل دفعة صغيرة تقرّبك","Every small contribution moves you closer","Her küçük katkı seni yaklaştırır","Chaque petite contribution vous rapproche","Jeder kleine Beitrag bringt dich näher","Cada pequeña aportación te acerca"),
        "add_to_goal" to s("أضف للهدف","Add to goal","Hedefe ekle","Ajouter à l’objectif","Zum Ziel hinzufügen","Añadir a la meta"),
        "link_notice" to s("أنشئ هدف جديد مربوط بحساب حتى يصير الادخار جزء من السجل المالي.","Create a goal linked to an account so savings stay in the financial ledger.","Tasarrufun finansal kayıtta kalması için bir hesaba bağlı hedef oluştur.","Créez un objectif lié à un compte afin que l’épargne reste dans le registre financier.","Erstelle ein mit einem Konto verknüpftes Ziel, damit das Sparen im Finanzverlauf bleibt.","Crea una meta vinculada a una cuenta para que el ahorro quede en el registro financiero."),
        "dialog_title" to s("إضافة للهدف — {title}","Add to goal — {title}","Hedefe ekle — {title}","Ajouter à l’objectif — {title}","Zum Ziel hinzufügen — {title}","Añadir a la meta — {title}"),
        "reserve_notice" to s("المبلغ ينحجز من المتاح للصرف، وما ينحسب كمصروف.","This amount is reserved from safe-to-spend and is not counted as an expense.","Bu tutar güvenle harcanabilir bakiyeden ayrılır ve gider sayılmaz.","Ce montant est réservé sur le disponible à dépenser et n’est pas compté comme dépense.","Dieser Betrag wird vom sicher verfügbaren Betrag reserviert und nicht als Ausgabe gezählt.","Este importe se reserva del disponible para gastar y no se cuenta como gasto."),
        "remaining_amount" to s("المتبقي {amount}","Remaining {amount}","Kalan {amount}","Restant {amount}","Verbleibend {amount}","Restante {amount}"),
        "too_large" to s("المبلغ أكبر من المتبقي للهدف","Amount exceeds the goal remainder","Tutar hedefte kalan miktarı aşıyor","Le montant dépasse le reste de l’objectif","Betrag übersteigt den verbleibenden Zielbetrag","El importe supera lo que queda de la meta"),
        "reserve" to s("حجز المبلغ","Reserve amount","Tutarı ayır","Réserver le montant","Betrag reservieren","Reservar importe")
    )
    fun text(language:String,key:String,vararg values:Pair<String,Any?>):String{
        val lang=if(language in setOf("ar","en","tr","fr","de","es"))language else "ar"
        var v=copy.getValue(key).getValue(lang)
        values.forEach{(k,x)->v=v.replace("{$k}",x.toString())}
        return v
    }
}
