(()=>{
'use strict';
if(window.__FLOSI_BANK_LINK__)return;
window.__FLOSI_BANK_LINK__=true;

const main=document.querySelector('main.main')||document.querySelector('.main');
const me=document.getElementById('me');
if(!main||!me)return;

const css=document.createElement('style');
css.textContent=`
.bankConnectCard{width:100%;border:1px solid var(--line);background:#fff;border-radius:20px;padding:14px;display:flex;align-items:center;gap:12px;text-align:start;box-shadow:var(--shadow);cursor:pointer}.bankConnectCard .bankIcon{width:44px;height:44px;border-radius:14px;background:#eaf7ff;color:#2878d8;display:grid;place-items:center;font-size:19px}.bankConnectCard .copy{flex:1}.bankConnectCard b{display:block;font-size:12px}.bankConnectCard small{display:block;color:var(--muted);font-size:9px;margin-top:2px;line-height:1.7}.bankConnectCard .arrow{color:var(--p);font-size:18px}.bankGrid{display:grid;gap:10px}.bankProvider{border:1px solid var(--line);background:#fff;border-radius:17px;padding:12px;text-align:start}.bankProvider.active{border-color:#a978ff;background:#f6f1ff}.bankSwitch{display:flex;align-items:center;gap:10px;padding:12px 0;border-bottom:1px solid var(--line)}.bankSwitch:last-child{border-bottom:0}.bankSwitch span{flex:1;font-size:10px}.bankStatus{border-radius:18px;padding:13px;background:linear-gradient(135deg,#f3edff,#fbf9ff);border:1px solid #e7dcff;font-size:9px;line-height:1.8;color:#756d80}.bankPrimary{width:100%;border:0;border-radius:15px;background:linear-gradient(145deg,#955bff,#7138eb);color:#fff;padding:13px 15px;font-weight:700;margin-top:12px}.bankSecondary{width:100%;border:1px solid var(--line);border-radius:15px;background:#fff;color:var(--text);padding:12px 15px;font-weight:700;margin-top:8px}.bankWarn{color:var(--amber);font-size:9px;line-height:1.8;margin-top:10px}.bankOk{color:var(--green);font-size:9px;line-height:1.8;margin-top:10px}
`;
document.head.appendChild(css);

const list=me.querySelector('div[style*="display:grid;gap:10px"]');
if(list&&!document.getElementById('openBankConnect')){
  const btn=document.createElement('button');
  btn.id='openBankConnect';btn.className='bankConnectCard';btn.type='button';
  btn.innerHTML='<span class="bankIcon">🏦</span><span class="copy"><b>ربط حساب مصرفي</b><small>مزامنة الحركات والراتب تلقائياً عند توفر ربط مصرفي رسمي</small></span><span class="arrow">‹</span>';
  list.insertBefore(btn,list.firstChild);
}

if(!document.getElementById('bank-connect')){
  const s=document.createElement('section');s.className='screen';s.id='bank-connect';
  s.innerHTML=`<div class="head"><div class="headCopy"><div class="eyebrow">الحسابات المصرفية المتصلة</div><h1 class="title">ربط المصرف</h1><div class="sub">اختر المصرف وطريقة مزامنة الحركات</div></div><button class="round" id="bankBack">←</button></div>
  <div class="routeHero"><small>حالة الربط</small><b id="bankStateTitle">غير مرتبط</b><small id="bankStateSub">لم يتم ربط حساب مصرفي بهذا الجهاز بعد</small></div>
  <div class="routeCard"><h3>اختر المصرف</h3><div class="bankGrid" id="bankProviders"></div></div>
  <div class="routeCard" style="margin-top:10px"><h3>خيارات المزامنة</h3>
    <label class="bankSwitch"><span>مزامنة الحركات تلقائياً</span><input id="bankSyncToggle" type="checkbox"></label>
    <label class="bankSwitch"><span>إضافة الراتب تلقائياً عند نزوله</span><input id="bankSalaryToggle" type="checkbox"></label>
    <label class="bankSwitch"><span>مراجعة الحركات قبل إضافتها</span><input id="bankReviewToggle" type="checkbox"></label>
  </div>
  <div class="bankStatus" style="margin-top:10px">Flosi لا يطلب كلمة مرور حسابك المصرفي ولا يخزنها. الربط الحقيقي يجب أن يتم عبر API / OAuth رسمي من المصرف أو مزود Open Banking معتمد.</div>
  <button class="bankPrimary" id="bankConnectNow">ربط المصرف</button>
  <button class="bankSecondary" id="bankImportStatement">استيراد كشف حساب بدلاً من الربط</button>
  <div id="bankMessage"></div>`;
  main.appendChild(s);
  try{if(typeof screenIds!=='undefined'&&screenIds.add)screenIds.add('bank-connect')}catch(_){ }
}

const providers=[
  {id:'rafidain',name:'مصرف الرافدين'},
  {id:'rasheed',name:'مصرف الرشيد'},
  {id:'tbi',name:'Trade Bank of Iraq (TBI)'},
  {id:'other',name:'مصرف آخر'}
];
let selected=localStorage.getItem('flosi-bank-provider')||'';
const grid=document.getElementById('bankProviders');
function renderProviders(){
  grid.innerHTML='';providers.forEach(p=>{const b=document.createElement('button');b.className='bankProvider'+(selected===p.id?' active':'');b.type='button';b.innerHTML=`<b>${p.name}</b><small>${p.id==='other'?'إضافة المصرف عند توفر ربط رسمي':'يتطلب API / OAuth رسمي من المصرف'}</small>`;b.onclick=()=>{selected=p.id;localStorage.setItem('flosi-bank-provider',selected);renderProviders()};grid.appendChild(b)})
}
function openScreen(id){
  if(typeof go==='function'){go(id);return}
  document.querySelectorAll('.screen').forEach(x=>x.classList.toggle('active',x.id===id));window.scrollTo(0,0)
}
function loadSettings(){
  document.getElementById('bankSyncToggle').checked=localStorage.getItem('flosi-bank-sync')==='1';
  document.getElementById('bankSalaryToggle').checked=localStorage.getItem('flosi-bank-salary-auto')==='1';
  document.getElementById('bankReviewToggle').checked=localStorage.getItem('flosi-bank-review')!=='0';
  const linked=localStorage.getItem('flosi-bank-linked')==='1';
  document.getElementById('bankStateTitle').textContent=linked?'مرتبط':'غير مرتبط';
  document.getElementById('bankStateSub').textContent=linked?'تم حفظ إعداد الربط، وتنتظر المزامنة قناة مصرفية رسمية فعالة.':'لم يتم ربط حساب مصرفي بهذا الجهاز بعد';
}
['bankSyncToggle','bankSalaryToggle','bankReviewToggle'].forEach(id=>document.getElementById(id).addEventListener('change',e=>{
 const key=id==='bankSyncToggle'?'flosi-bank-sync':id==='bankSalaryToggle'?'flosi-bank-salary-auto':'flosi-bank-review';localStorage.setItem(key,e.target.checked?'1':'0')
}));

document.getElementById('openBankConnect')?.addEventListener('click',()=>{loadSettings();openScreen('bank-connect')});
document.getElementById('bankBack').onclick=()=>openScreen('me');
document.getElementById('bankConnectNow').onclick=()=>{
 const m=document.getElementById('bankMessage');
 if(!selected){m.className='bankWarn';m.textContent='اختر المصرف أولاً.';return}
 // Do not fake a live banking connection. This becomes live when an OAuth/API endpoint is configured.
 localStorage.setItem('flosi-bank-link-requested','1');
 m.className='bankWarn';m.textContent='تم تجهيز إعداد الربط. الربط الحي يحتاج API / OAuth رسمي من المصرف؛ عند توفيره يفتح Flosi صفحة التفويض الآمنة للمصرف ثم يستورد الحركات الجديدة فقط.';
};
document.getElementById('bankImportStatement').onclick=()=>{
 const input=document.createElement('input');input.type='file';input.accept='.csv,.ofx,.qfx,.xlsx,.pdf';input.onchange=()=>{const m=document.getElementById('bankMessage');m.className='bankOk';m.textContent=input.files&&input.files[0]?`تم اختيار ${input.files[0].name} للمراجعة قبل الاستيراد.`:'لم يتم اختيار ملف.'};input.click();
};
renderProviders();loadSettings();
})();
