(()=>{
'use strict';
if(window.__FLOSI_ROUTES_READY__)return;
window.__FLOSI_ROUTES_READY__=true;

const main=document.querySelector('main.main')||document.querySelector('.main');
if(!main||typeof go!=='function')return;

const style=document.createElement('style');
style.id='flosi-routes-style';
style.textContent=`
.routeScreen .head{margin-bottom:14px}.routeHero{background:linear-gradient(145deg,#17131f,#30213f);color:#fff;border-radius:26px;padding:18px;box-shadow:0 18px 38px rgba(36,27,51,.16);margin-bottom:12px}.routeHero small{display:block;color:#c8c0d1;font-size:9px}.routeHero b{display:block;font-size:19px;margin-top:3px}.routeGrid{display:grid;gap:10px}.routeCard{background:#fff;border:1px solid var(--line);border-radius:20px;padding:14px;box-shadow:var(--shadow)}.routeCard h3{font-size:13px;margin:0 0 4px}.routeCard p{font-size:9px;line-height:1.8;color:var(--muted);margin:0}.routeRow{display:flex;align-items:center;gap:11px;padding:13px 0;border-bottom:1px solid var(--line)}.routeRow:last-child{border-bottom:0}.routeIcon{width:42px;height:42px;border-radius:14px;display:grid;place-items:center;background:#f2ebff;color:var(--p);flex:0 0 42px}.routeCopy{flex:1;min-width:0}.routeCopy b{display:block;font-size:11px}.routeCopy small{display:block;color:var(--muted);font-size:9px;margin-top:2px}.routeValue{font-size:11px;font-weight:700}.routeAction{border:0;border-radius:12px;background:#f3edff;color:var(--p);padding:8px 10px;font-size:9px;font-weight:700}.routeAction.primary{background:linear-gradient(145deg,#955bff,#7138eb);color:#fff;box-shadow:0 8px 18px rgba(123,68,239,.2)}.routePill{display:inline-flex;align-items:center;gap:5px;border-radius:999px;padding:5px 9px;background:#e8f9f2;color:var(--green);font-size:8px;font-weight:700}.routeClickable{cursor:pointer;-webkit-tap-highlight-color:transparent}.routeClickable:active{transform:scale(.992)}.routeManager{border:0;background:#f6f1ff;color:var(--p);border-radius:10px;padding:6px 8px;font-size:8px;font-weight:700;margin-inline-start:6px}.routeSelectGrid{display:grid;grid-template-columns:1fr 1fr;gap:8px}.routeSelect{border:1px solid var(--line);background:#fff;border-radius:15px;padding:11px;text-align:start;font-size:10px}.routeSelect.active{border-color:#a978ff;background:#f6f1ff;color:var(--p);font-weight:700}.routeDanger{background:#fff3f5;color:#db4056}.routeNote{background:linear-gradient(135deg,#f4efff,#fbf9ff);border:1px solid #e8ddff;border-radius:18px;padding:13px;font-size:9px;line-height:1.8;color:#756d80}.routeAudit{display:none}@media(min-width:720px){.routeGrid.cols2{grid-template-columns:1fr 1fr}.routeSelectGrid{grid-template-columns:repeat(3,1fr)}}
`;
document.head.appendChild(style);

function addScreen(id,html){
 if(document.getElementById(id))return;
 const s=document.createElement('section');s.className='screen routeScreen';s.id=id;s.innerHTML=html;main.appendChild(s);
 try{if(typeof screenIds!=='undefined'&&screenIds.add)screenIds.add(id)}catch(_){ }
}
function head(eyebrow,title,sub,parent){return `<div class="head"><div class="headCopy"><div class="eyebrow">${eyebrow}</div><h1 class="title">${title}</h1>${sub?`<div class="sub">${sub}</div>`:''}</div><button class="round" data-go="${parent}">←</button></div>`}

addScreen('activity-detail',`${head('تفاصيل الحركة','الحركة','كل التفاصيل المرتبطة بهذه العملية','activity')}<div class="routeHero"><small id="routeTxType">عملية مالية</small><b id="routeTxAmount">0.00</b><small id="routeTxTitle">—</small></div><div class="routeCard"><div class="routeRow"><div class="routeIcon">◷</div><div class="routeCopy"><b>التاريخ والوقت</b><small id="routeTxDate">اليوم</small></div></div><div class="routeRow"><div class="routeIcon">▣</div><div class="routeCopy"><b>الحساب</b><small id="routeTxAccount">الحساب الرئيسي</small></div></div><div class="routeRow"><div class="routeIcon">⌁</div><div class="routeCopy"><b>التصنيف</b><small id="routeTxCategory">عام</small></div></div></div><div class="routeNote" style="margin-top:10px">هذه الصفحة مرتبطة مباشرة بصفحة الحركات. اختيار أي حركة يفتح تفاصيلها هنا بدون فقدان التنقل.</div>`);

addScreen('insight-detail',`${head('Flosi Intelligence','تحليل اليوم','قراءة أعمق لما يحدث في أموالك','insights')}<div class="routeHero"><small>ملخص ذكي</small><b>وضعك المالي مستقر</b><small>مع فرصة واضحة لتحسين الادخار هذا الشهر</small></div><div class="routeGrid"><div class="routeCard"><h3>اتجاه الصرف</h3><p>المطاعم والتوصيل أقل من الشهر الماضي، بينما الاشتراكات المتكررة تحتاج مراجعة.</p></div><div class="routeCard"><h3>فرصة التوفير</h3><p>إضافة 120 USD للادخار تقرّب هدف السفر تقريباً شهراً كاملاً.</p></div><div class="routeCard"><h3>المخاطر</h3><p>خطر تجاوز الميزانية منخفض إذا بقي معدل الصرف اليومي قريباً من مستواه الحالي.</p></div></div>`);

addScreen('plan-travel',`${head('هدف مالي','السفر','تفاصيل الهدف وخطة الوصول إليه','plan')}<div class="routeHero"><small>التقدم الحالي</small><b>3,400 / 5,000 USD</b><small>68% مكتمل</small></div><div class="routeCard"><div class="routeRow"><div class="routeCopy"><b>المتبقي</b><small>للوصول إلى الهدف</small></div><div class="routeValue">1,600 USD</div></div><div class="routeRow"><div class="routeCopy"><b>الادخار المقترح</b><small>شهرياً</small></div><div class="routeValue">400 USD</div></div><div class="routeRow"><div class="routeCopy"><b>الوقت المتوقع</b><small>بالمعدل المقترح</small></div><div class="routeValue">4 أشهر</div></div></div>`);

addScreen('plan-emergency',`${head('هدف مالي','صندوق الطوارئ','احتياطي يحميك من المفاجآت','plan')}<div class="routeHero"><small>التقدم الحالي</small><b>2,100 / 5,000 USD</b><small>42% مكتمل</small></div><div class="routeCard"><div class="routeRow"><div class="routeCopy"><b>المتبقي</b><small>للوصول إلى الهدف</small></div><div class="routeValue">2,900 USD</div></div><div class="routeRow"><div class="routeCopy"><b>التغطية الحالية</b><small>تقدير تقريبي للمصروف</small></div><div class="routeValue">1.8 شهر</div></div><div class="routeRow"><div class="routeCopy"><b>الحالة</b><small>حسب مستوى الأمان المالي</small></div><span class="routePill">قيد البناء</span></div></div>`);

const securityPages={
 biometric:['القياسات الحيوية','البصمة والوجه','فتح Flosi بسرعة وأمان','◉','استخدم بصمة الإصبع أو الوجه للدخول إلى بياناتك المالية.','biometric'],
 pin:['رمز الدخول','رمز PIN','طبقة احتياطية عند تعذر البصمة','#','غيّر رمز PIN أو أعد ضبطه مع الحفاظ على حماية الحساب.','pin'],
 autolock:['الحماية التلقائية','القفل التلقائي','اختر متى يقفل التطبيق نفسه','◷','القفل التلقائي يقلل فرصة الوصول إلى بياناتك عند ترك الجهاز.','autolock'],
 screenshots:['الخصوصية المرئية','حماية لقطات الشاشة','إخفاء المعلومات الحساسة','▣','يحجب الأرصدة والمعلومات الحساسة عند اللقطات أو شاشة التطبيقات الأخيرة.','screenshots']
};
Object.entries(securityPages).forEach(([key,v])=>addScreen('security-'+key,`${head(v[0],v[1],v[2],'security')}<div class="routeHero"><small>طبقة حماية</small><b>${v[3]} ${v[1]}</b><small>${v[4]}</small></div><div class="routeCard"><div class="routeRow"><div class="routeCopy"><b>الحالة</b><small>يمكن تغييرها من صفحة الأمان الرئيسية</small></div><span class="routePill" id="routeSecState-${key}">مفعّل</span></div><div class="routeRow"><div class="routeCopy"><b>توصية Flosi</b><small>${key==='autolock'?'فعّل القفل بعد مغادرة التطبيق أو الخمول.':'ابقِ هذه الطبقة مفعّلة إذا كان جهازك يستخدم خارج المنزل.'}</small></div></div></div>`));

addScreen('locale-languages',`${head('الإعدادات العالمية','كل اللغات','اختر لغة واجهة Flosi','locale')}<div class="routeCard"><div id="routeLanguageGrid" class="routeSelectGrid"></div></div>`);
addScreen('locale-currencies',`${head('الإعدادات العالمية','كل العملات','اختر العملة الأساسية للتقارير والأهداف','locale')}<div class="routeCard"><div id="routeCurrencyGrid" class="routeSelectGrid"></div></div>`);

addScreen('backup-history',`${head('البيانات','سجل النسخ الاحتياطي','راجع آخر النسخ المشفرة','backup')}<div class="routeHero"><small>الحالة</small><b id="routeBackupHeadline">لا توجد نسخة بعد</b><small>نسخك تبقى تحت سيطرتك</small></div><div class="routeCard"><div class="routeRow"><div class="routeIcon">☁</div><div class="routeCopy"><b>آخر نسخة</b><small id="routeBackupLast">لم يتم الإنشاء</small></div><span class="routePill">مشفرة</span></div></div>`);
addScreen('backup-restore',`${head('البيانات','استعادة نسخة','استعادة آمنة مع خطوة مراجعة','backup')}<div class="routeHero"><small>قبل الاستعادة</small><b>راجع النسخة أولاً</b><small>لن يتم استبدال بياناتك بدون تأكيد</small></div><div class="routeCard"><div class="routeRow"><div class="routeCopy"><b>نسخة Flosi المشفرة</b><small id="routeRestoreInfo">لا توجد نسخة محلية متاحة حالياً</small></div></div><button class="routeAction primary" id="routeRestoreConfirm" style="width:100%;margin-top:12px">استعادة النسخة</button></div>`);

addScreen('pro-features',`${head('FLOSI PRO','ميزات Pro','كل ما تضيفه الخطة الاحترافية','pro')}<div class="routeHero"><small>Flosi Pro</small><b>ذكاء مالي أعمق</b><small>تقارير متقدمة، مزامنة، أهداف أكثر وتنبيهات ذكية</small></div><div class="routeGrid cols2"><div class="routeCard"><h3>ذكاء متقدم</h3><p>قراءات أعمق للتدفق النقدي والقرارات القادمة.</p></div><div class="routeCard"><h3>تقارير احترافية</h3><p>تصدير وتحليل أكثر تفصيلاً عبر الفترات والحسابات.</p></div><div class="routeCard"><h3>مزامنة ونسخ</h3><p>خيارات أوسع لحماية واستمرارية بياناتك.</p></div><div class="routeCard"><h3>أهداف غير محدودة</h3><p>أنشئ أهدافاً وخططاً مالية متعددة في نفس الوقت.</p></div></div><button class="routeAction primary" id="routeProTrial" style="width:100%;margin-top:12px">ابدأ التجربة</button>`);

const coreGo=go;
function validRoute(id){return !!document.getElementById(id)&&document.getElementById(id).classList.contains('screen')}
function navigate(id,opts={}){
 const safe=validRoute(id)?id:'home';
 coreGo(safe);
 if(opts.history!==false){
  const hash='#'+safe;
  if(opts.replace||location.hash===hash)history.replaceState({screen:safe},'',hash);else history.pushState({screen:safe},'',hash);
 }
 syncRouteState(safe);
}
try{go=navigate}catch(_){window.flosiNavigate=navigate}
window.flosiNavigate=navigate;
window.addEventListener('popstate',()=>{const id=(location.hash||'#home').slice(1);coreGo(validRoute(id)?id:'home');syncRouteState(id)});

function txData(el){
 const title=el.querySelector('.txText b')?.textContent?.trim()||'حركة';
 const meta=el.querySelector('.txText small')?.textContent?.trim()||'اليوم';
 const amount=el.querySelector('.neg,.pos')?.textContent?.trim()||'0.00';
 return{title,meta,amount,type:el.querySelector('.pos')?'دخل':'مصروف'};
}
function openTx(el){
 const d=txData(el);document.getElementById('routeTxTitle').textContent=d.title;document.getElementById('routeTxDate').textContent=d.meta;document.getElementById('routeTxAmount').textContent=d.amount;document.getElementById('routeTxType').textContent=d.type;document.getElementById('routeTxCategory').textContent=d.title;navigate('activity-detail');
}
document.querySelectorAll('#activity .tx,#recent .tx').forEach(el=>{el.classList.add('routeClickable');el.setAttribute('role','button');el.tabIndex=0;el.addEventListener('click',()=>openTx(el));el.addEventListener('keydown',e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();openTx(el)}})});

