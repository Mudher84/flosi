(()=>{
'use strict';
if(window.__FLOSI_PREVIEW_STATE__) return;
window.__FLOSI_PREVIEW_STATE__=true;

const STORAGE_KEY='flosi-preview-ledger-v1';
const SOURCE_KEY='flosi-preview-source-currency-v1';
const RESET_KEY='flosi-preview-zero-reset-20260823-v1';
const BASE=Object.freeze({balance:0,monthIncome:0,monthExpense:0,safe:0,forecast:0});
const DEMO=[];

function selectedCurrency(){return (localStorage.getItem('flosi-currency')||'IQD').toUpperCase()}
function resetFinancialDataOnce(){
  if(localStorage.getItem(RESET_KEY)==='1')return;
  localStorage.removeItem(STORAGE_KEY);
  localStorage.setItem(SOURCE_KEY,selectedCurrency());
  localStorage.setItem(RESET_KEY,'1');
}
resetFinancialDataOnce();

function sourceCurrency(){
  let source=(localStorage.getItem(SOURCE_KEY)||'').toUpperCase();
  if(!source){source=selectedCurrency();localStorage.setItem(SOURCE_KEY,source)}
  return source;
}
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
function rateKey(from,to){return `flosi-fx-${String(from).toUpperCase()}-${String(to).toUpperCase()}`}
function fxRate(currency){
  const source=sourceCurrency(),target=String(currency||'').toUpperCase();
  if(target===source)return 1;
  const direct=Number(localStorage.getItem(rateKey(source,target))||'');
  if(Number.isFinite(direct)&&direct>0)return direct;
  const reverse=Number(localStorage.getItem(rateKey(target,source))||'');
  return Number.isFinite(reverse)&&reverse>0?1/reverse:null;
}
function toSource(value,currency){
  const n=Number(value);if(!Number.isFinite(n))return null;
  const rate=fxRate(currency);return rate===null?null:n/rate;
}
function fromSource(value,currency){
  const rate=fxRate(currency);return rate===null?null:Number(value)*rate;
}
function fractionDigits(currency){return ['IQD','JPY','KRW'].includes(currency)?0:2}
function money(value,currency,locale){
  try{return new Intl.NumberFormat(locale,{style:'currency',currency,maximumFractionDigits:fractionDigits(currency)}).format(Math.abs(value))}
  catch(_){return `${Math.abs(value).toLocaleString('en-US',{maximumFractionDigits:fractionDigits(currency)})} ${currency}`}
}
function fmtSource(value){
  const {currency,locale}=localeState(),source=sourceCurrency(),numeric=Number(value)||0;
  if(numeric===0)return money(0,currency,locale);
  const converted=fromSource(numeric,currency);
  return converted===null?money(numeric,source,locale):money(converted,currency,locale);
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
  const now=new Date();return Math.max(1,new Date(now.getFullYear(),now.getMonth()+1,0).getDate()-now.getDate()+1);
}
function mark(el){if(el)el.setAttribute('data-locale-no-transform','');return el}
function setText(el,text){if(mark(el))el.textContent=text}
function ensureMetricLive(card,key){
  if(!card)return null;const original=card.querySelector('strong');if(original)original.style.display='none';
  let live=card.querySelector(`[data-preview-metric="${key}"]`);
  if(!live){live=document.createElement('div');live.dataset.previewMetric=key;live.style.cssText='display:block;font-size:19px;font-weight:700;margin-top:2px';mark(live);if(original)original.insertAdjacentElement('afterend',live);else card.appendChild(live)}
  return live;
}
function installLiveRegions(){
  const home=document.getElementById('home');if(!home)return {};
  const heroLabels=home.querySelectorAll('.hero .heroLabel'),glassValues=home.querySelectorAll('.heroRow .glass b'),metricCards=home.querySelectorAll('.metric');
  return {
    heroBalance:mark(home.querySelector('.hero .balance')),heroNet:mark(heroLabels[1]),safe:mark(glassValues[0]),forecastHero:mark(glassValues[1]),forecast:mark(home.querySelector('.forecastValue')),brief:mark(home.querySelector('.brief p')),
    monthIncome:ensureMetricLive(metricCards[0],'income'),monthExpense:ensureMetricLive(metricCards[1],'expense'),commitments:ensureMetricLive(metricCards[2],'commitments'),savingsRate:ensureMetricLive(metricCards[3],'savings')
  };
}
function emptyRow(text){const row=document.createElement('div');row.style.cssText='padding:14px 2px;color:var(--muted);font-size:10px';row.textContent=text;mark(row);return row}
function rowFor(tx){
  const row=document.createElement('div');row.className='tx';mark(row);
  const icon=document.createElement('div');icon.className='txIcon';icon.textContent=tx.icon||(tx.kind==='income'?'💼':'●');
  const text=document.createElement('div');text.className='txText';const b=document.createElement('b');b.textContent=tx.title||'حركة';const small=document.createElement('small');small.textContent=tx.subtitle||'الآن';text.append(b,small);
  const amount=document.createElement('div');amount.className='flosiLiveTxAmount';amount.style.cssText=`font-size:11px;font-weight:700;color:${tx.kind==='income'?'var(--green)':'var(--red)'}`;amount.textContent=(tx.kind==='income'?'+':'−')+fmtSource(Number(tx.amountUsd));
  row.append(icon,text,amount);return row;
}
function renderTransactions(){
  const custom=entries().slice().sort((a,b)=>(b.createdAt||0)-(a.createdAt||0)),all=[...custom,...DEMO];
  const recent=document.getElementById('recent');if(recent){recent.replaceChildren(...(all.length?all.slice(0,5).map(rowFor):[emptyRow('لا توجد حركات بعد')]));mark(recent)}
  const activityPanel=document.querySelector('#activity .panel');if(activityPanel){activityPanel.replaceChildren(...(all.length?all.map(rowFor):[emptyRow('لا توجد حركات بعد')]));mark(activityPanel)}
}
function zeroStaticDemo(){
  const {currency}=localeState();
  document.querySelectorAll('.obAmount').forEach(el=>setText(el,money(0,currency,localeState().locale)));
  document.querySelectorAll('.goalRing').forEach(el=>{const b=el.querySelector('b');if(b)setText(b,'0%');el.style.background='conic-gradient(var(--p) 0 0%,#eeeaf4 0)'});
  document.querySelectorAll('.goalText small').forEach(el=>setText(el,`0 / 0 ${currency}`));
  document.querySelectorAll('.intel ul').forEach(list=>{
    list.replaceChildren();
    ['ابدأ بإضافة أول دخل أو مصروف حتى يبني Flosi تحليلك.','أضف هدفاً أو ميزانية عندما تكون جاهزاً.','التحليلات والتوقعات تبدأ من بياناتك أنت فقط.'].forEach(text=>{const li=document.createElement('li');li.textContent=text;list.appendChild(li)});
    mark(list);
  });
  const what=document.getElementById('whatAmount');if(what&&what.dataset.zeroed!=='1'){what.value='0';what.dataset.zeroed='1'}
}
function renderMetrics(){
  const r=installLiveRegions(),m=metrics();
  setText(r.heroBalance,fmtSource(m.balance));setText(r.heroNet,(m.netMonth>=0?'+':'−')+fmtSource(Math.abs(m.netMonth))+' هذا الشهر');setText(r.safe,fmtSource(m.safe));setText(r.forecastHero,fmtSource(m.forecast));setText(r.forecast,fmtSource(m.forecast));setText(r.monthIncome,fmtSource(m.monthIncome));setText(r.monthExpense,fmtSource(m.monthExpense));setText(r.commitments,fmtSource(0));setText(r.savingsRate,`${Math.round(m.savingsRate)}%`);
  setText(r.brief,m.monthIncome===0&&m.monthExpense===0?'التطبيق مصفّر وجاهز. أضف أول حركة حتى يبدأ Flosi بحساب ملخصك المالي.':`المتاح للصرف بأمان الآن ${fmtSource(m.safe)}. معدل الصرف اليومي المقترح ${fmtSource(m.safe/remainingDays())} حتى نهاية الشهر.`);
  zeroStaticDemo();
}
function syncModalCurrency(){
  const input=document.getElementById('txAmount');if(!input)return;const {currency}=localeState();const label=input.closest('.field')?.querySelector('label');if(label)setText(label,`المبلغ (${currency})`);input.step=fractionDigits(currency)===0?'1':'0.01';if(input.dataset.zeroed!=='1'){input.value='';input.dataset.zeroed='1'}
  const name=document.getElementById('txName');if(name&&name.dataset.zeroed!=='1'){name.value='';name.dataset.zeroed='1'}
}
function renderAll(){renderMetrics();renderTransactions();syncModalCurrency();if(typeof window.FLOSI_LATINIZE_DIGITS==='function')window.FLOSI_LATINIZE_DIGITS()}
function userToast(msg){if(typeof window.toast==='function'){window.toast(msg);return}const t=document.getElementById('toast');if(!t)return;t.textContent=msg;t.classList.add('show');setTimeout(()=>t.classList.remove('show'),1800)}
function saveTransaction(){
  const title=(document.getElementById('txName')?.value||'').trim(),raw=Number(document.getElementById('txAmount')?.value||0),kind=document.getElementById('txType')?.value==='income'?'income':'expense', {currency}=localeState();
  if(!title){userToast('اكتب بيان الحركة');return}if(!Number.isFinite(raw)||raw<=0){userToast('أدخل مبلغاً صحيحاً أكبر من صفر');return}
  const amountSource=toSource(raw,currency);if(amountSource===null){userToast(`أضف سعر تحويل ${sourceCurrency()} → ${currency} من إعدادات العملة أولاً`);return}
  const state=readState();state.entries.unshift({id:`tx-${Date.now()}`,kind,title,amountUsd:Number(amountSource.toFixed(6)),createdAt:Date.now(),subtitle:'الآن',icon:kind==='income'?'💼':'●'});writeState(state);renderAll();document.getElementById('addModal')?.classList.remove('open');const name=document.getElementById('txName'),amount=document.getElementById('txAmount');if(name)name.value='';if(amount)amount.value='';userToast('تمت إضافة الحركة وتحديث الحسابات')
}
function simulate(){
  const input=Number(document.getElementById('whatAmount')?.value||0),box=document.getElementById('simResult');if(!box)return;const {currency}=localeState();if(!Number.isFinite(input)||input<0){userToast('أدخل مبلغاً صحيحاً للمحاكاة');return}
  const purchase=toSource(input,currency);if(purchase===null){userToast(`أضف سعر تحويل ${sourceCurrency()} → ${currency} أولاً`);return}
  const after=metrics().safe-purchase,daily=Math.max(0,after)/remainingDays();mark(box);box.style.display='block';box.replaceChildren();const title=document.createElement('b');title.textContent=after>=0?'بعد الشراء':'الشراء يتجاوز المتاح الآمن';const p=document.createElement('p');p.style.cssText='font-size:11px;color:var(--muted);line-height:1.8';p.textContent=after>=0?`يبقى ${fmtSource(after)} متاحاً للصرف بأمان، ومعدل الصرف اليومي المقترح ${fmtSource(daily)} حتى نهاية الشهر.`:`المبلغ يتجاوز المتاح للصرف بأمان بمقدار ${fmtSource(Math.abs(after))}.`;box.append(title,p)
}
function bind(){
  const save=document.getElementById('saveTx');if(save)save.onclick=saveTransaction;const sim=document.getElementById('simulate');if(sim)sim.onclick=simulate;
  document.addEventListener('change',e=>{if(e.target&&['settingsLang','settingsCurrency'].includes(e.target.id))setTimeout(renderAll,80)},true);
  document.addEventListener('click',e=>{if(e.target.closest('#settingsSaveLocale,#previewFxSave'))setTimeout(renderAll,160)},true);
  window.addEventListener('storage',e=>{if(!e.key||e.key.startsWith('flosi-'))renderAll()});
}

bind();renderAll();setTimeout(renderAll,180);
})();
