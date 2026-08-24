(()=>{
'use strict';
try{
  localStorage.setItem('flosi-lang','ar');
  localStorage.setItem('flosi-lang-preview','ar');
}catch(_){ }

window.FLOSI_I18N={
  ar:{
    dir:'rtl',
    name:'العربية',
    t:{
      morning:'صباح الخير',
      total:'إجمالي أموالك',
      month:'هذا الشهر +1,420.00',
      income:'الدخل',
      expense:'المصروف',
      todayIncome:'المقبوض اليوم',
      todayExpense:'المصروف اليوم',
      recent:'آخر الحركات',
      viewAll:'عرض الكل',
      shopping:'التسوق',
      salary:'راتب',
      allAccounts:'كل حساباتك',
      transactions:'الحركات',
      settingsPrivacy:'الإعدادات والحماية',
      me:'أنا',
      security:'الأمان',
      securityDesc:'بصمة، PIN، قفل تلقائي',
      languageCurrency:'اللغة والعملة',
      reportsTables:'التقارير والجداول',
      reportsDesc:'تحليل مالي متقدم',
      privacy:'الخصوصية',
      biometrics:'البصمة والوجه',
      pin:'رمز PIN',
      autoLock:'القفل التلقائي',
      hideRecent:'إخفاء من التطبيقات الأخيرة',
      blockShots:'منع لقطات الشاشة',
      globalSettings:'إعدادات عالمية',
      appLanguage:'لغة التطبيق',
      baseCurrency:'العملة الأساسية',
      saveSettings:'حفظ الإعدادات',
      visualAnalysis:'تحليل بصري',
      monthlyExpense:'مصروف الشهر',
      savings:'الادخار',
      net:'الصافي',
      date:'التاريخ',
      category:'التصنيف',
      account:'الحساب',
      amount:'المبلغ',
      today:'اليوم',
      reports:'التقارير',
      addTransaction:'إضافة حركة',
      description:'البيان',
      save:'حفظ',
      cancel:'إلغاء',
      saved:'تم الحفظ',
      mainAccount:'الحساب الرئيسي'
    }
  }
};

function lockArabicOnly(){
  document.documentElement.lang='ar';
  document.documentElement.dir='rtl';
  if(document.body){
    document.body.dir='rtl';
    document.body.style.fontFamily='Cairo, "Noto Sans Arabic", system-ui, sans-serif';
  }
  const select=document.getElementById('settingsLang');
  if(select){
    [...select.options].forEach(opt=>{if(opt.value!=='ar')opt.remove();});
    select.value='ar';
    select.disabled=true;
    select.setAttribute('aria-label','العربية فقط');
  }
  document.querySelectorAll('[data-language-option],[data-lang]').forEach(el=>{
    const v=el.getAttribute('data-language-option')||el.getAttribute('data-lang');
    if(v&&v!=='ar')el.remove();
  });
}

if(document.readyState==='loading'){
  document.addEventListener('DOMContentLoaded',lockArabicOnly,{once:true});
}else{
  lockArabicOnly();
}
setTimeout(lockArabicOnly,0);
setTimeout(lockArabicOnly,120);
setTimeout(lockArabicOnly,400);
})();