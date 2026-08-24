(()=>{
'use strict';
if(window.__FLOSI_TX_CAPSULE__)return;window.__FLOSI_TX_CAPSULE__=true;
const $=(s,r=document)=>r.querySelector(s);
const esc=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
function installStyle(){if($('#flosiTxCapsuleStyle'))return;const st=document.createElement('style');st.id='flosiTxCapsuleStyle';st.textContent=`
html,body,*{scrollbar-width:none!important;-ms-overflow-style:none!important}
html::-webkit-scrollbar,body::-webkit-scrollbar,*::-webkit-scrollbar{display:none!important;width:0!important;height:0!important;background:transparent!important}
#addModal .sheet{overflow:auto!important;scrollbar-width:none!important;-ms-overflow-style:none!important}
#addModal .sheet::-webkit-scrollbar{display:none!important;width:0!important;height:0!important}
#addModal .txTypeNative,#addModal .txPillNative{position:absolute!important;opacity:0!important;pointer-events:none!important;width:1px!important;height:1px!important;margin:0!important;padding:0!important}
#addModal .txTypeCapsule,#addModal .txPillSelect{position:relative}
#addModal .txTypeButton{width:100%;height:52px;border:1.5px solid #b684ff;background:linear-gradient(180deg,#fff,#fbf9ff);border-radius:999px;display:grid;grid-template-columns:42px 1fr 42px;align-items:center;padding:0 8px;color:#17131f;box-shadow:0 6px 18px rgba(123,68,239,.08);cursor:pointer}
#addModal .txTypeButton:focus-visible,#addModal .txPillButton:focus-visible{outline:0;box-shadow:0 0 0 4px rgba(123,68,239,.12)}
#addModal .txTypeIcon{width:36px;height:36px;border-radius:50%;display:grid;place-items:center;font-family:Inter,system-ui,sans-serif;font-size:18px;font-weight:700}
#addModal .txTypeIcon.expense{background:#f3edff;color:#7b44ef}#addModal .txTypeIcon.income{background:#e8f9f2;color:#18b97d}
#addModal .txTypeValue{text-align:center;font-size:14px;font-weight:600;white-space:nowrap}
#addModal .txTypeChevron{display:grid;place-items:center;color:#7b44ef;font-size:16px;transition:.18s}.txTypeCapsule.open .txTypeChevron{transform:rotate(180deg)}
#addModal .txTypeMenu{position:absolute;z-index:230;left:0;right:0;top:58px;background:#fff;border:1px solid #eadff7;border-radius:22px;padding:8px;box-shadow:0 20px 48px rgba(51,32,75,.18);display:none;overflow:hidden}.txTypeCapsule.open .txTypeMenu{display:grid;gap:6px}
#addModal .txTypeOption{height:48px;border:0;border-radius:16px;background:#fff;display:grid;grid-template-columns:34px 1fr 34px;align-items:center;padding:0 10px;cursor:pointer;color:#17131f}.txTypeOption:hover{background:#faf7ff}.txTypeOption.active{background:linear-gradient(135deg,#f3edff,#faf7ff);color:#6d36de;font-weight:700}
#addModal .txTypeOption .mini{width:30px;height:30px;border-radius:50%;display:grid;place-items:center;font-family:Inter,system-ui,sans-serif}.txTypeOption .check{font-size:16px;color:#7b44ef;text-align:center}
#addModal .txMoreToggle{width:100%;height:42px;margin-top:4px;border:0;border-radius:14px;background:#f7f3fb;color:#6f6877;display:flex;align-items:center;justify-content:space-between;padding:0 12px;font-size:10px;font-weight:700;cursor:pointer}
#addModal .txMorePanel{display:none;margin-top:8px;padding-top:2px}.txMorePanel.open{display:block}
#addModal .txMoreGrid{display:grid;grid-template-columns:1fr 1fr;gap:8px}#addModal .txMoreGrid .field{margin:6px 0}
#addModal .txMorePanel input,#addModal .txMorePanel textarea{width:100%;border:1px solid #e7e0ef;border-radius:999px;background:#fff;padding:0 14px;outline:0;box-sizing:border-box}.txMorePanel input{height:46px}.txMorePanel textarea{min-height:70px;border-radius:18px;padding-top:10px;resize:vertical;font:inherit}
#addModal .txPillButton{width:100%;height:46px;border:1px solid #e4dced;background:#fff;border-radius:999px;display:grid;grid-template-columns:30px 1fr 30px;align-items:center;padding:0 10px;box-sizing:border-box;color:#17131f;font:inherit;cursor:pointer;box-shadow:0 3px 10px rgba(72,48,96,.035)}
#addModal .txPillValue{text-align:start;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;padding-inline:4px}
#addModal .txPillChevron{display:grid;place-items:center;color:#6d36de;font-size:16px;line-height:1;transition:transform .18s ease}.txPillSelect.open .txPillChevron{transform:rotate(180deg)}
#addModal .txPillSpacer{width:30px;height:30px}
#addModal .txPillMenu{position:absolute;z-index:260;top:52px;left:0;right:0;display:none;padding:7px;background:#fff;border:1px solid #e8e0f0;border-radius:20px;box-shadow:0 18px 42px rgba(52,35,72,.16);overflow:hidden}.txPillSelect.open .txPillMenu{display:grid;gap:4px}
#addModal .txPillOption{width:100%;min-height:42px;border:0;border-radius:14px;background:#fff;color:#17131f;display:grid;grid-template-columns:28px 1fr 28px;align-items:center;padding:0 9px;font:inherit;cursor:pointer;text-align:start}.txPillOption:hover{background:#faf7ff}.txPillOption.active{background:linear-gradient(135deg,#f1eaff,#faf7ff);color:#6334ce;font-weight:700}
#addModal .txPillCheck{display:grid;place-items:center;color:#6d36de;font-size:15px}.txPillOptionText{padding-inline:3px}
#addModal .txCheckRow{display:flex;align-items:center;gap:9px;padding:10px 12px;font-size:10px;color:#6f6877;border:1px solid #eee8f4;border-radius:16px;background:#faf8fd;margin-top:8px}.txCheckRow input{width:18px!important;height:18px!important;accent-color:#7b44ef}
@media(max-width:420px){#addModal .txMoreGrid{grid-template-columns:1fr 1fr;gap:8px}}
`;document.head.appendChild(st)}
function labels(){const lang=localStorage.getItem('flosi-lang-preview')||localStorage.getItem('flosi-lang')||'ar';return lang==='ar'?{expense:'مصروف',income:'دخل',more:'تفاصيل إضافية',category:'التصنيف',account:'الحساب',date:'التاريخ',note:'ملاحظة',repeat:'حركة متكررة',receipt:'إرفاق إيصال لاحقاً',general:'عام',main:'الحساب الرئيسي',cash:'نقداً',bank:'مصرف'}:{expense:'Expense',income:'Income',more:'More details',category:'Category',account:'Account',date:'Date',note:'Note',repeat:'Recurring transaction',receipt:'Attach receipt later',general:'General',main:'Main account',cash:'Cash',bank:'Bank'}}
function enhancePillSelect(sel){
 if(!sel||sel.dataset.pill==='1')return;
 sel.dataset.pill='1';sel.classList.add('txPillNative');
 const wrap=document.createElement('div');wrap.className='txPillSelect';
 wrap.innerHTML=`<button type="button" class="txPillButton" aria-haspopup="listbox" aria-expanded="false"><span class="txPillSpacer"></span><span class="txPillValue"></span><span class="txPillChevron">⌄</span></button><div class="txPillMenu" role="listbox"></div>`;
 sel.insertAdjacentElement('afterend',wrap);
 const btn=$('.txPillButton',wrap),value=$('.txPillValue',wrap),menu=$('.txPillMenu',wrap);
 [...sel.options].forEach((op,i)=>{const b=document.createElement('button');b.type='button';b.className='txPillOption';b.dataset.i=String(i);b.setAttribute('role','option');b.innerHTML=`<span class="txPillCheck"></span><span class="txPillOptionText">${esc(op.textContent)}</span><span></span>`;menu.appendChild(b)});
 const sync=()=>{const i=sel.selectedIndex<0?0:sel.selectedIndex;value.textContent=sel.options[i]?.textContent||'';menu.querySelectorAll('.txPillOption').forEach(o=>{const active=Number(o.dataset.i)===i;o.classList.toggle('active',active);o.setAttribute('aria-selected',String(active));$('.txPillCheck',o).textContent=active?'✓':''})};
 sync();
 btn.onclick=e=>{e.stopPropagation();document.querySelectorAll('#addModal .txPillSelect.open,#addModal .txTypeCapsule.open').forEach(x=>{if(x!==wrap)x.classList.remove('open')});wrap.classList.toggle('open');btn.setAttribute('aria-expanded',String(wrap.classList.contains('open')))};
 menu.querySelectorAll('.txPillOption').forEach(o=>o.onclick=e=>{e.stopPropagation();sel.selectedIndex=Number(o.dataset.i);sel.dispatchEvent(new Event('change',{bubbles:true}));wrap.classList.remove('open');btn.setAttribute('aria-expanded','false');sync()});
 sel.addEventListener('change',sync);
 document.addEventListener('click',e=>{if(!wrap.contains(e.target)){wrap.classList.remove('open');btn.setAttribute('aria-expanded','false')}});
}
function build(){
 installStyle();
 const sel=$('#txType');
 if(!sel)return;
 if(sel.dataset.capsule==='1'||$('#addModal .txTypeCapsule'))return;
 sel.dataset.capsule='1';sel.classList.add('txTypeNative');
 const L=labels();const wrap=document.createElement('div');wrap.className='txTypeCapsule';wrap.innerHTML=`<button type="button" class="txTypeButton" aria-haspopup="listbox" aria-expanded="false"><span class="txTypeIcon expense">↓</span><span class="txTypeValue"></span><span class="txTypeChevron">⌄</span></button><div class="txTypeMenu" role="listbox"><button type="button" class="txTypeOption" data-v="expense"><span class="mini" style="background:#f3edff;color:#7b44ef">↓</span><span>${esc(L.expense)}</span><span class="check"></span></button><button type="button" class="txTypeOption" data-v="income"><span class="mini" style="background:#e8f9f2;color:#18b97d">↑</span><span>${esc(L.income)}</span><span class="check"></span></button></div>`;sel.insertAdjacentElement('afterend',wrap);
 const btn=$('.txTypeButton',wrap),value=$('.txTypeValue',wrap),icon=$('.txTypeIcon',wrap);function sync(){const income=sel.value==='income';value.textContent=income?L.income:L.expense;icon.textContent=income?'↑':'↓';icon.className='txTypeIcon '+(income?'income':'expense');wrap.querySelectorAll('.txTypeOption').forEach(o=>{const active=o.dataset.v===sel.value;o.classList.toggle('active',active);$('.check',o).textContent=active?'✓':''})}sync();btn.onclick=e=>{e.stopPropagation();document.querySelectorAll('#addModal .txPillSelect.open').forEach(x=>x.classList.remove('open'));wrap.classList.toggle('open');btn.setAttribute('aria-expanded',String(wrap.classList.contains('open')))};wrap.querySelectorAll('.txTypeOption').forEach(o=>o.onclick=e=>{e.stopPropagation();sel.value=o.dataset.v;sel.dispatchEvent(new Event('change',{bubbles:true}));wrap.classList.remove('open');btn.setAttribute('aria-expanded','false');sync()});document.addEventListener('click',e=>{if(!wrap.contains(e.target)){wrap.classList.remove('open');btn.setAttribute('aria-expanded','false')}});
 const sheet=sel.closest('.sheet');const actionRow=sheet&&sheet.lastElementChild;if(sheet&&actionRow&&!$('#txMoreToggle',sheet)&&!$('.txExtraToggle',sheet)){const more=document.createElement('div');more.innerHTML=`<button type="button" id="txMoreToggle" class="txMoreToggle"><span>${esc(L.more)}</span><span>＋</span></button><div id="txMorePanel" class="txMorePanel"><div class="txMoreGrid"><div class="field"><label>${esc(L.category)}</label><select id="txCategory"><option value="general">${esc(L.general)}</option><option value="food">طعام / Food</option><option value="shopping">تسوق / Shopping</option><option value="transport">مواصلات / Transport</option><option value="bills">فواتير / Bills</option><option value="salary">راتب / Salary</option></select></div><div class="field"><label>${esc(L.account)}</label><select id="txAccount"><option value="main">${esc(L.main)}</option><option value="cash">${esc(L.cash)}</option><option value="bank">${esc(L.bank)}</option></select></div></div><div class="field"><label>${esc(L.date)}</label><input id="txDate" type="date"></div><div class="field"><label>${esc(L.note)}</label><textarea id="txNote" maxlength="240"></textarea></div><label class="txCheckRow"><input id="txRecurring" type="checkbox"><span>${esc(L.repeat)}</span></label><label class="txCheckRow"><input id="txReceiptLater" type="checkbox"><span>${esc(L.receipt)}</span></label></div>`;sheet.insertBefore(more,actionRow);const tog=$('#txMoreToggle',sheet),panel=$('#txMorePanel',sheet);tog.onclick=()=>{panel.classList.toggle('open');tog.lastElementChild.textContent=panel.classList.contains('open')?'−':'＋'};const d=$('#txDate',sheet);if(d&&!d.value)d.value=new Date().toISOString().slice(0,10);enhancePillSelect($('#txCategory',sheet));enhancePillSelect($('#txAccount',sheet))}
 const save=$('#saveTx');if(save&&!save.dataset.extraHook){save.dataset.extraHook='1';save.addEventListener('click',()=>{const meta={category:$('#txCategory')?.selectedOptions?.[0]?.textContent||L.general,account:$('#txAccount')?.selectedOptions?.[0]?.textContent||L.main,date:$('#txDate')?.value||'',note:$('#txNote')?.value||'',recurring:!!$('#txRecurring')?.checked,receiptLater:!!$('#txReceiptLater')?.checked};localStorage.setItem('flosi-last-tx-meta',JSON.stringify(meta));setTimeout(()=>{const small=$('#recent .tx:first-child .txText small');if(small)small.textContent=[meta.account,meta.category].filter(Boolean).join(' • ')},0)})}
}
function boot(){build()}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',boot,{once:true});else boot();
})();
