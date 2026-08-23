# فلوسي v0.8 — Build Audit

- تم إصلاح نوع الرصيد عند إضافة شخص إلى Long صراحة.
- تم جعل WorkManager Result صريحاً.
- تم إصلاح نطاق Dashboard حتى تستمر الحركات الجديدة بالظهور بعد بدء التطبيق.
- تم إجراء فحص توازن الأقواس على ملفات Kotlin.
- تم تمرير ملفات Kotlin عبر kotlinc كفحص parser؛ مراجع Android/Compose غير متاحة في بيئة المحادثة، لذلك لا يُعد هذا Android compile.
- أضيفت أوامر بناء Windows/Linux وGitHub Actions لبناء APK في بيئة تحتوي Android SDK.