const planCards=[...document.querySelectorAll('#plan .panel')];
if(planCards[0]){planCards[0].classList.add('routeClickable');planCards[0].onclick=()=>navigate('plan-travel')}
if(planCards[1]){planCards[1].classList.add('routeClickable');planCards[1].onclick=()=>navigate('plan-emergency')}

const insightCard=document.querySelector('#insights .intel');if(insightCard){insightCard.classList.add('routeClickable');insightCard.onclick=()=>navigate('insight-detail')}

[...document.querySelectorAll('#security [data-security-toggle]')].forEach(btn=>{
 const key=btn.dataset.securityToggle,parent=btn.parentElement;if(!key||!parent)return;
 const manage=document.createElement('button');manage.className='routeManager';manage.textContent='إعدادات';manage.onclick=e=>{e.preventDefault();e.stopPropagation();navigate('security-'+key)};parent.insertBefore(manage,btn);
});

function buildLocaleExtensions(){
 if(typeof localeMeta==='undefined'||typeof langSelect==='undefined'||typeof currencySelect==='undefined')return;
 const lg=document.getElementById('routeLanguageGrid'),cg=document.getElementById('routeCurrencyGrid');
 if(lg&&!lg.dataset.ready){Object.entries(localeMeta).forEach(([code,m])=>{const b=document.createElement('button');b.className='routeSelect';b.dataset.value=code;b.textContent=m.label;b.onclick=()=>{langSelect.value=code;renderLocale();localStorage.setItem('flosi-lang',code);paintLocaleChoices();navigate('locale')};lg.appendChild(b)});lg.dataset.ready='1'}
 if(cg&&!cg.dataset.ready){[...currencySelect.options].forEach(o=>{const b=document.createElement('button');b.className='routeSelect';b.dataset.value=o.value;b.textContent=o.value;b.onclick=()=>{currencySelect.value=o.value;renderLocale();localStorage.setItem('flosi-currency',o.value);paintLocaleChoices();navigate('locale')};cg.appendChild(b)});cg.dataset.ready='1'}
 paintLocaleChoices();
 const choices=[...document.querySelectorAll('#locale .localeChoice')];
 if(choices[0]&&!choices[0].querySelector('[data-locale-route]')){const x=document.createElement('button');x.className='routeManager';x.dataset.localeRoute='1';x.textContent='كل اللغات';x.onclick=()=>navigate('locale-languages');choices[0].querySelector('.localeChoiceHead')?.appendChild(x)}
 if(choices[1]&&!choices[1].querySelector('[data-currency-route]')){const x=document.createElement('button');x.className='routeManager';x.dataset.currencyRoute='1';x.textContent='كل العملات';x.onclick=()=>navigate('locale-currencies');choices[1].querySelector('.localeChoiceHead')?.appendChild(x)}
}
function paintLocaleChoices(){
 document.querySelectorAll('#routeLanguageGrid .routeSelect').forEach(b=>b.classList.toggle('active',b.dataset.value===langSelect?.value));
 document.querySelectorAll('#routeCurrencyGrid .routeSelect').forEach(b=>b.classList.toggle('active',b.dataset.value===currencySelect?.value));
}
buildLocaleExtensions();

