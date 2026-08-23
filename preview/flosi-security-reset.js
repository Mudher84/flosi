(()=>{
'use strict';
window.__FLOSI_SECURITY_RESET__=true;

/* Keep selected language/currency optically centered in Chromium/Android. */
if(!document.getElementById('flosi-select-center-fix')){
  const style=document.createElement('style');
  style.id='flosi-select-center-fix';
  style.textContent=`
    .localeSelectWrap{position:relative!important}
    #settingsLang.localeSelect,#settingsCurrency.localeSelect{
      appearance:none!important;-webkit-appearance:none!important;color:transparent!important;
      caret-color:transparent!important;padding-left:56px!important;padding-right:56px!important;
      background-image:linear-gradient(45deg,transparent 50%,#7b44ef 50%),linear-gradient(135deg,#7b44ef 50%,transparent 50%),linear-gradient(180deg,#fff,#faf8ff)!important;
      background-position:36px 22px,42px 22px,0 0!important;background-size:7px 7px,7px 7px,100% 100%!important;background-repeat:no-repeat!important
    }
    #settingsLang.localeSelect option,#settingsCurrency.localeSelect option{color:#17131f!important;background:#fff!important}
    .flosiCenteredSelectValue{position:absolute!important;inset:0!important;display:flex!important;align-items:center!important;justify-content:center!important;padding:0 58px!important;pointer-events:none!important;color:#17131f!important;font:inherit!important;line-height:1!important;text-align:center!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important;z-index:2!important}
  `;
  document.head.appendChild(style);
}
function bindCenteredSelect(select){
  if(!select||select.dataset.flosiCentered==='1')return;
  const wrap=select.closest('.localeSelectWrap')||select.parentElement;if(!wrap)return;
  let label=wrap.querySelector('.flosiCenteredSelectValue');
  if(!label){label=document.createElement('span');label.className='flosiCenteredSelectValue';wrap.appendChild(label)}
  const sync=()=>{const o=select.options[select.selectedIndex];label.textContent=o?o.textContent:select.value};
  select.dataset.flosiCentered='1';select.addEventListener('change',()=>requestAnimationFrame(sync));select.addEventListener('input',()=>requestAnimationFrame(sync));
  new MutationObserver(()=>requestAnimationFrame(sync)).observe(select,{attributes:true,childList:true,subtree:true});sync();
}
function installCenteredSelectors(){bindCenteredSelect(document.getElementById('settingsLang'));bindCenteredSelect(document.getElementById('settingsCurrency'))}

/*
 * Preview language hardening.
 * The original preview HTML is Arabic-first. The main locale runtime translates
 * dictionary-backed strings. This layer guarantees that a non-Arabic locale never
 * leaves Arabic UI behind: known strings use the requested dictionary; newer
 * preview-only strings use a curated translation (Chinese) or English fallback.
 */
const AR=/[\u0600-\u06ff\u0750-\u077f\u08a0-\u08ff]/;
const sourceText=new WeakMap();
const EN={
'إعدادات عالمية':'Global settings','اللغة والعملة':'Language & currency','تجربة مالية تتكيف مع لغتك، منطقتك وطريقة عرض الأرقام':'A financial experience adapted to your language, region and number format',
'إعدادك الحالي':'Current setup','متزامن على هذا الجهاز':'Synced on this device','اتجاه الواجهة':'Interface direction','العملة':'Currency','لغة التنسيق':'Formatting locale',
'اقتراح Flosi الذكي':'Flosi smart suggestion','نقدر نضبط اللغة والعملة تلقائياً حسب إعدادات جهازك، بدون ربط التطبيق بدولة محددة.':'We can set language and currency automatically from your device settings without tying Flosi to one country.',
'تطبيق':'Apply','لغة التطبيق':'App language','تحدد اتجاه القراءة وتنسيق التاريخ والأرقام':'Controls reading direction and date/number formatting','كل اللغات':'All languages',
'العملة الأساسية':'Base currency','تستخدم في الملخصات، التقارير والأهداف':'Used in summaries, reports and goals','كل العملات':'All currencies','معاينة فورية':'Live preview','تتحدث تلقائياً':'Updates automatically',
'هكذا ستظهر القيم في الواجهة والتقارير':'This is how values will appear across the app and reports','التاريخ':'Date','الاتجاه':'Direction','مثال صغير':'Compact example',
'حفظ وتطبيق الإعدادات':'Save & apply settings','يحفظ Flosi تفضيلاتك على هذا الجهاز ويستخدمها في العرض والتقارير.':'Flosi saves these preferences on this device and uses them throughout the app and reports.',
'واجهة RTL':'RTL interface','واجهة LTR':'LTR interface','تنسيق أرقام عربي':'Arabic number formatting','من اليمين إلى اليسار':'Right to left','من اليسار إلى اليمين':'Left to right',
'اليوم':'Today','الحركات':'Transactions','الذكاء':'Insights','أنا':'Me','صباح الخير':'Good morning','هذا ملخص وضعك المالي اليوم':'Here is your financial summary for today','صافي ثروتك':'Net worth',
'المتاح للصرف بأمان':'Safe to spend','توقع نهاية الشهر':'End-of-month forecast','الدخل هذا الشهر':'Income this month','المصروف هذا الشهر':'Expenses this month','التزامات قريبة':'Upcoming commitments','نسبة الادخار':'Savings rate',
'قراراتك القادمة':'Your next decisions','عرض الذكاء المالي':'View financial insights','إذا استمريت بنفس المعدل':'If you keep the same pace','الرصيد المتوقع بنهاية الشهر':'Expected balance at month end','هدف السفر':'Travel goal',
'الالتزامات القادمة':'Upcoming commitments','عرض الكل':'View all','الإيجار':'Rent','بعد يومين':'In 2 days','اشتراك الإنترنت':'Internet subscription','بعد 3 أيام':'In 3 days','اشتراك رقمي':'Digital subscription','بعد 5 أيام':'In 5 days',
'فتح':'Open','ثلاث ملاحظات مهمة اليوم':'Three important notes today','إجراءات سريعة':'Quick actions','حركة':'Transaction','هدف':'Goal','ماذا لو':'What if','آخر الحركات':'Recent transactions','التسوق':'Shopping','راتب':'Salary','الحساب الرئيسي':'Main account',
'كل أموالك':'All your money','الذكاء المالي':'Financial insights','مو بس شنو صار، بل شنو راح يصير وشنو الأفضل تسوي':'Not only what happened, but what may happen next and what you can do better','ماذا لو؟':'What if?',
'إذا اشتريت شيء بهذا السعر':'If you buy something at this price','احسب التأثير':'Calculate impact','تحليل اليوم':'Today’s analysis','المستقبل':'Future','التخطيط':'Planning','السفر':'Travel','صندوق الطوارئ':'Emergency fund',
'الخصوصية والتخصيص':'Privacy & personalization','إعداداتك، بياناتك وحماية حسابك':'Your settings, data and account protection','ملفك المالي':'Your financial profile','خصوصية عالية • بياناتك تحت سيطرتك':'High privacy • Your data stays under your control','محمي':'Protected',
'الأمان':'Security','بصمة، PIN، قفل تلقائي وحماية اللقطات':'Biometrics, PIN, auto-lock and screenshot protection','النسخ الاحتياطي المشفر':'Encrypted backup','نسخ واستعادة آمنة وتحكم كامل ببياناتك':'Secure backup and restore with full control of your data',
'ميزات متقدمة لإدارة أموالك':'Advanced features for managing your money','فلوسي برو':'Flosi Pro','ابدأ التجربة':'Start trial','الخصوصية':'Privacy','حماية ذكية لأموالك وبياناتك الحساسة':'Smart protection for your money and sensitive data',
'مستوى الحماية':'Protection level','ممتاز':'Excellent','جميع طبقات الحماية الأساسية فعّالة':'All essential protection layers are active','طبقات':'Layers','آخر فتح آمن':'Last secure unlock','الآن • هذا الجهاز':'Now • This device','حالة الجلسة':'Session status','محمية محلياً':'Protected locally',
'طبقات الحماية':'Protection layers','اضبط مستوى الأمان حسب استخدامك':'Adjust security to match how you use Flosi','مفعّلة':'enabled','البصمة والوجه':'Biometrics','فتح سريع وآمن بالقياسات الحيوية':'Fast, secure biometric unlock','مفعّل':'Enabled','متوقف':'Off',
'رمز PIN':'PIN','طبقة احتياطية عند تعذر البصمة':'Backup unlock when biometrics are unavailable','القفل التلقائي':'Auto-lock','يقفل Flosi بعد مغادرة التطبيق أو الخمول':'Locks Flosi after leaving the app or inactivity','حماية لقطات الشاشة':'Screenshot protection','يخفي الأرصدة والمعلومات الحساسة':'Hides balances and sensitive information',
'توصية Flosi الذكية':'Flosi smart recommendation','إذا تستخدم التطبيق خارج البيت كثيراً، خلّي كل طبقات الحماية مفعّلة حتى ما تظهر أرصدتك في Recent Apps أو اللقطات.':'If you often use Flosi away from home, keep all protection layers enabled so balances stay hidden from Recent Apps and screenshots.',
'إضافة حركة':'Add transaction','البيان':'Description','المبلغ':'Amount','النوع':'Type','مصروف':'Expense','دخل':'Income','حفظ':'Save','إلغاء':'Cancel','الآن':'Now'
};
const ZH={
'Global settings':'全局设置','Language & currency':'语言与货币','A financial experience adapted to your language, region and number format':'根据你的语言、地区和数字格式自动调整的财务体验','Current setup':'当前设置','Synced on this device':'已在此设备同步','Interface direction':'界面方向','Currency':'货币','Formatting locale':'格式区域',
'Flosi smart suggestion':'Flosi 智能建议','We can set language and currency automatically from your device settings without tying Flosi to one country.':'可根据设备设置自动选择语言和货币，而不绑定特定国家。','Apply':'应用','App language':'应用语言','Controls reading direction and date/number formatting':'决定阅读方向以及日期和数字格式','All languages':'所有语言','Base currency':'基础货币','Used in summaries, reports and goals':'用于摘要、报表和目标','All currencies':'所有货币','Live preview':'实时预览','Updates automatically':'自动更新','This is how values will appear across the app and reports':'数值将在应用和报表中按此格式显示','Date':'日期','Direction':'方向','Compact example':'简短示例','Save & apply settings':'保存并应用设置','Flosi saves these preferences on this device and uses them throughout the app and reports.':'Flosi 会在此设备保存这些偏好，并应用到整个应用和报表。',
'Today':'今日','Transactions':'交易','Insights':'智能分析','Me':'我的','Good morning':'早上好','Here is your financial summary for today':'这是你今天的财务摘要','Net worth':'净资产','Safe to spend':'可安全支出','End-of-month forecast':'月末预测','Income this month':'本月收入','Expenses this month':'本月支出','Upcoming commitments':'近期应付款','Savings rate':'储蓄率','Your next decisions':'下一步决策','View financial insights':'查看财务分析','View all':'查看全部','Rent':'房租','Internet subscription':'网络订阅','Digital subscription':'数字订阅','Quick actions':'快捷操作','Transaction':'交易','Goal':'目标','What if':'情景模拟','Recent transactions':'最近交易','Shopping':'购物','Salary':'工资','Main account':'主账户','All your money':'全部资金','Financial insights':'财务智能','Future':'未来','Planning':'规划','Travel':'旅行','Emergency fund':'应急基金','Privacy & personalization':'隐私与个性化','Your settings, data and account protection':'你的设置、数据和账户保护','Your financial profile':'你的财务档案','Protected':'已保护','Security':'安全','Encrypted backup':'加密备份','Advanced features for managing your money':'高级资金管理功能','Start trial':'开始试用','Privacy':'隐私','Protection level':'保护级别','Excellent':'优秀','Protection layers':'保护层','Biometrics':'生物识别','Enabled':'已启用','Off':'已关闭','PIN':'PIN','Auto-lock':'自动锁定','Screenshot protection':'截图保护','Add transaction':'添加交易','Description':'说明','Amount':'金额','Type':'类型','Expense':'支出','Income':'收入','Save':'保存','Cancel':'取消','Now':'现在'
};
function currentLang(){return localStorage.getItem('flosi-lang-preview')||localStorage.getItem('flosi-lang')||'ar'}
function reverseDictionary(){
  const map=new Map(),i18n=window.FLOSI_I18N||{},ar=i18n.ar&&i18n.ar.t;
  if(ar)Object.entries(ar).forEach(([k,v])=>typeof v==='string'&&map.set(v,k));return map;
}
function translateNodeText(base,lang,reverse){
  if(lang==='ar')return base;
  const trim=base.trim();if(!trim)return base;
  let translated=null,key=reverse.get(trim),target=window.FLOSI_I18N&&window.FLOSI_I18N[lang]&&window.FLOSI_I18N[lang].t;
  if(key&&target&&target[key])translated=target[key];
  if(!translated){const en=EN[trim];if(en)translated=(lang==='zh-CN'&&ZH[en])?ZH[en]:en}
  if(!translated&&AR.test(trim))translated=trim.replace(/[\u0600-\u06ff\u0750-\u077f\u08a0-\u08ff]+(?:\s+[\u0600-\u06ff\u0750-\u077f\u08a0-\u08ff]+)*/g,'').replace(/\s{2,}/g,' ').trim();
  if(!translated)return base;
  const at=base.indexOf(trim);return base.slice(0,at)+translated+base.slice(at+trim.length);
}
let translating=false;
function applyNoArabicLeak(){
  if(translating)return;translating=true;
  try{
    const lang=currentLang();if(lang==='ar')return;
    const reverse=reverseDictionary(),root=document.body;if(!root)return;
    const walker=document.createTreeWalker(root,NodeFilter.SHOW_TEXT);const nodes=[];while(walker.nextNode())nodes.push(walker.currentNode);
    nodes.forEach(n=>{
      const p=n.parentElement;if(!p||p.closest('script,style,select,option,textarea,input,.flosiCenteredSelectValue,[data-locale-no-transform]'))return;
      if(!sourceText.has(n))sourceText.set(n,n.nodeValue||'');
      const base=sourceText.get(n)||'';const next=translateNodeText(base,lang,reverse);if(next!==n.nodeValue)n.nodeValue=next;
    });
  }finally{translating=false}
}
function refreshLocaleHardening(){installCenteredSelectors();requestAnimationFrame(()=>{applyNoArabicLeak();installCenteredSelectors()})}
installCenteredSelectors();applyNoArabicLeak();
document.addEventListener('DOMContentLoaded',refreshLocaleHardening,{once:true});
document.addEventListener('change',e=>{if(e.target&&['settingsLang','settingsCurrency'].includes(e.target.id))setTimeout(refreshLocaleHardening,0)});
document.addEventListener('click',e=>{if(e.target.closest('#settingsSaveLocale,#localeApplySmart,[data-go]'))setTimeout(refreshLocaleHardening,0)});
new MutationObserver(ms=>{if(translating)return;if(ms.some(m=>m.addedNodes&&m.addedNodes.length))requestAnimationFrame(refreshLocaleHardening)}).observe(document.documentElement,{childList:true,subtree:true});
setTimeout(refreshLocaleHardening,0);
})();
