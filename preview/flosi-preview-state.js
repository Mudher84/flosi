(()=>{
'use strict';
if(window.__FLOSI_PREVIEW_STATE__) return;
window.__FLOSI_PREVIEW_STATE__=true;

const STORAGE_KEY='flosi-preview-ledger-v1';
const SOURCE_CURRENCY='USD';
const BASE=Object.freeze({balance:12840.50,monthIncome:4850,monthExpense:3430,safe:2360,forecast:14120});
const DEMO=[
  {id:'demo-shopping',kind:'expense',title:'التسوق',subtitle:'Visa • اليوم 10:20',amountUsd:86.20,icon:'🛍'},
  {id:'demo-salary',kind:'income',title:'راتب',subtitle:'الحساب الرئيسي • اليوم 08:00',amountUsd:2500,icon:'💼'},
  {id:'demo-coffee',kind:'expense',title:'قهوة',subtitle:'Cash • أمس',amountUsd:4.50,icon:'☕'}
];

function readState(){
  try{
    const parsed=JSON.parse(localStorage.getItem(STORAGE_KEY)||'null');
    if(parsed&&parsed.version===1&&Array.isArray(parsed.entries)) return parsed;
  }catch(_){ }
  return {version:1,entries:[]};
}
function writeState(state){localStorage.setItem(STORAGE_KEY,JSON.stringify(state))}
function entries(){return readState().entries.filter(x=>x&&['income','expense'].includes(x.kind)&&Number.isFinite(Number(x.amountUsd))&&Number(x.amountUsd)>0)}
function localeState(){
  const lang=localStorage.getItem('flosi-lang-preview')||localStorage.getItem('flosi-lang')||'ar';
  const currency=(localStorage.getItem('flosi-currency-preview')||localStorage.getItem('flosi-currency')||'IQD').toUpperCase();
  const locale=localStorage.getItem('flosi-locale')||(lang==='ar'?'ar-IQ':'en-US');
  return {lang,currency,locale};
}
function fxRate(currency){
  if(currency===SOURCE_CURRENCY) return 1;
  const n=Number(localStorage.getItem(`flosi-fx-${SOURCE_CURRENCY}-${currency}`)||'');
  return Number.isFinite(n)&&n>0?n:null;
}
function toUsd(value,currency){
  const n=Number(value);
  if(!Number.isFinite(n)) return null;
  const rate=fxRate(currency);
  return rate===null?null:n/rate;
}
function fromUsd(value,currency){
  const rate=fxRate(currency);
  return rate===null?null:Number(value)*rate;
}
function fractionDigits(currency){return ['IQD','JPY','KRW'].includes(currency)?0:2}
function fmtUsd(value){
  const {currency,locale}=localeState();
  const converted=fromUsd(value,currency);
  const actual=converted===null?Number(value):converted;
  const actualCurrency=converted===null?SOURCE_CURRENCY:currency;
  try{
    return new Intl.NumberFormat(locale,{style:'currency',currency:actualCurrency,maximumFractionDigits:fractionDigits(actualCurrency)}).format(Math.abs(actual));
  }catch(_){
    return `${Math.abs(actual).toLocaleString('en-US',{maximumFractionDigits:fractionDigits(actualCurrency)})} ${actualCurrency}`;
  }
}
function metrics(){
  const list=entries();
  const incomeDelta=list.filter(x=>x.kind==='income').reduce((s,x)=>s+Number(x.amountUsd),0);
  const expenseDelta=list.filter(x=>x.kind==='expense').reduce((s,x)=>s+Number(x.amountUsd),0);
  const balanceDelta=incomeDelta-expenseDelta;
  const monthIncome=BASE.monthIncome+incomeDelta;
  const monthExpense=BASE.monthExpense+expenseDelta;
  const netMonth=monthIncome-monthExpense;
  const balance=BASE.balance+balanceDelta;
  const safe=Math.max(0,BASE.safe+balanceDelta);
  const forecast=Math.max(0,BASE.forecast+balanceDelta);
  const savingsRate=monthIncome>0?Math.max(0,Math.min(100,(netMonth/monthIncome)*100)):0;
  return {monthIncome,monthExpense,netMonth,balance,safe,forecast,savingsRate};
}
function remainingDays(){
  const now=new Date();
  return Math.max(1,new Date(now.getFullYear(),now.getMonth()+1,0).getDate()-now.getDate()+1);
}
function mark(el){if(el)el.setAttribute('data-locale-no-transform','');return el}
function setText(el,text){if(mark(el))el.textContent=text}
function ensureMetricLive(card,key){
  if(!card) return null;
  const original=card.querySelector('strong');
  if(original) original.style.display='none';
  let live=card.querySelector(`[data-preview-metric="${key}"]`);
  if(!live){
    live=document.createElement('div');
    live.dataset.previewMetric=key;
    live.style.cssText='display:block;font-size:19px;font-weight:700;margin-top:2px';
    mark(live);
    if(original) original.insertAdjacentElement('afterend',live); else card.appendChild(live);
  }
  return live;
}
function installLiveRegions(){
  const home=document.getElementById('home');
  if(!home) return {};
  const heroLabels=home.querySelectorAll('.hero .heroLabel');
  const glassValues=home.querySelectorAll('.heroRow .glass b');
  const metricCards=home.querySelectorAll('.metric');
  return {
    heroBalance:mark(home.querySelector('.hero .balance')),
    heroNet:mark(heroLabels[1]),
    safe:mark(glassValues[0]),
    forecastHero:mark(glassValues[1]),
    forecast:mark(home.querySelector('.forecastValue')),
    brief:mark(home.querySelector('.brief p')),
    monthIncome:ensureMetricLive(metricCards[0],'income'),
    monthExpense:ensureMetricLive(metricCards[1],'expense'),
    savingsRate:ensureMetricLive(metricCards[3],'savings')
  };
}
function rowFor(tx){
  const row=document.createElement('div');row.className='tx';mark(row);
  const icon=document.createElement('div');icon.className='txIcon';icon.textContent=tx.icon||(tx.kind==='income'?'💼':'●');
  const text=document.createElement('div');text.className='txText';
  const b=document.createElement('b');b.textContent=tx.title||'حركة';
  const small=document.createElement('small');small.textContent=tx.subtitle||'الآن';
  text.append(b,small);
  const amount=document.createElement('div');
  amount.className='flosiLiveTxAmount';
  amount.style.cssText=`font-size:11px;font-weight:700;color:${tx.kind==='income'?'var(--green)':'var(--red)'}`;
  amount.textContent=(tx.kind==='income'?'+':'−')+fmtUsd(Number(tx.amountUsd));
  row.append(icon,text,amount);
  return row;
}
function renderTransactions(){
  const custom=entries().slice().sort((a,b)=>(b.createdAt||0)-(a.createdAt||0));
  const all=[...custom,...DEMO];
  const recent=document.getElementById('recent');
  if(recent){recent.replaceChildren(...all.slice(0,5).map(rowFor));mark(recent)}
  const activityPanel=document.querySelector('#activity .panel');
  if(activityPanel){activityPanel.replaceChildren(...all.map(rowFor));mark(activityPanel)}
}
function renderMetrics(){
  const r=installLiveRegions();
  const m=metrics();
  setText(r.heroBalance,fmtUsd(m.balance));
  setText(r.heroNet,(m.netMonth>=0?'+':'−')+fmtUsd(Math.abs(m.netMonth))+' هذا الشهر');
  setText(r.safe,fmtUsd(m.safe));
  setText(r.forecastHero,fmtUsd(m.forecast));
  setText(r.forecast,fmtUsd(m.forecast));
  setText(r.monthIncome,fmtUsd(m.monthIncome));
  setText(r.monthExpense,fmtUsd(m.monthExpense));
  setText(r.savingsRate,`${Math.round(m.savingsRate)}%`);
  setText(r.brief,`المتاح للصرف بأمان الآن ${fmtUsd(m.safe)}. معدل الصرف اليومي المقترح ${fmtUsd(m.safe/remainingDays())} حتى نهاية الشهر، مع إبقاء الالتزامات والأهداف بدون تأثير.`);
}
function syncModalCurrency(){
  const input=document.getElementById('txAmount');
  if(!input) return;
  const {currency}=localeState();
  const label=input.closest('.field')?.querySelector('label');
  if(label)setText(label,`المبلغ (${currency})`);
  input.step=fractionDigits(currency)===0?'1':'0.01';
}
function renderAll(){
  renderMetrics();renderTransactions();syncModalCurrency();
  if(typeof window.FLOSI_LATINIZE_DIGITS==='function')window.FLOSI_LATINIZE_DIGITS();
}
function userToast(msg){
  if(typeof window.toast==='function'){window.toast(msg);return}
  const t=document.getElementById('toast');if(!t)return;
  t.textContent=msg;t.classList.add('show');setTimeout(()=>t.classList.remove('show'),1800);
}
function saveTransaction(){
  const title=(document.getElementById('txName')?.value||'').trim();
  const raw=Number(document.getElementById('txAmount')?.value||0);
  const kind=document.getElementById('txType')?.value==='income'?'income':'expense';
  const {currency}=localeState();
  if(!title){userToast('اكتب بيان الحركة');return}
  if(!Number.isFinite(raw)||raw<=0){userToast('أدخل مبلغاً صحيحاً أكبر من صفر');return}
  const amountUsd=toUsd(raw,currency);
  if(amountUsd===null){userToast(`أضف سعر تحويل USD → ${currency} من إعدادات العملة أولاً`);return}
  const state=readState();
  state.entries.unshift({id:`tx-${Date.now()}`,kind,title,amountUsd:Number(amountUsd.toFixed(6)),createdAt:Date.now(),subtitle:'الآن',icon:kind==='income'?'💼':'●'});
  writeState(state);renderAll();
  document.getElementById('addModal')?.classList.remove('open');
  const name=document.getElementById('txName'),amount=document.getElementById('txAmount');
  if(name)name.value='';if(amount)amount.value='';
  userToast('تمت إضافة الحركة وتحديث الحسابات');
}
function simulate(){
  const input=Number(document.getElementById('whatAmount')?.value||0);
  const box=document.getElementById('simResult');if(!box)return;
  const {currency}=localeState();
  if(!Number.isFinite(input)||input<0){userToast('أدخل مبلغاً صحيحاً للمحاكاة');return}
  const purchaseUsd=toUsd(input,currency);
  if(purchaseUsd===null){userToast(`أضف سعر تحويل USD → ${currency} أولاً`);return}
  const after=metrics().safe-purchaseUsd;
  const daily=Math.max(0,after)/remainingDays();
  mark(box);box.style.display='block';box.replaceChildren();
  const title=document.createElement('b');title.textContent=after>=0?'بعد الشراء':'الشراء يتجاوز المتاح الآمن';
  const p=document.createElement('p');p.style.cssText='font-size:11px;color:var(--muted);line-height:1.8';
  p.textContent=after>=0?`يبقى ${fmtUsd(after)} متاحاً للصرف بأمان، ومعدل الصرف اليومي المقترح ${fmtUsd(daily)} حتى نهاية الشهر.`:`المبلغ يتجاوز المتاح للصرف بأمان بمقدار ${fmtUsd(Math.abs(after))}.`;
  box.append(title,p);
}
function bind(){
  const save=document.getElementById('saveTx');if(save)save.onclick=saveTransaction;
  const sim=document.getElementById('simulate');if(sim)sim.onclick=simulate;
  document.addEventListener('change',e=>{if(e.target&&['settingsLang','settingsCurrency'].includes(e.target.id))setTimeout(renderAll,80)},true);
  document.addEventListener('click',e=>{if(e.target.closest('#settingsSaveLocale,#previewFxSave'))setTimeout(renderAll,160)},true);
  window.addEventListener('storage',e=>{if(!e.key||e.key.startsWith('flosi-'))renderAll()});
}

bind();renderAll();setTimeout(renderAll,180);
})();
