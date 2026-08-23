(()=>{
'use strict';
if(window.__FLOSI_GOALS__)return;
window.__FLOSI_GOALS__=true;

const STORAGE_KEY='flosi-preview-goals-v1';
const DEFAULTS=[
  {id:'travel',name:'السفر',target:0,saved:0,currency:'IQD',deadline:''},
  {id:'emergency',name:'صندوق الطوارئ',target:0,saved:0,currency:'IQD',deadline:''}
];

function selectedCurrency(){return (localStorage.getItem('flosi-currency')||'IQD').toUpperCase()}
function locale(){return localStorage.getItem('flosi-locale')||'ar-IQ'}
function digits(currency){return ['IQD','JPY','KRW'].includes(currency)?0:2}
function money(value,currency){
  try{return new Intl.NumberFormat(locale(),{style:'currency',currency,maximumFractionDigits:digits(currency)}).format(Number(value)||0)}
  catch(_){return `${Number(value||0).toLocaleString('en-US')} ${currency}`}
}
function read(){
  try{
    const parsed=JSON.parse(localStorage.getItem(STORAGE_KEY)||'null');
    if(parsed&&parsed.version===1&&Array.isArray(parsed.goals))return parsed.goals;
  }catch(_){ }
  const currency=selectedCurrency();
  const goals=DEFAULTS.map(g=>({...g,currency}));
  write(goals);return goals;
}
function write(goals){localStorage.setItem(STORAGE_KEY,JSON.stringify({version:1,goals}))}
function toast(msg){
  if(typeof window.toast==='function'){window.toast(msg);return}
  const t=document.getElementById('toast');if(!t)return;t.textContent=msg;t.classList.add('show');setTimeout(()=>t.classList.remove('show'),1500)
}
function esc(s){return String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]))}
function progress(g){const target=Number(g.target)||0,saved=Math.max(0,Number(g.saved)||0);return target>0?Math.max(0,Math.min(100,Math.round(saved/target*100))):0}

function ensureStyle(){
  if(document.getElementById('flosiGoalsStyle'))return;
  const s=document.createElement('style');s.id='flosiGoalsStyle';s.textContent=`
  #flosiGoalList{display:grid;gap:13px}.flosiGoalToolbar{display:flex;align-items:center;justify-content:space-between;margin:-2px 2px 12px}.flosiGoalToolbar b{font-size:13px}.flosiGoalAdd{border:0;background:#f2ebff;color:var(--p);border-radius:12px;padding:8px 11px;font-size:9px;font-weight:700}.flosiGoalCard{background:#fff;border:1px solid var(--line);border-radius:22px;padding:14px 15px;box-shadow:var(--shadow);display:flex;align-items:center;gap:13px;cursor:pointer}.flosiGoalCard:active{transform:scale(.994)}.flosiGoalRing{width:54px;height:54px;border-radius:50%;display:grid;place-items:center;position:relative;flex:0 0 54px}.flosiGoalRing:after{content:"";position:absolute;width:40px;height:40px;background:#fff;border-radius:50%}.flosiGoalRing b{position:relative;z-index:1;font-size:10px}.flosiGoalCopy{flex:1;min-width:0}.flosiGoalCopy b{display:block;font-size:12px}.flosiGoalCopy small{display:block;color:var(--muted);font-size:9px;margin-top:3px}.flosiGoalEdit{width:34px;height:34px;border:0;border-radius:11px;background:#f6f1ff;color:var(--p);display:grid;place-items:center;font-size:15px}.flosiGoalEmpty{background:#fff;border:1px dashed #ddd4e9;border-radius:22px;padding:24px;text-align:center;color:var(--muted);font-size:10px}.flosiGoalModal{position:fixed;inset:0;background:rgba(20,14,29,.58);z-index:220;display:none;align-items:center;justify-content:center;padding:18px;backdrop-filter:blur(10px)}.flosiGoalModal.open{display:flex}.flosiGoalSheet{width:min(100%,470px);background:#fff;border-radius:26px;padding:20px;box-shadow:0 28px 70px rgba(25,16,38,.28)}.flosiGoalSheet h3{margin:0 0 15px;font-size:18px}.flosiGoalField{display:grid;gap:6px;margin:11px 0}.flosiGoalField label{font-size:9px;color:var(--muted);font-weight:700}.flosiGoalField input{height:48px;border:1px solid #e6dff0;background:#faf9fc;border-radius:14px;padding:0 13px;outline:0}.flosiGoalField input:focus{border-color:#aa7cff;box-shadow:0 0 0 4px #7b44ef12}.flosiGoalHint{font-size:8px;line-height:1.8;color:var(--muted);background:#f8f5fc;border-radius:13px;padding:10px;margin-top:10px}.flosiGoalActions{display:grid;grid-template-columns:1fr 1fr;gap:9px;margin-top:16px}.flosiGoalActions button{height:46px;border:0;border-radius:14px;font-size:10px;font-weight:700}.flosiGoalSave{background:linear-gradient(145deg,#955bff,#7138eb);color:#fff}.flosiGoalCancel{background:#f1eef6;color:#615a69}.flosiGoalDelete{width:100%;margin-top:9px;height:42px;border:0;border-radius:13px;background:#fff1f3;color:var(--red);font-size:9px;font-weight:700}
  `;document.head.appendChild(s)
}

