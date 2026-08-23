(()=>{
'use strict';
window.__FLOSI_SECURITY_RESET__=true;

const STYLE_ID='flosi-ui-hardening-v3';
if(!document.getElementById(STYLE_ID)){
  const s=document.createElement('style');
  s.id=STYLE_ID;
  s.textContent=`
  .localeSelectWrap{position:relative!important}
  #settingsLang.localeSelect,#settingsCurrency.localeSelect{appearance:none!important;-webkit-appearance:none!important;color:transparent!important;caret-color:transparent!important;padding-inline:56px!important;background-image:linear-gradient(45deg,transparent 50%,#7b44ef 50%),linear-gradient(135deg,#7b44ef 50%,transparent 50%),linear-gradient(180deg,#fff,#faf8ff)!important;background-position:36px 22px,42px 22px,0 0!important;background-size:7px 7px,7px 7px,100% 100%!important;background-repeat:no-repeat!important}
  #settingsLang.localeSelect option,#settingsCurrency.localeSelect option{color:#17131f!important;background:#fff!important}
  .flosiCenteredSelectValue{position:absolute!important;inset:0!important;display:flex!important;align-items:center!important;justify-content:center!important;padding:0 58px!important;pointer-events:none!important;color:#17131f!important;font:inherit!important;line-height:1!important;text-align:center!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important;z-index:2!important}

  .flosiTxTypeNative{position:absolute!important;opacity:0!important;pointer-events:none!important;width:1px!important;height:1px!important;overflow:hidden!important}
  .flosiTxTypeWrap{position:relative;margin-top:2px}
  .flosiTxTypeButton{width:100%;height:54px;border:1.5px solid #b987ff;background:linear-gradient(180deg,#fff,#fbf9ff);border-radius:999px;display:flex;align-items:center;justify-content:space-between;gap:12px;padding:0 17px;box-shadow:0 0 0 4px rgba(123,68,239,.06),0 8px 22px rgba(63,40,92,.06);cursor:pointer;color:#17131f}
  .flosiTxTypeButton:focus-visible{outline:none;box-shadow:0 0 0 4px rgba(123,68,239,.13),0 8px 22px rgba(63,40,92,.08)}
  .flosiTxTypeMain{display:flex;align-items:center;gap:10px;min-width:0}
  .flosiTxTypeIcon{width:34px;height:34px;border-radius:50%;display:grid;place-items:center;background:#f2eaff;color:#7b44ef;font-size:17px;flex:0 0 34px}
  .flosiTxTypeLabel{font-size:13px;font-weight:600;white-space:nowrap}
  .flosiTxChevron{color:#7b44ef;font-size:16px;transition:transform .18s ease}
  .flosiTxTypeWrap.open .flosiTxChevron{transform:rotate(180deg)}
  .flosiTxTypeMenu{position:absolute;left:0;right:0;top:calc(100% + 8px);z-index:220;background:#fff;border:1px solid #e7ddf5;border-radius:22px;padding:8px;box-shadow:0 24px 60px rgba(41,25,61,.22),0 8px 22px rgba(41,25,61,.08);display:none;overflow:hidden}
  .flosiTxTypeWrap.open .flosiTxTypeMenu{display:block;animation:flosiMenuIn .16s ease-out}
  .flosiTxTypeOption{width:100%;height:50px;border:0;background:#fff;border-radius:15px;padding:0 13px;display:flex;align-items:center;justify-content:space-between;gap:10px;color:#17131f;cursor:pointer}
  .flosiTxTypeOption+.flosiTxTypeOption{margin-top:5px}
  .flosiTxTypeOption.active{background:linear-gradient(135deg,#f2eaff,#eee4ff);color:#6f35df;font-weight:700}
  .flosiTxTypeOption .left{display:flex;align-items:center;gap:10px}
  .flosiTxTypeOption .ico{width:32px;height:32px;border-radius:50%;display:grid;place-items:center;background:#f6f2fb;font-size:15px}
  .flosiTxTypeOption[data-value=income] .ico{background:#e8f9f2;color:#18b97d}
  .flosiTxTypeOption[data-value=expense] .ico{background:#f2eaff;color:#7b44ef}
  .flosiTxTypeCheck{width:25px;height:25px;border-radius:50%;display:grid;place-items:center;background:#7b44ef;color:#fff;opacity:0;transform:scale(.8);transition:.15s}
  .flosiTxTypeOption.active .flosiTxTypeCheck{opacity:1;transform:scale(1)}
  @keyframes flosiMenuIn{from{opacity:0;transform:translateY(-5px) scale(.985)}to{opacity:1;transform:none}}

  .flosiTxExtras{margin-top:12px;border:1px solid #ece6f4;border-radius:18px;background:#fbfaff;overflow:hidden}
  .flosiTxExtrasToggle{width:100%;min-height:48px;border:0;background:transparent;padding:0 14px;display:flex;align-items:center;justify-content:space-between;color:#6d6575;cursor:pointer;font-weight:600}
  .flosiTxExtrasToggle span:last-child{color:#7b44ef;transition:transform .18s}
  .flosiTxExtras.open .flosiTxExtrasToggle span:last-child{transform:rotate(180deg)}
  .flosiTxExtrasBody{display:none;padding:0 12px 12px}
  .flosiTxExtras.open .flosiTxExtrasBody{display:block}
  .flosiTxExtraGrid{display:grid;grid-template-columns:1fr 1fr;gap:9px}
  .flosiTxExtraField{display:grid;gap:5px;margin-top:9px}
  .flosiTxExtraField.full{grid-column:1/-1}
  .flosiTxExtraField label{font-size:9px!important;color:#7b7480!important;font-weight:600!important}
  .flosiTxExtraField input,.flosiTxExtraField select,.flosiTxExtraField textarea{width:100%;border:1px solid #e5deed;border-radius:13px;background:#fff;padding:0 11px;outline:0}
  .flosiTxExtraField input,.flosiTxExtraField select{height:43px}.flosiTxExtraField textarea{min-height:70px;padding-top:10px;resize:vertical}
  .flosiTxExtraField input:focus,.flosiTxExtraField select:focus,.flosiTxExtraField textarea:focus{border-color:#b489ff;box-shadow:0 0 0 3px rgba(123,68,239,.08)}
  .flosiTxToggleRow{grid-column:1/-1;display:flex;align-items:center;justify-content:space-between;gap:12px;padding:10px 1px 0;color:#6d6575;font-size:10px}
  .flosiTxToggleRow input{accent-color:#7b44ef;width:18px;height:18px}
  @media(max-width:420px){.flosiTxExtraGrid{grid-template-columns:1fr}}
  `;
  document.head.appendChild(s);
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

const AR=/[\u0600-\u06ff\u0750-\u077f\u08a0-\u08ff]/;
const originalText=new WeakMap();
const fallback={
'إعدادات عالمية':'Global settings','اللغة والعملة':'Language & currency','لغة التطبيق':'App language','العملة الأساسية':'Base currency','كل اللغات':'All languages','كل العملات':'All currencies','معاينة فورية':'Live preview','حفظ وتطبيق الإعدادات':'Save & apply settings','اليوم':'Today','الحركات':'Transactions','الذكاء':'Insights','أنا':'Me','صباح الخير':'Good morning','الأمان':'Security','الخصوصية':'Privacy','إضافة حركة':'Add transaction','البيان':'Description','المبلغ':'Amount','النوع':'Type','مصروف':'Expense','دخل':'Income','حفظ':'Save','إلغاء':'Cancel','تفاصيل إضافية':'More details','التصنيف':'Category','الحساب':'Account','التاريخ':'Date','ملاحظة':'Note','حركة متكررة':'Recurring transaction','إرفاق إيصال':'Attach receipt','الحساب الرئيسي':'Main account','عام':'General'
};
function currentLang(){return localStorage.getItem('flosi-lang-preview')||localStorage.getItem('flosi-lang')||'ar'}
function reverseArabic(){const m=new Map(),d=window.FLOSI_I18N&&window.FLOSI_I18N.ar&&window.FLOSI_I18N.ar.t;if(d)Object.entries(d).forEach(([k,v])=>typeof v==='string'&&m.set(v,k));return m}
function translateText(base,lang,rev){
  if(lang==='ar')return base;const t=base.trim();if(!t)return base;
  const key=rev.get(t),dict=window.FLOSI_I18N&&window.FLOSI_I18N[lang]&&window.FLOSI_I18N[lang].t;
  let out=key&&dict&&dict[key]?dict[key]:fallback[t];
  if(!out&&AR.test(t))out=t.replace(/[\u0600-\u06ff\u0750-\u077f\u08a0-\u08ff]+(?:\s+[\u0600-\u06ff\u0750-\u077f\u08a0-\u08ff]+)*/g,'').replace(/\s{2,}/g,' ').trim();
  if(!out)return base;const i=base.indexOf(t);return base.slice(0,i)+out+base.slice(i+t.length);
}
let translating=false;
function hardenLocale(){
  if(translating)return;translating=true;
  try{const lang=currentLang();if(lang==='ar')return;const rev=reverseArabic();const w=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT);const ns=[];while(w.nextNode())ns.push(w.currentNode);ns.forEach(n=>{const p=n.parentElement;if(!p||p.closest('script,style,select,option,textarea,input,.flosiCenteredSelectValue,[data-locale-no-transform]'))return;if(!originalText.has(n))originalText.set(n,n.nodeValue||'');const b=originalText.get(n)||'',x=translateText(b,lang,rev);if(x!==n.nodeValue)n.nodeValue=x})}finally{translating=false}
}

function ensureTransactionTypeCapsule(){
  const select=document.getElementById('txType');
  if(!select||select.dataset.flosiCapsule==='1')return;
  select.dataset.flosiCapsule='1';select.classList.add('flosiTxTypeNative');
  const field=select.closest('.field')||select.parentElement;if(!field)return;
  const wrap=document.createElement('div');wrap.className='flosiTxTypeWrap';wrap.setAttribute('data-locale-no-transform','');
  wrap.innerHTML=`<button type="button" class="flosiTxTypeButton" aria-haspopup="listbox" aria-expanded="false"><span class="flosiTxTypeMain"><span class="flosiTxTypeIcon">↘</span><span class="flosiTxTypeLabel"></span></span><span class="flosiTxChevron">⌄</span></button><div class="flosiTxTypeMenu" role="listbox"><button type="button" class="flosiTxTypeOption" data-value="expense"><span class="left"><span class="ico">↘</span><span>مصروف</span></span><span class="flosiTxTypeCheck">✓</span></button><button type="button" class="flosiTxTypeOption" data-value="income"><span class="left"><span class="ico">↗</span><span>دخل</span></span><span class="flosiTxTypeCheck">✓</span></button></div>`;
  select.insertAdjacentElement('afterend',wrap);
  const btn=wrap.querySelector('.flosiTxTypeButton'),label=wrap.querySelector('.flosiTxTypeLabel'),icon=wrap.querySelector('.flosiTxTypeIcon');
  const optionText=v=>v==='income'?'دخل':'مصروف';
  const sync=()=>{const v=select.value==='income'?'income':'expense';label.textContent=optionText(v);icon.textContent=v==='income'?'↗':'↘';icon.style.color=v==='income'?'#18b97d':'#7b44ef';icon.style.background=v==='income'?'#e8f9f2':'#f2eaff';wrap.querySelectorAll('.flosiTxTypeOption').forEach(o=>o.classList.toggle('active',o.dataset.value===v))};
  btn.addEventListener('click',e=>{e.stopPropagation();const open=wrap.classList.toggle('open');btn.setAttribute('aria-expanded',String(open))});
  wrap.querySelectorAll('.flosiTxTypeOption').forEach(o=>o.addEventListener('click',()=>{select.value=o.dataset.value;select.dispatchEvent(new Event('change',{bubbles:true}));wrap.classList.remove('open');btn.setAttribute('aria-expanded','false');sync()}));
  document.addEventListener('click',e=>{if(!wrap.contains(e.target)){wrap.classList.remove('open');btn.setAttribute('aria-expanded','false')}});
  select.addEventListener('change',sync);sync();

  if(!document.getElementById('flosiTxExtras')){
    const extras=document.createElement('div');extras.id='flosiTxExtras';extras.className='flosiTxExtras';extras.setAttribute('data-locale-no-transform','');
    extras.innerHTML=`<button type="button" class="flosiTxExtrasToggle"><span>تفاصيل إضافية</span><span>⌄</span></button><div class="flosiTxExtrasBody"><div class="flosiTxExtraGrid"><div class="flosiTxExtraField"><label>التصنيف</label><select id="txCategory"><option>عام</option><option>طعام</option><option>تسوق</option><option>مواصلات</option><option>فواتير</option><option>راتب</option></select></div><div class="flosiTxExtraField"><label>الحساب</label><select id="txAccount"><option>الحساب الرئيسي</option><option>نقداً</option><option>مصرف</option></select></div><div class="flosiTxExtraField full"><label>التاريخ</label><input id="txDate" type="datetime-local"></div><div class="flosiTxExtraField full"><label>ملاحظة</label><textarea id="txNote" placeholder="اختياري"></textarea></div><label class="flosiTxToggleRow"><span>حركة متكررة</span><input id="txRecurring" type="checkbox"></label><label class="flosiTxToggleRow"><span>إرفاق إيصال</span><input id="txReceipt" type="checkbox"></label></div></div>`;
    field.insertAdjacentElement('afterend',extras);
    extras.querySelector('.flosiTxExtrasToggle').addEventListener('click',()=>extras.classList.toggle('open'));
    const d=extras.querySelector('#txDate');if(d&&!d.value){const n=new Date(),off=n.getTimezoneOffset();d.value=new Date(n.getTime()-off*60000).toISOString().slice(0,16)}
  }
}

function refresh(){installCenteredSelectors();ensureTransactionTypeCapsule();requestAnimationFrame(hardenLocale)}
refresh();
document.addEventListener('DOMContentLoaded',refresh,{once:true});
document.addEventListener('change',e=>{if(e.target&&['settingsLang','settingsCurrency'].includes(e.target.id))setTimeout(refresh,0)});
document.addEventListener('click',e=>{if(e.target.closest('#settingsSaveLocale,#localeApplySmart,[data-go],#addBtn,#quickAdd'))setTimeout(refresh,0)});
new MutationObserver(ms=>{if(translating)return;if(ms.some(m=>m.addedNodes&&m.addedNodes.length))requestAnimationFrame(refresh)}).observe(document.documentElement,{childList:true,subtree:true});
setTimeout(refresh,0);
})();
