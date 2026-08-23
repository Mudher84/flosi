# فلوسي — Android Native v0.7 — Export + Security + Encrypted Backup

تم الإبقاء على Design Lock كما هو.

## نفذ في هذه المرحلة

### PDF حقيقي
- تقرير الحركات باستخدام Android PdfDocument.
- فاتورة PDF حقيقية من بيانات Room.
- حفظ عبر Android Storage Access Framework.

### Excel حقيقي
- تصدير XLSX صالح ببنية Office Open XML مضغوطة.
- كل الحركات والتصنيفات والأشخاص والحسابات المرتبطة تظهر في الملف.

### CSV
- التصدير السابق صار ظاهر ومربوط من مركز البيانات.

### Google Drive بدون أسرار تطبيق
- إنشاء النسخة أو تصدير PDF/XLSX/CSV يفتح Android Document Picker.
- إذا Google Drive مثبت/مربوط بالجهاز يمكن اختياره كمكان الحفظ مباشرة.
- هذا مسار حقيقي ولا يحتاج Client ID لتخزين يدوي.
- النسخ التلقائي الصامت إلى Drive يحتاج OAuth/Google API credentials لذلك لم نزعم تنفيذه.

### النسخ الاحتياطي المشفر
- قاعدة Room تعمل WAL checkpoint قبل النسخ.
- AES-256-GCM.
- مفتاح مشتق من كلمة مرور المستخدم بـ PBKDF2-HMAC-SHA256 (180,000 دورة).
- حفظ ملف .flosi عبر SAF، ويمكن وضعه في Google Drive.
- استرجاع النسخة وفك التشفير ثم استبدال قاعدة البيانات.

### البصمة
- MainActivity أصبحت FragmentActivity.
- عند تفعيل Biometric Lock من الإعدادات، التطبيق يقفل عند التشغيل ويطلب بصمة أو قفل الجهاز.
- إعداد Hide Recent Apps يطبق FLAG_SECURE.

## ما يبقى قبل Release
- Build فعلي بـ Android SDK/Gradle واختبارات compile/instrumentation.
- ضبط signing وapplicationId النهائي.
- تحسين عربية PDF بخط مضمّن إذا أردنا rendering عربي كامل داخل PDF.
- Google Drive background automatic backup يحتاج OAuth credentials لتطبيق الإنتاج.