function ensureModal(){
  let modal=document.getElementById('flosiGoalModal');if(modal)return modal;
  modal=document.createElement('div');modal.id='flosiGoalModal';modal.className='flosiGoalModal';modal.setAttribute('data-locale-no-transform','');
  modal.innerHTML=`<div class="flosiGoalSheet"><h3 id="flosiGoalTitle">تعديل الهدف</h3><div class="flosiGoalField"><label>اسم الهدف</label><input id="flosiGoalName" maxlength="40"></div><div class="flosiGoalField"><label id="flosiGoalTargetLabel">المبلغ المستهدف</label><input id="flosiGoalTarget" inputmode="decimal" type="number" min="0"></div><div class="flosiGoalField"><label>تاريخ الوصول المستهدف</label><input id="flosiGoalDeadline" type="date"></div><div class="flosiGoalHint">المبلغ المدخر لا يتغير من شاشة التعديل؛ يتغير من عمليات الادخار المرتبطة بالهدف حتى تبقى الحسابات المالية صحيحة.</div><div class="flosiGoalActions"><button class="flosiGoalSave" id="flosiGoalSave">حفظ</button><button class="flosiGoalCancel" id="flosiGoalCancel">إلغاء</button></div><button class="flosiGoalDelete" id="flosiGoalDelete">حذف الهدف</button></div>`;
  document.body.appendChild(modal);
  modal.addEventListener('click',e=>{if(e.target===modal)closeEditor()});
  document.getElementById('flosiGoalCancel').onclick=closeEditor;
  document.addEventListener('keydown',e=>{if(e.key==='Escape'&&modal.classList.contains('open'))closeEditor()});
  return modal;
}

let editingId=null;
function openEditor(id=null){
  const goals=read(),goal=id?goals.find(g=>g.id===id):null,currency=goal?.currency||selectedCurrency();editingId=id;
  ensureModal();document.getElementById('flosiGoalTitle').textContent=goal?'تعديل الهدف':'هدف جديد';document.getElementById('flosiGoalName').value=goal?.name||'';document.getElementById('flosiGoalTarget').value=goal&&Number(goal.target)>0?String(goal.target):'';document.getElementById('flosiGoalDeadline').value=goal?.deadline||'';document.getElementById('flosiGoalTargetLabel').textContent=`المبلغ المستهدف (${currency})`;document.getElementById('flosiGoalDelete').style.display=goal?'block':'none';document.getElementById('flosiGoalModal').classList.add('open');setTimeout(()=>document.getElementById('flosiGoalName').focus(),60)
}
function closeEditor(){document.getElementById('flosiGoalModal')?.classList.remove('open');editingId=null}
function saveEditor(){
  const name=document.getElementById('flosiGoalName').value.trim(),target=Number(document.getElementById('flosiGoalTarget').value||0),deadline=document.getElementById('flosiGoalDeadline').value;
  if(!name){toast('اكتب اسم الهدف');return}if(!Number.isFinite(target)||target<0){toast('أدخل مبلغاً مستهدفاً صحيحاً');return}
  const goals=read();if(editingId){const i=goals.findIndex(g=>g.id===editingId);if(i>=0)goals[i]={...goals[i],name,target,deadline}}else{goals.push({id:`goal-${Date.now()}`,name,target,saved:0,currency:selectedCurrency(),deadline})}
  write(goals);closeEditor();render();toast('تم حفظ الهدف')
}
function deleteEditor(){if(!editingId)return;const goals=read().filter(g=>g.id!==editingId);write(goals);closeEditor();render();toast('تم حذف الهدف')}

