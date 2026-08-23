(()=>{
'use strict';
if(window.__FLOSI_SECURITY_CENTER_V3__)return;
window.__FLOSI_SECURITY_CENTER_V3__=true;

const K={
 finger:'flosi-sec-fingerprint-v2',
 face:'flosi-sec-face-v2',
 auto:'flosi-sec-autolock-v2',
 shot:'flosi-sec-screenshot-v2'
};

function toast(msg){
 if(typeof window.toast==='function'){window.toast(msg);return}
 const t=document.getElementById('toast');if(!t)return;
 t.textContent=msg;t.classList.add('show');setTimeout(()=>t.classList.remove('show'),1800)
}
function bool(key){return localStorage.getItem(key)==='1'}
function setBool(key,v){localStorage.setItem(key,v?'1':'0')}
function autoValue(){return localStorage.getItem(K.auto)||'60'}

function clearPasswordData(){
 [
  'flosi-sec-password-v2','flosi-security-password','flosi-security-passcode',
  'flosi-password','flosi-passcode','flosi-pin','flosi-security-pin'
 ].forEach(k=>localStorage.removeItem(k));
 document.getElementById('secV2Modal')?.remove();
 document.getElementById('secV2Lock')?.remove();
 document.getElementById('flosiResetPasswordModal')?.remove();
}

function ensureStyle(){
 if(document.getElementById('flosiSecurityV3Style'))return;
 const s=document.createElement('style');s.id='flosiSecurityV3Style';s.textContent=`
 #security[data-security-v3]{padding-bottom:24px}.secV3Hero{background:linear-gradient(145deg,#17131f,#30213f);color:#fff;border-radius:26px;padding:18px;box-shadow:0 18px 38px rgba(36,27,51,.18);margin-bottom:12px}.secV3HeroTop{display:flex;align-items:center;gap:12px}.secV3Shield{width:48px;height:48px;border-radius:16px;background:linear-gradient(145deg,#9b62ff,#7138eb);display:grid;place-items:center;font-size:22px}.secV3HeroCopy{flex:1}.secV3HeroCopy small{display:block;color:#c8c0d1;font-size:9px}.secV3HeroCopy b{display:block;font-size:17px;margin-top:2px}.secV3Score{padding:6px 9px;border:1px solid #ffffff18;background:#ffffff0f;border-radius:999px;font-size:8px}.secV3List{display:grid;gap:10px}.secV3Card{background:#fff;border:1px solid var(--line);border-radius:20px;padding:13px 14px;box-shadow:var(--shadow)}.secV3Row{display:flex;align-items:center;gap:11px}.secV3Icon{width:42px;height:42px;border-radius:14px;background:#f4efff;color:var(--p);display:grid;place-items:center;flex:0 0 42px;font-size:18px}.secV3Copy{flex:1;min-width:0}.secV3Copy b{display:block;font-size:11px}.secV3Copy small{display:block;color:var(--muted);font-size:9px;line-height:1.6;margin-top:2px}.secV3Switch{width:43px;height:25px;accent-color:var(--p)}.secV3Select{height:39px;min-width:118px;border:1px solid #e4daf4;background:#faf8fd;border-radius:11px;padding:0 9px;color:var(--text);font-size:9px;outline:0}.secV3Note{margin-top:12px;padding:11px 12px;border-radius:15px;background:#f7f3ff;border:1px solid #eadfff;color:#67577c;font-size:9px;line-height:1.8}
 `;document.head.appendChild(s)
}

function score(){
 let n=0;
 if(bool(K.finger))n+=25;
 if(bool(K.face))n+=25;
 if(autoValue()!=='never')n+=25;
 if(bool(K.shot))n+=25;
 return n
}

function render(){
 const sec=document.getElementById('security');if(!sec)return;
 clearPasswordData();ensureStyle();sec.setAttribute('data-security-v3','');sec.removeAttribute('data-security-v2');
 const s=score();
 sec.innerHTML=`<div class="head"><div class="headCopy"><div class="eyebrow">الخصوصية والأمان</div><h1 class="title">أمان Flosi</h1><div class="sub">تحكم بطرق الدخول وحماية بياناتك المالية</div></div><button class="round" data-go="me">←</button></div>
 <div class="secV3Hero"><div class="secV3HeroTop"><div class="secV3Shield">⌾</div><div class="secV3HeroCopy"><small>مستوى الحماية</small><b>${s}% محمي</b></div><span class="secV3Score">${s>=75?'قوي':s>=50?'جيد':'يحتاج إعداد'}</span></div></div>
 <div class="secV3List">
  <div class="secV3Card"><div class="secV3Row"><div class="secV3Icon">◉</div><div class="secV3Copy"><b>بصمة الإصبع</b><small>استخدم بصمة الإصبع المسجلة في جهاز Android لفتح Flosi.</small></div><input class="secV3Switch" id="secV3Finger" type="checkbox" ${bool(K.finger)?'checked':''}></div></div>
  <div class="secV3Card"><div class="secV3Row"><div class="secV3Icon">◎</div><div class="secV3Copy"><b>بصمة الوجه</b><small>استخدم التحقق بالوجه إذا كان جهاز Android يدعمه ومُعداً من النظام.</small></div><input class="secV3Switch" id="secV3Face" type="checkbox" ${bool(K.face)?'checked':''}></div></div>
  <div class="secV3Card"><div class="secV3Row"><div class="secV3Icon">◷</div><div class="secV3Copy"><b>القفل التلقائي</b><small>حدد متى يقفل Flosi بعد مغادرة التطبيق أو الخمول.</small></div><select class="secV3Select" id="secV3Auto"><option value="0">فوراً</option><option value="30">بعد 30 ثانية</option><option value="60">بعد دقيقة</option><option value="300">بعد 5 دقائق</option><option value="900">بعد 15 دقيقة</option><option value="never">لا يقفل تلقائياً</option></select></div></div>
  <div class="secV3Card"><div class="secV3Row"><div class="secV3Icon">▣</div><div class="secV3Copy"><b>حماية لقطة الشاشة</b><small>في تطبيق Android تمنع تصوير الشاشة وظهور البيانات الحساسة في التطبيقات الأخيرة.</small></div><input class="secV3Switch" id="secV3Shot" type="checkbox" ${bool(K.shot)?'checked':''}></div></div>
 </div>
 <div class="secV3Note">تم إلغاء كلمة المرور من Flosi. التحقق الحيوي يعتمد على البصمة أو الوجه المسجلين في نظام الجهاز، والقفل التلقائي يعمل معها في تطبيق Android.</div>`;
 const auto=sec.querySelector('#secV3Auto');auto.value=autoValue();
 sec.querySelector('#secV3Finger').onchange=e=>{setBool(K.finger,e.target.checked);toast(e.target.checked?'تم تفعيل بصمة الإصبع':'تم إيقاف بصمة الإصبع');render()};
 sec.querySelector('#secV3Face').onchange=e=>{setBool(K.face,e.target.checked);toast(e.target.checked?'تم تفعيل بصمة الوجه':'تم إيقاف بصمة الوجه');render()};
 auto.onchange=e=>{localStorage.setItem(K.auto,e.target.value);toast('تم حفظ إعداد القفل التلقائي');render()};
 sec.querySelector('#secV3Shot').onchange=e=>{setBool(K.shot,e.target.checked);toast(e.target.checked?'تم تفعيل حماية لقطة الشاشة':'تم إيقاف حماية لقطة الشاشة');render()};
 if(typeof window.FLOSI_LATINIZE_DIGITS==='function')window.FLOSI_LATINIZE_DIGITS();
}

function bindScreenshotPreview(){
 window.addEventListener('blur',()=>{if(bool(K.shot))document.documentElement.style.filter='blur(8px)'});
 window.addEventListener('focus',()=>{document.documentElement.style.filter=''});
}

clearPasswordData();render();bindScreenshotPreview();
setTimeout(render,220);
})();
