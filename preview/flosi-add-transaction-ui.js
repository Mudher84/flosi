(()=>{
'use strict';
if(window.__FLOSI_ADD_TX_POLISH__)return;
window.__FLOSI_ADD_TX_POLISH__=true;

const style=document.createElement('style');
style.id='flosi-add-tx-polish-style';
style.textContent=`
#addModal .sheet{overflow:visible!important}
#addModal .field select#txType{position:absolute!important;opacity:0!important;pointer-events:none!important;width:1px!important;height:1px!important}
.txTypeCapsule{position:relative;margin-top:4px}
.txTypeButton{width:100%;height:52px;border:1.5px solid #b98cff;background:linear-gradient(180deg,#fff,#fbf9ff);border-radius:999px;display:grid;grid-template-columns:44px 1fr 44px;align-items:center;padding:0 10px;color:#17131f;box-shadow:0 7px 20px rgba(96,57,150,.07);cursor:pointer}
.txTypeButton:focus{outline:0;box-shadow:0 0 0 4px rgba(123,68,239,.10),0 7px 20px rgba(96,57,150,.07)}
.txTypeButton .txTypeIcon{width:34px;height:34px;border-radius:50%;display:grid;place-items:center;background:#f2ebff;color:#7b44ef;font-size:16px;font-weight:700}
.txTypeButton .txTypeLabel{text-align:center;font-weight:700;font-size:13px}
.txTypeButton .txTypeChevron{color:#7b44ef;text-align:center;font-size:18px;transition:.18s}
.txTypeCapsule.open .txTypeChevron{transform:rotate(180deg)}
.txTypeMenu{position:absolute;left:0;right:0;top:59px;background:#fff;border:1px solid #e7dcf7;border-radius:22px;padding:8px;box-shadow:0 22px 55px rgba(52,33,78,.18);z-index:220;display:none;overflow:hidden}
.txTypeCapsule.open .txTypeMenu{display:block;animation:txPop .16s ease-out}
.txTypeOption{width:100%;border:0;background:#fff;border-radius:15px;min-height:48px;padding:8px 12px;display:grid;grid-template-columns:36px 1fr 28px;align-items:center;gap:6px;color:#17131f;cursor:pointer}
.txTypeOption+.txTypeOption{margin-top:4px}
.txTypeOption:hover{background:#faf7ff}.txTypeOption.active{background:linear-gradient(135deg,#f2eaff,#f8f3ff);color:#6f35de}
.txTypeOption .ico{width:32px;height:32px;border-radius:11px;display:grid;place-items:center;background:#f2ebff;color:#7b44ef;font-weight:800}.txTypeOption[data-value="income"] .ico{background:#e8f9f2;color:#18b97d}
.txTypeOption .txt{text-align:center;font-size:12px;font-weight:700}.txTypeOption .check{text-align:center;font-size:15px;color:#7b44ef}
.txExtraToggle{width:100%;border:0;background:#f7f3fc;color:#6d6476;border-radius:14px;height:42px;margin-top:4px;font-weight:700;font-size:10px;cursor:pointer}
.txExtra{display:none;margin-top:10px;padding-top:2px}.txExtra.open{display:block}
.txChipRow{display:flex;gap:7px;flex-wrap:wrap;margin-top:7px}.txChip{border:1px solid #e8e1ef;background:#fff;border-radius:999px;padding:7px 10px;font-size:9px;color:#6e6578}.txChip.active{border-color:#b98cff;background:#f4eeff;color:#733be5;font-weight:700}
#addModal .txExtra textarea{width:100%;min-height:74px;resize:vertical;border:1px solid #e7e0ef;border-radius:15px;background:#faf9fc;padding:11px 13px;outline:0;font:inherit}
#addModal .txExtra textarea:focus{border-color:#b489ff;background:#fff;box-shadow:0 0 0 4px rgba(123,68,239,.09)}
.txToggleRow{display:flex;align-items:center;gap:10px;background:#faf8fd;border:1px solid #eee8f4;border-radius:15px;padding:10px 12px;margin-top:9px}.txToggleRow span{flex:1;font-size:10px}.txToggleRow input{accent-color:#7b44ef;width:18px;height:18px}
@keyframes txPop{from{opacity:0;transform:translateY(-5px) scale(.98)}to{opacity:1;transform:none}}
html[dir=ltr] .txTypeButton,html[dir=ltr] .txTypeOption{direction:ltr}
`;
document.head.appendChild(style);

function install(){
 const modal=document.getElementById('addModal');
 const select=document.getElementById('txType');
 if(!modal||!select||modal.dataset.txPolished==='1')return;
 modal.dataset.txPolished='1';
 const field=select.closest('.field');
 if(!field)return;
 const capsule=document.createElement('div');
 capsule.className='txTypeCapsule';
 capsule.innerHTML=`<button type="button" class="txTypeButton" aria-haspopup="listbox" aria-expanded="false"><span class="txTypeIcon">↘</span><span class="txTypeLabel">مصروف</span><span class="txTypeChevron">⌄</span></button><div class="txTypeMenu" role="listbox"><button type="button" class="txTypeOption active" data-value="expense"><span class="ico">↘</span><span class="txt">مصروف</span><span class="check">✓</span></button><button type="button" class="txTypeOption" data-value="income"><span class="ico">↗</span><span class="txt">دخل</span><span class="check"></span></button></div>`;
 field.appendChild(capsule);
 const button=capsule.querySelector('.txTypeButton'),label=capsule.querySelector('.txTypeLabel'),icon=capsule.querySelector('.txTypeIcon');
 const sync=()=>{const income=select.value==='income';label.textContent=income?'دخل':'مصروف';icon.textContent=income?'↗':'↘';icon.style.background=income?'#e8f9f2':'#f2ebff';icon.style.color=income?'#18b97d':'#7b44ef';capsule.querySelectorAll('.txTypeOption').forEach(o=>{const active=o.dataset.value===select.value;o.classList.toggle('active',active);o.querySelector('.check').textContent=active?'✓':''})};
 button.onclick=()=>{const open=!capsule.classList.contains('open');capsule.classList.toggle('open',open);button.setAttribute('aria-expanded',String(open))};
 capsule.querySelectorAll('.txTypeOption').forEach(o=>o.onclick=()=>{select.value=o.dataset.value;select.dispatchEvent(new Event('change',{bubbles:true}));capsule.classList.remove('open');button.setAttribute('aria-expanded','false');sync()});
 document.addEventListener('click',e=>{if(!capsule.contains(e.target)){capsule.classList.remove('open');button.setAttribute('aria-expanded','false')}});
 sync();

 const actions=modal.querySelector('.sheet>div:last-child');
 const anchor=actions||field;
 const extras=document.createElement('div');
 extras.innerHTML=`<button type="button" class="txExtraToggle">+ تفاصيل إضافية</button><div class="txExtra"><div class="field"><label>التصنيف</label><div class="txChipRow" id="txCategoryChips"><button type="button" class="txChip active">عام</button><button type="button" class="txChip">طعام</button><button type="button" class="txChip">تسوق</button><button type="button" class="txChip">فواتير</button><button type="button" class="txChip">مواصلات</button><button type="button" class="txChip">راتب</button></div></div><div class="field"><label>الحساب</label><select id="txAccount"><option>الحساب الرئيسي</option><option>نقداً</option><option>حساب مصرفي</option></select></div><div class="field"><label>التاريخ</label><input id="txDate" type="date"></div><div class="field"><label>ملاحظة</label><textarea id="txNote" placeholder="ملاحظة اختيارية عن الحركة"></textarea></div><label class="txToggleRow"><span>حركة متكررة</span><input id="txRecurring" type="checkbox"></label><label class="txToggleRow"><span>إرفاق إيصال لاحقاً</span><input id="txReceipt" type="checkbox"></label></div>`;
 anchor.parentNode.insertBefore(extras,anchor);
 const extra=extras.querySelector('.txExtra'),toggle=extras.querySelector('.txExtraToggle');
 toggle.onclick=()=>{const open=!extra.classList.contains('open');extra.classList.toggle('open',open);toggle.textContent=open?'− إخفاء التفاصيل الإضافية':'+ تفاصيل إضافية'};
 extras.querySelectorAll('.txChip').forEach(ch=>ch.onclick=()=>{extras.querySelectorAll('.txChip').forEach(x=>x.classList.remove('active'));ch.classList.add('active')});
 const date=extras.querySelector('#txDate');if(date)date.value=new Date().toISOString().slice(0,10);
 }

install();
new MutationObserver(install).observe(document.documentElement,{childList:true,subtree:true});
})();