function render(){
  const plan=document.getElementById('plan');if(!plan)return;
  ensureStyle();ensureModal();
  let toolbar=document.getElementById('flosiGoalToolbar');
  if(!toolbar){toolbar=document.createElement('div');toolbar.id='flosiGoalToolbar';toolbar.className='flosiGoalToolbar';toolbar.setAttribute('data-locale-no-transform','');toolbar.innerHTML='<b>أهدافك</b><button class="flosiGoalAdd" id="flosiGoalAdd">+ هدف جديد</button>';const head=plan.querySelector('.head');head?.insertAdjacentElement('afterend',toolbar);document.getElementById('flosiGoalAdd').onclick=()=>openEditor()}
  let list=document.getElementById('flosiGoalList');
  if(!list){list=document.createElement('div');list.id='flosiGoalList';list.setAttribute('data-locale-no-transform','');[...plan.querySelectorAll(':scope > .panel')].forEach(x=>x.remove());toolbar.insertAdjacentElement('afterend',list)}
  const goals=read();list.replaceChildren();
  if(!goals.length){const e=document.createElement('div');e.className='flosiGoalEmpty';e.textContent='لا توجد أهداف. اضغط «هدف جديد» لإضافة أول هدف.';list.appendChild(e);return}
  goals.forEach(g=>{
    const p=progress(g),card=document.createElement('div');card.className='flosiGoalCard';card.setAttribute('role','button');card.tabIndex=0;card.dataset.goalId=g.id;
    const target=Number(g.target)||0,saved=Math.min(Math.max(0,Number(g.saved)||0),target||Number(g.saved)||0),sub=target>0?`${money(saved,g.currency)} / ${money(target,g.currency)}`:`المبلغ المستهدف غير محدد • ${g.currency}`;
    card.innerHTML=`<div class="flosiGoalRing" style="background:conic-gradient(var(--p) 0 ${p}%,#eeeaf4 ${p}% 100%)"><b>${p}%</b></div><div class="flosiGoalCopy"><b>${esc(g.name)}</b><small>${esc(sub)}${g.deadline?` • ${esc(g.deadline)}`:''}</small></div><button class="flosiGoalEdit" aria-label="تعديل الهدف">✎</button>`;
    const open=()=>openEditor(g.id);card.onclick=open;card.onkeydown=e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();open()}};card.querySelector('.flosiGoalEdit').onclick=e=>{e.stopPropagation();open()};list.appendChild(card)
  });
  if(typeof window.FLOSI_LATINIZE_DIGITS==='function')window.FLOSI_LATINIZE_DIGITS();
}

document.addEventListener('click',e=>{if(e.target.closest('#flosiGoalSave'))saveEditor();if(e.target.closest('#flosiGoalDelete'))deleteEditor()});
document.addEventListener('change',e=>{if(e.target&&e.target.id==='settingsCurrency')setTimeout(render,80)},true);
window.addEventListener('storage',e=>{if(e.key===STORAGE_KEY||e.key==='flosi-currency')render()});
render();setTimeout(render,220);
})();