const backupRow=document.querySelector('#backup .settingsRow');if(backupRow){backupRow.classList.add('routeClickable');backupRow.onclick=()=>navigate('backup-history')}
const restoreBtn=document.getElementById('settingsRestore');if(restoreBtn)restoreBtn.onclick=()=>navigate('backup-restore');
const proBtn=document.getElementById('settingsStartPro');if(proBtn)proBtn.onclick=()=>navigate('pro-features');
const restoreConfirm=document.getElementById('routeRestoreConfirm');if(restoreConfirm)restoreConfirm.onclick=()=>{const v=localStorage.getItem('flosi-backup');toast(v?'تمت استعادة النسخة بنجاح':'لا توجد نسخة متاحة للاستعادة')};
const proTrial=document.getElementById('routeProTrial');if(proTrial)proTrial.onclick=()=>toast('تم بدء تجربة Flosi Pro');

function syncRouteState(id){
 try{
  Object.keys(securityPages).forEach(key=>{const pill=document.getElementById('routeSecState-'+key);if(!pill)return;const on=localStorage.getItem('flosi-security-'+key)!=='0';pill.textContent=on?'مفعّل':'متوقف';pill.style.background=on?'#e8f9f2':'#f1eef4';pill.style.color=on?'var(--green)':'#8f8798'});
  const stamp=localStorage.getItem('flosi-backup'),headline=document.getElementById('routeBackupHeadline'),last=document.getElementById('routeBackupLast'),restore=document.getElementById('routeRestoreInfo');
  if(stamp){const d=new Date(stamp);const txt=isNaN(d)?'نسخة محفوظة':d.toLocaleString();if(headline)headline.textContent='نسخة مشفرة متاحة';if(last)last.textContent=txt;if(restore)restore.textContent='آخر نسخة: '+txt}else{if(headline)headline.textContent='لا توجد نسخة بعد';if(last)last.textContent='لم يتم الإنشاء';if(restore)restore.textContent='لا توجد نسخة محلية متاحة حالياً'}
  if(id==='locale-languages'||id==='locale-currencies')paintLocaleChoices();
 }catch(_){ }
}

// Fail-safe audit: every data-go must point to a real screen. Unknown links are redirected home instead of producing a blank page.
document.querySelectorAll('[data-go]').forEach(el=>{const id=el.dataset.go;if(id&&!validRoute(id))el.dataset.go='home'});

const initial=(location.hash||'').slice(1);
if(initial&&validRoute(initial))navigate(initial,{history:false});else history.replaceState({screen:'home'},'','#home');
syncRouteState(initial||'home');
})();
