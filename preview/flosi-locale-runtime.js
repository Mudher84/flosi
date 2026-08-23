(()=>{
'use strict';
if(window.__FLOSI_LOCALE_RUNTIME__) return;
window.__FLOSI_LOCALE_RUNTIME__=true;

const I18N=window.FLOSI_I18N||{};
const LEGACY_SOURCE_CURRENCY='USD';
const PREVIEW_SOURCE_KEY='flosi-preview-source-currency-v1';
const localeMeta=window.localeMeta||{
  ar:{label:'العربية',locale:'ar-IQ',dir:'rtl'},en:{label:'English',locale:'en-US',dir:'ltr'},
  'zh-CN':{label:'简体中文',locale:'zh-CN',dir:'ltr'},es:{label:'Español',locale:'es-ES',dir:'ltr'},
  fr:{label:'Français',locale:'fr-FR',dir:'ltr'},de:{label:'Deutsch',locale:'de-DE',dir:'ltr'},
  tr:{label:'Türkçe',locale:'tr-TR',dir:'ltr'},fa:{label:'فارسی',locale:'fa-IR',dir:'rtl'},
  ur:{label:'اردو',locale:'ur-PK',dir:'rtl'},hi:{label:'हिन्दी',locale:'hi-IN',dir:'ltr'},
  pt:{label:'Português',locale:'pt-BR',dir:'ltr'},it:{label:'Italiano',locale:'it-IT',dir:'ltr'},
  ru:{label:'Русский',locale:'ru-RU',dir:'ltr'},ja:{label:'日本語',locale:'ja-JP',dir:'ltr'},
  ko:{label:'한국어',locale:'ko-KR',dir:'ltr'},id:{label:'Bahasa Indonesia',locale:'id-ID',dir:'ltr'},
  ms:{label:'Bahasa Melayu',locale:'ms-MY',dir:'ltr'},bn:{label:'বাংলা',locale:'bn-BD',dir:'ltr'},
  nl:{label:'Nederlands',locale:'nl-NL',dir:'ltr'},pl:{label:'Polski',locale:'pl-PL',dir:'ltr'},
  sv:{label:'Svenska',locale:'sv-SE',dir:'ltr'},th:{label:'ไทย',locale:'th-TH',dir:'ltr'},
  vi:{label:'Tiếng Việt',locale:'vi-VN',dir:'ltr'},he:{label:'עברית',locale:'he-IL',dir:'rtl'}
};

const originalText=new WeakMap();
const originalBare=new WeakMap();
let applying=false;
let observer=null;

const manual={ar:{
  'Daily Money Brief':'الملخص المالي اليومي','Flosi Intelligence':'ذكاء Flosi المالي',
  'Smart Money OS':'نظام المال الذكي','Main':'الحساب الرئيسي','Cash':'نقداً',
  'Settings & privacy':'الإعدادات والخصوصية','Global settings':'إعدادات عالمية',
  'Base currency':'العملة الأساسية','App language':'لغة التطبيق','Recent transactions':'آخر الحركات',
  'Transactions':'الحركات','Reports':'التقارير','Security':'الأمان'
}};

function committedState(){
  const lang=localStorage.getItem('flosi-lang')||'ar';
  const currency=(localStorage.getItem('flosi-currency')||'IQD').toUpperCase();
  const meta=localeMeta[lang]||localeMeta.ar;
  return {lang,currency,meta};
}
function state(){
  const committed=committedState();
  const lang=localStorage.getItem('flosi-lang-preview')||committed.lang;
  const currency=(localStorage.getItem('flosi-currency-preview')||committed.currency).toUpperCase();
  return {lang,currency,meta:localeMeta[lang]||localeMeta.ar};
}
function sourceCurrency(){return (localStorage.getItem(PREVIEW_SOURCE_KEY)||committedState().currency||'IQD').toUpperCase()}
function safeNumber(raw){
  const n=Number(String(raw).replace(/,/g,'').replace(/\s/g,''));
  return Number.isFinite(n)?n:null;
}
function fractionDigits(currency){return ['IQD','JPY','KRW'].includes(currency)?0:2}
function money(n,currency,locale){
  try{return new Intl.NumberFormat(locale,{style:'currency',currency,maximumFractionDigits:fractionDigits(currency)}).format(n)}
  catch(_){return `${Number(n).toLocaleString(locale)} ${currency}`}
}
function rateKey(from,to){return `flosi-fx-${String(from).toUpperCase()}-${String(to).toUpperCase()}`}
function getRate(currency){
  const source=sourceCurrency(),target=String(currency||'').toUpperCase();
  if(target===source)return 1;
  const direct=Number(localStorage.getItem(rateKey(source,target))||'');
  if(Number.isFinite(direct)&&direct>0)return direct;
  const reverse=Number(localStorage.getItem(rateKey(target,source))||'');
  return Number.isFinite(reverse)&&reverse>0?1/reverse:null;
}
function convertValue(value,currency){
  const rate=getRate(currency);
  return rate===null?null:value*rate;
}
function formatConverted(value,currency,locale){
  const converted=convertValue(value,currency);
  return converted===null?null:money(converted,currency,locale);
}
function currencyText(text,currency,locale){
  const source=sourceCurrency();
  if(currency===source)return text;
  if(source!==LEGACY_SOURCE_CURRENCY||getRate(currency)===null)return text;
  let out=text;
  out=out.replace(/([+−-]?\d[\d,]*(?:\.\d+)?)\s*\/\s*([+−-]?\d[\d,]*(?:\.\d+)?)\s+USD\b/g,(m,a,b)=>{
    const na=safeNumber(a.replace(/[+−-]/g,'')),nb=safeNumber(b.replace(/[+−-]/g,''));
    if(na===null||nb===null)return m;
    return `${formatConverted(na,currency,locale)} / ${formatConverted(nb,currency,locale)}`;
  });
  out=out.replace(/\bUSD\s*([+−-]?\d[\d,]*(?:\.\d+)?)/g,(m,a)=>{
    const sign=/^[+]/.test(a)?'+':/^[−-]/.test(a)?'−':'';
    const n=safeNumber(a.replace(/[+−-]/g,''));
    const v=n===null?null:formatConverted(n,currency,locale);
    return v===null?m:sign+v;
  });
  out=out.replace(/([+−-]?\d[\d,]*(?:\.\d+)?)\s+USD\b/g,(m,a)=>{
    const sign=/^[+]/.test(a)?'+':/^[−-]/.test(a)?'−':'';
    const n=safeNumber(a.replace(/[+−-]/g,''));
    const v=n===null?null:formatConverted(n,currency,locale);
    return v===null?m:sign+v;
  });
  return out;
}
function buildReverse(){
  const reverse=new Map();
  ['ar','en'].forEach(code=>{const t=I18N[code]&&I18N[code].t;if(!t)return;Object.entries(t).forEach(([k,v])=>{if(typeof v==='string')reverse.set(v,k)})});
  return reverse;
}
function translateExact(text,lang,reverse){
  const trimmed=text.trim();if(!trimmed)return text;
  let replacement=null;const key=reverse.get(trimmed);
  if(key&&I18N[lang]&&I18N[lang].t&&I18N[lang].t[key])replacement=I18N[lang].t[key];
  if(!replacement&&manual[lang]&&manual[lang][trimmed])replacement=manual[lang][trimmed];
  if(!replacement)return text;const start=text.indexOf(trimmed);
  return text.slice(0,start)+replacement+text.slice(start+trimmed.length);
}
function shouldSkip(node){const p=node.parentElement;return !p||!!p.closest('script,style,option,select,textarea,input,[data-locale-no-transform]')}
function applyText(root=document.body){
  if(!root)return;const {lang,currency,meta}=state();const reverse=buildReverse();
  const walker=document.createTreeWalker(root,NodeFilter.SHOW_TEXT);const nodes=[];while(walker.nextNode())nodes.push(walker.currentNode);
  nodes.forEach(node=>{if(shouldSkip(node))return;if(!originalText.has(node))originalText.set(node,node.nodeValue||'');const base=originalText.get(node)||'';let next=translateExact(base,lang,reverse);next=currencyText(next,currency,meta.locale);if(node.nodeValue!==next)node.nodeValue=next});
}
function signedBare(raw,currency,locale){
  const sign=raw.trim().startsWith('+')?'+':raw.trim().startsWith('−')||raw.trim().startsWith('-')?'−':'';
  const n=safeNumber(raw.replace(/[+−-]/g,''));if(n===null)return raw;
  if(n===0)return sign+money(0,currency,locale);
  const out=formatConverted(n,currency,locale);return out===null?raw:sign+out;
}
function applyBareValues(){
  const {currency,meta}=state();
  const selectors=['.metric strong','.tx .neg','.tx .pos'];
  document.querySelectorAll(selectors.join(',')).forEach(el=>{
    if(!originalBare.has(el))originalBare.set(el,el.textContent||'');
    const base=originalBare.get(el)||'';
    if(base.includes('%')){el.textContent=base;return}
    el.textContent=signedBare(base,currency,meta.locale);
  });
}
function ensureCenteredSelectors(){
  if(document.getElementById('flosiLocaleCentering'))return;
  const style=document.createElement('style');style.id='flosiLocaleCentering';
  style.textContent='#settingsLang,#settingsCurrency{text-align:center!important;text-align-last:center!important;padding-inline:52px!important}#settingsLang option,#settingsCurrency option{text-align:center!important}';
  document.head.appendChild(style);
}
function ensureFxPanel(){
  const locale=document.getElementById('locale'),preview=document.querySelector('#locale .localePreview');if(!locale||!preview)return;
  let panel=document.getElementById('previewFxPanel');
  if(!panel){
    panel=document.createElement('div');panel.id='previewFxPanel';panel.setAttribute('data-locale-no-transform','');
    panel.style.cssText='background:#fff;border:1px solid var(--line);border-radius:20px;padding:14px;box-shadow:var(--shadow);margin-top:12px';
    panel.innerHTML='<div style="display:flex;gap:10px;align-items:flex-start"><div style="width:40px;height:40px;border-radius:13px;background:#fff6e7;color:var(--amber);display:grid;place-items:center;font-size:18px">⇄</div><div style="flex:1"><b style="display:block;font-size:11px">سعر تحويل نسخة العرض</b><small id="previewFxHelp" style="display:block;color:var(--muted);font-size:8px;line-height:1.7;margin-top:2px"></small></div></div><div id="previewFxInputs" style="display:grid;grid-template-columns:1fr auto;gap:8px;margin-top:11px"><input id="previewFxRate" inputmode="decimal" style="height:46px;border:1px solid #e4d9f7;border-radius:14px;padding:0 12px;outline:0"><button id="previewFxSave" style="border:0;border-radius:14px;background:var(--p);color:#fff;padding:0 15px;font-weight:700">حفظ السعر</button></div><small id="previewFxStatus" style="display:block;margin-top:8px;font-size:8px;color:var(--muted)"></small>';
    preview.parentNode.insertBefore(panel,preview);
    panel.querySelector('#previewFxSave').addEventListener('click',()=>{
      const {currency}=state(),source=sourceCurrency(),input=panel.querySelector('#previewFxRate');
      const rate=Number(String(input.value||'').replace(',','.'));
      if(currency===source)return;
      if(!Number.isFinite(rate)||rate<=0){panel.querySelector('#previewFxStatus').textContent='أدخل سعر صرف أكبر من صفر.';panel.querySelector('#previewFxStatus').style.color='var(--red)';return}
      localStorage.setItem(rateKey(source,currency),String(rate));panel.querySelector('#previewFxStatus').textContent=`تم حفظ: 1 ${source} = ${rate} ${currency}`;panel.querySelector('#previewFxStatus').style.color='var(--green)';applyDocument();
    });
  }
  const {currency}=state(),source=sourceCurrency(),rate=getRate(currency),inputs=panel.querySelector('#previewFxInputs');
  panel.style.display=currency===source?'none':'block';if(currency===source)return;
  panel.querySelector('#previewFxHelp').textContent=`بياناتك الحالية محفوظة بعملة ${source}. التحويل إلى ${currency} يحتاج سعر صرف.`;
  panel.querySelector('#previewFxRate').value=rate===null?'':String(rate);
  panel.querySelector('#previewFxRate').placeholder=`1 ${source} = ? ${currency}`;
  inputs.style.display='grid';
  panel.querySelector('#previewFxStatus').textContent=rate===null?`لا يوجد سعر محفوظ بين ${source} و ${currency}.`:`نشط: 1 ${source} = ${rate} ${currency}`;
  panel.querySelector('#previewFxStatus').style.color=rate===null?'var(--amber)':'var(--green)';
}
function syncControls(){
  const {lang,currency}=state();const ls=document.getElementById('settingsLang'),cs=document.getElementById('settingsCurrency');
  if(ls&&[...ls.options].some(o=>o.value===lang))ls.value=lang;if(cs&&[...cs.options].some(o=>o.value===currency))cs.value=currency;
}
function fixLocalePreview(){
  const {currency,meta}=state(),amount=document.getElementById('localeAmountPreview'),compact=document.getElementById('localeCompactPreview');
  if(!amount||!compact)return;
  amount.textContent=money(0,currency,meta.locale);
  compact.textContent=money(0,currency,meta.locale);
}
function applyDocument(){
  if(applying)return;applying=true;
  try{
    const {lang,meta}=state();document.documentElement.lang=lang;document.documentElement.dir=meta.dir;document.body&&document.body.setAttribute('dir',meta.dir);
    ensureCenteredSelectors();syncControls();if(typeof window.renderLocale==='function'){try{window.renderLocale()}catch(_){}}
    applyText(document.body);applyBareValues();ensureFxPanel();fixLocalePreview();
    if(typeof window.FLOSI_LATINIZE_DIGITS==='function')window.FLOSI_LATINIZE_DIGITS();
  }finally{applying=false}
}
function saveFromControls(){
  const ls=document.getElementById('settingsLang'),cs=document.getElementById('settingsCurrency');
  if(ls)localStorage.setItem('flosi-lang',ls.value);if(cs)localStorage.setItem('flosi-currency',cs.value);
  const meta=localeMeta[(ls&&ls.value)||'ar']||localeMeta.ar;localStorage.setItem('flosi-dir',meta.dir);localStorage.setItem('flosi-locale',meta.locale);
  localStorage.removeItem('flosi-lang-preview');localStorage.removeItem('flosi-currency-preview');applyDocument();
}
function bind(){
  document.addEventListener('click',e=>{if(e.target.closest('#settingsSaveLocale'))setTimeout(saveFromControls,0)});
  document.addEventListener('change',e=>{
    if(!e.target||!['settingsLang','settingsCurrency'].includes(e.target.id))return;
    const ls=document.getElementById('settingsLang'),cs=document.getElementById('settingsCurrency');
    if(ls)localStorage.setItem('flosi-lang-preview',ls.value);if(cs)localStorage.setItem('flosi-currency-preview',cs.value);requestAnimationFrame(applyDocument);
  });
}
function startObserver(){
  observer=new MutationObserver(ms=>{if(applying)return;if(ms.some(m=>m.type==='childList'&&m.addedNodes.length))requestAnimationFrame(applyDocument)});
  observer.observe(document.body,{childList:true,subtree:true});
}
function loadPreviewState(){
  if(window.__FLOSI_PREVIEW_STATE__||document.getElementById('flosiPreviewStateScript'))return;
  const script=document.createElement('script');script.id='flosiPreviewStateScript';script.src='flosi-preview-state.js?v=20260823-zero-iqd-1';document.body.appendChild(script);
}

if(!localStorage.getItem('flosi-lang'))localStorage.setItem('flosi-lang','ar');
if(!localStorage.getItem('flosi-currency'))localStorage.setItem('flosi-currency','IQD');
localStorage.removeItem('flosi-lang-preview');localStorage.removeItem('flosi-currency-preview');
bind();applyDocument();startObserver();loadPreviewState();
})();
