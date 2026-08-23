(()=>{
'use strict';
if(window.__FLOSI_SECURITY_CENTER_V2__)return;
window.__FLOSI_SECURITY_CENTER_V2__=true;

const K={
 verifier:'flosi-sec-password-v2',finger:'flosi-sec-fingerprint-v2',face:'flosi-sec-face-v2',auto:'flosi-sec-autolock-v2',shot:'flosi-sec-screenshot-v2'
};
const enc=new TextEncoder();
let bgAt=0,locked=false;

function toast(msg){if(typeof window.toast==='function'){window.toast(msg);return}const t=document.getElementById('toast');if(!t)return;t.textContent=msg;t.classList.add('show');setTimeout(()=>t.classList.remove('show'),1800)}
function hasPassword(){return !!localStorage.getItem(K.verifier)}
function bool(key){return localStorage.getItem(key)==='1'}
function setBool(key,v){localStorage.setItem(key,v?'1':'0')}
function autoValue(){return localStorage.getItem(K.auto)||'60'}
function randHex(bytes=16){const a=new Uint8Array(bytes);crypto.getRandomValues(a);return [...a].map(x=>x.toString(16).padStart(2,'0')).join('')}
function fromHex(hex){const a=new Uint8Array(hex.length/2);for(let i=0;i<a.length;i++)a[i]=parseInt(hex.slice(i*2,i*2+2),16);return a}
async function derive(password,saltHex,iterations=120000){
 const base=await crypto.subtle.importKey('raw',enc.encode(password),'PBKDF2',false,['deriveBits']);
 const bits=await crypto.subtle.deriveBits({name:'PBKDF2',hash:'SHA-256',salt:fromHex(saltHex),iterations},base,256);
 return [...new Uint8Array(bits)].map(x=>x.toString(16).padStart(2,'0')).join('');
}
async function setPassword(password){const salt=randHex();const iterations=120000;const hash=await derive(password,salt,iterations);localStorage.setItem(K.verifier,JSON.stringify({salt,iterations,hash}))}
async function verify(password){try{const v=JSON.parse(localStorage.getItem(K.verifier)||'null');if(!v)return false;return (await derive(password,v.salt,v.iterations))===v.hash}catch(_){return false}}

function ensureStyle(){
 if(document.getElementById('flosiSecurityV2Style'))return;
 const s=document.createElement('style');s.id='flosiSecurityV2Style';s.textContent=`
 #security[data-security-v2]{padding-bottom:24px}.secV2Hero{background:linear-gradient(145deg,#17131f,#30213f);color:#fff;border-radius:26px;padding:18px;box-shadow:0 18px 38px rgba(36,27,51,.18);margin-bottom:12px}.secV2HeroTop{display:flex;align-items:center;gap:12px}.secV2Shield{width:48px;height:48px;border-radius:16px;background:linear-gradient(145deg,#9b62ff,#7138eb);display:grid;place-items:center;font-size:22px}.secV2HeroCopy{flex:1}.secV2HeroCopy small{display:block;color:#c8c0d1;font-size:9px}.secV2HeroCopy b{display:block;font-size:17px;margin-top:2px}.secV2Score{padding:6px 9px;border:1px solid #ffffff18;background:#ffffff0f;border-radius:999px;font-size:8px}.secV2List{display:grid;gap:10px}.secV2Card{background:#fff;border:1px solid var(--line);border-radius:20px;padding:13px 14px;box-shadow:var(--shadow)}.secV2Row{display:flex;align-items:center;gap:11px}.secV2Icon{width:42px;height:42px;border-radius:14px;background:#f4efff;color:var(--p);display:grid;place-items:center;flex:0 0 42px;font-size:18px}.secV2Copy{flex:1;min-width:0}.secV2Copy b{display:block;font-size:11px}.secV2Copy small{display:block;color:var(--muted);font-size:9px;line-height:1.6;margin-top:2px}.secV2Action{border:0;border-radius:11px;background:#f2ebff;color:var(--p);padding:8px 10px;font-size:9px;font-weight:700;white-space:nowrap}.secV2Switch{width:43px;height:25px;accent-color:var(--p)}.secV2Select{height:39px;min-width:118px;border:1px solid #e4daf4;background:#faf8fd;border-radius:11px;padding:0 9px;color:var(--text);font-size:9px;outline:0}.secV2Note{margin-top:10px;padding:10px 12px;border-radius:14px;background:#faf7ff;color:#756d80;font-size:8px;line-height:1.8}.secV2Modal{position:fixed;inset:0;background:rgba(20,14,29,.6);z-index:260;display:none;align-items:center;justify-content:center;padding:18px;backdrop-filter:blur(10px)}.secV2Modal.open{display:flex}.secV2Sheet{width:min(100%,460px);background:#fff;border-radius:26px;padding:20px;box-shadow:0 28px 70px rgba(25,16,38,.3)}.secV2Sheet h3{margin:0 0 15px;font-size:18px}.secV2Field{display:grid;gap:6px;margin:11px 0}.secV2Field label{font-size:9px;color:var(--muted);font-weight:700}.secV2Field input{height:48px;border:1px solid #e6dff0;background:#faf9fc;border-radius:14px;padding:0 13px;outline:0}.secV2Field input:focus{border-color:#aa7cff;box-shadow:0 0 0 4px #7b44ef12}.secV2Btns{display:grid;grid-template-columns:1fr 1fr;gap:9px;margin-top:16px}.secV2Btns button{height:46px;border:0;border-radius:14px;font-size:10px;font-weight:700}.secV2Primary{background:linear-gradient(145deg,#955bff,#7138eb);color:#fff}.secV2Cancel{background:#f1eef6;color:#615a69}.secV2Lock{position:fixed;inset:0;z-index:500;background:linear-gradient(145deg,#17131f,#30213f);display:none;align-items:center;justify-content:center;padding:22px;color:#fff}.secV2Lock.open{display:flex}.secV2LockCard{width:min(100%,420px);text-align:center}.secV2LockIcon{width:70px;height:70px;border-radius:24px;margin:0 auto 14px;background:linear-gradient(145deg,#a66dff,#7138eb);display:grid;place-items:center;font-size:30px;box-shadow:0 18px 40px rgba(123,68,239,.3)}.secV2Lock h2{margin:0;font-size:23px}.secV2Lock p{color:#c9c2d1;font-size:10px;line-height:1.8}.secV2Lock input{width:100%;height:50px;border:1px solid #ffffff20;background:#ffffff10;color:#fff;border-radius:15px;padding:0 14px;outline:0;margin-top:8px}.secV2Lock button{width:100%;height:48px;border:0;border-radius:15px;background:#8b53f4;color:#fff;font-weight:700;margin-top:10px}
 `;document.head.appendChild(s)
}
function statusText(type){
 if(type==='password')return hasPassword()?'مُعيّنة':'غير معيّنة';
 if(type==='finger')return bool(K.finger)?'مفعّلة':'متوقفة';
 if(type==='face')return bool(K.face)?'مفعّلة':'متوقفة';
 if(type==='shot')return bool(K.shot)?'مفعّلة':'متوقفة';
 return '';
}
function score(){let n=0;if(hasPassword())n+=35;if(bool(K.finger))n+=15;if(bool(K.face))n+=15;if(autoValue()!=='never')n+=20;if(bool(K.shot))n+=15;return Math.min(100,n)}
function render(){
 const sec=document.getElementById('security');if(!sec)return;
 ensureStyle();sec.setAttribute('data-security-v2','');
 sec.innerHTML=`<div class="head"><div class="headCopy"><div class="eyebrow">الخصوصية والأمان</div><h1 class="title">أمان Flosi</h1><div class="sub">تحكم بطرق الدخول وحماية بياناتك المالية</div></div><button class="round" data-go="me">←</button></div>
 <div class="secV2Hero"><div class="secV2HeroTop"><div class="secV2Shield">⌾</div><div class="secV2HeroCopy"><small>مستوى الحماية</small><b>${score()}% محمي</b></div><span class="secV2Score">${score()>=80?'قوي':score()>=50?'جيد':'يحتاج إعداد'}</span></div></div>
 <div class="secV2List">
  <div class="secV2Card"><div class="secV2Row"><div class="secV2Icon">●</div><div class="secV2Copy"><b>كلمة المرور</b><small>كلمة مرور احتياطية لفتح التطبيق وحماية إعدادات الأمان.</small></div><button class="secV2Action" id="secV2Password">${hasPassword()?'تغيير':'تعيين'}</button></div><div class="secV2Note">الحالة: ${statusText('password')}</div></div>
  <div class="secV2Card"><div class="secV2Row"><div class="secV2Icon">◉</div><div class="secV2Copy"><b>بصمة الإصبع</b><small>استخدم بصمة الإصبع المسجلة في جهازك لفتح Flosi.</small></div><input class="secV2Switch" id="secV2Finger" type="checkbox" ${bool(K.finger)?'checked':''}></div></div>
  <div class="secV2Card"><div class="secV2Row"><div class="secV2Icon">◎</div><div class="secV2Copy"><b>بصمة الوجه</b><small>استخدم التحقق بالوجه إذا كان جهاز Android يدعمه ومُعداً من النظام.</small></div><input class="secV2Switch" id="secV2Face" type="checkbox" ${bool(K.face)?'checked':''}></div></div>
  <div class="secV2Card"><div class="secV2Row"><div class="secV2Icon">◷</div><div class="secV2Copy"><b>القفل التلقائي</b><small>اقفل التطبيق بعد مغادرته أو بعد مدة من الخمول.</small></div><select class="secV2Select" id="secV2Auto"><option value="0">فوراً</option><option value="30">بعد 30 ثانية</option><option value="60">بعد دقيقة</option><option value="300">بعد 5 دقائق</option><option value="900">بعد 15 دقيقة</option><option value="never">لا يقفل تلقائياً</option></select></div></div>
  <div class="secV2Card"><div class="secV2Row"><div class="secV2Icon">▣</div><div class="secV2Copy"><b>حماية لقطة الشاشة</b><small>في تطبيق Android تمنع تصوير الشاشة وظهور البيانات الحساسة في التطبيقات الأخيرة.</small></div><input class="secV2Switch" id="secV2Shot" type="checkbox" ${bool(K.shot)?'checked':''}></div></div>
 </div>`;
 const auto=sec.querySelector('#secV2Auto');auto.value=autoValue();
 sec.querySelector('#secV2Password').onclick=openPassword;
 sec.querySelector('#secV2Finger').onchange=e=>toggleBiometric(K.finger,e.target.checked,'بصمة الإصبع');
 sec.querySelector('#secV2Face').onchange=e=>toggleBiometric(K.face,e.target.checked,'بصمة الوجه');
 auto.onchange=e=>{localStorage.setItem(K.auto,e.target.value);toast('تم حفظ إعداد القفل التلقائي');render()};
 sec.querySelector('#secV2Shot').onchange=e=>{setBool(K.shot,e.target.checked);toast(e.target.checked?'تم تفعيل حماية لقطة الشاشة':'تم إيقاف حماية لقطة الشاشة');render()};
 if(typeof window.FLOSI_LATINIZE_DIGITS==='function')window.FLOSI_LATINIZE_DIGITS();
}
function toggleBiometric(key,on,label){
 if(on&&!hasPassword()){setBool(key,false);toast('عيّن كلمة المرور أولاً كطريقة دخول احتياطية');render();return}
 setBool(key,on);toast(on?`تم تفعيل ${label}`:`تم إيقاف ${label}`);render();
}
function ensurePasswordModal(){
 let m=document.getElementById('secV2Modal');if(m)return m;m=document.createElement('div');m.id='secV2Modal';m.className='secV2Modal';m.setAttribute('data-locale-no-transform','');m.innerHTML=`<div class="secV2Sheet"><h3>تعيين كلمة المرور</h3><div class="secV2Field"><label>كلمة المرور الجديدة</label><input id="secV2Pass1" type="password" autocomplete="new-password" minlength="6"></div><div class="secV2Field"><label>تأكيد كلمة المرور</label><input id="secV2Pass2" type="password" autocomplete="new-password" minlength="6"></div><div class="secV2Note">6 أحرف على الأقل. لا يتم حفظ كلمة المرور كنص؛ تحفظ المعاينة مشتق PBKDF2 للتحقق المحلي.</div><div class="secV2Btns"><button class="secV2Primary" id="secV2PassSave">حفظ</button><button class="secV2Cancel" id="secV2PassCancel">إلغاء</button></div></div>`;document.body.appendChild(m);m.onclick=e=>{if(e.target===m)m.classList.remove('open')};m.querySelector('#secV2PassCancel').onclick=()=>m.classList.remove('open');m.querySelector('#secV2PassSave').onclick=savePassword;return m
}
function openPassword(){const m=ensurePasswordModal();m.querySelector('#secV2Pass1').value='';m.querySelector('#secV2Pass2').value='';m.classList.add('open');setTimeout(()=>m.querySelector('#secV2Pass1').focus(),60)}
async function savePassword(){const a=document.getElementById('secV2Pass1').value,b=document.getElementById('secV2Pass2').value;if(a.length<6){toast('كلمة المرور يجب أن تكون 6 أحرف على الأقل');return}if(a!==b){toast('تأكيد كلمة المرور غير مطابق');return}await setPassword(a);document.getElementById('secV2Modal').classList.remove('open');toast('تم تعيين كلمة المرور');render()}

function ensureLock(){
 let l=document.getElementById('secV2Lock');if(l)return l;l=document.createElement('div');l.id='secV2Lock';l.className='secV2Lock';l.setAttribute('data-locale-no-transform','');l.innerHTML=`<div class="secV2LockCard"><div class="secV2LockIcon">⌾</div><h2>Flosi مقفل</h2><p>أدخل كلمة المرور للعودة إلى بياناتك المالية.</p><input id="secV2UnlockPass" type="password" placeholder="كلمة المرور" autocomplete="current-password"><button id="secV2UnlockBtn">فتح Flosi</button><p id="secV2BioHint"></p></div>`;document.body.appendChild(l);l.querySelector('#secV2UnlockBtn').onclick=unlock;l.querySelector('#secV2UnlockPass').addEventListener('keydown',e=>{if(e.key==='Enter')unlock()});return l
}
function lock(){if(!hasPassword())return;locked=true;const l=ensureLock();l.querySelector('#secV2UnlockPass').value='';l.querySelector('#secV2BioHint').textContent=(bool(K.finger)||bool(K.face))?'في تطبيق Android يمكن الفتح بالبصمة أو الوجه حسب قدرات الجهاز.':'';l.classList.add('open');setTimeout(()=>l.querySelector('#secV2UnlockPass').focus(),80)}
async function unlock(){const pass=document.getElementById('secV2UnlockPass').value;if(await verify(pass)){locked=false;document.getElementById('secV2Lock').classList.remove('open');toast('تم فتح Flosi')}else toast('كلمة المرور غير صحيحة')}
function bindLifecycle(){
 document.addEventListener('visibilitychange',()=>{if(document.hidden){bgAt=Date.now();return}const v=autoValue();if(v==='never'||!hasPassword())return;const seconds=Math.max(0,(Date.now()-bgAt)/1000);if(v==='0'||seconds>=Number(v))lock()});
 window.addEventListener('blur',()=>{if(bool(K.shot))document.documentElement.style.filter='blur(8px)'});
 window.addEventListener('focus',()=>{document.documentElement.style.filter=''});
}

ensureStyle();render();ensurePasswordModal();ensureLock();bindLifecycle();
setTimeout(render,220);
})();
