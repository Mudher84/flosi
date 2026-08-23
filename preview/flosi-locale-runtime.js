(()=>{
'use strict';
if(window.__FLOSI_LOCALE_RUNTIME__) return;
window.__FLOSI_LOCALE_RUNTIME__=true;

const I18N=window.FLOSI_I18N||{};
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
const translatedText=new WeakMap();
let applying=false;
let observer=null;

const manual={
  ar:{
    'Daily Money Brief':'الملخص المالي اليومي',
    'Flosi Intelligence':'ذكاء Flosi المالي',
    'Smart Money OS':'نظام المال الذكي',
    'Main':'الحساب الرئيسي',
    'Cash':'نقداً',
    'Settings & privacy':'الإعدادات والخصوصية',
    'Global settings':'إعدادات عالمية',
    'Base currency':'العملة الأساسية',
    'App language':'لغة التطبيق',
    'Recent transactions':'آخر الحركات',
    'Transactions':'الحركات',
    'Reports':'التقارير',
    'Security':'الأمان'
  }
};

function state(){
  const lang=localStorage.getItem('flosi-lang')||'ar';
  const currency=localStorage.getItem('flosi-currency')||'IQD';
  const meta=localeMeta[lang]||localeMeta.ar;
  return {lang,currency,meta};
}

function safeNumber(raw){
  const n=Number(String(raw).replace(/,/g,''));
  return Number.isFinite(n)?n:null;
}

function money(n,currency,locale){
  try{
    return new Intl.NumberFormat(locale,{style:'currency',currency,maximumFractionDigits:currency==='IQD'?0:2}).format(n);
  }catch(_){
    return `${n.toLocaleString(locale)} ${currency}`;
  }
}

function currencyText(text,currency,locale){
  let out=text;
  // two-value goals, e.g. 3,400 / 5,000 USD
  out=out.replace(/([+−-]?\d[\d,]*(?:\.\d+)?)\s*\/\s*([+−-]?\d[\d,]*(?:\.\d+)?)\s+USD\b/g,(m,a,b)=>{
    const na=safeNumber(a.replace(/[+−-]/g,'')),nb=safeNumber(b.replace(/[+−-]/g,''));
    if(na===null||nb===null)return m;
    return `${money(na,currency,locale)} / ${money(nb,currency,locale)}`;
  });
  // code before amount
  out=out.replace(/\bUSD\s*([+−-]?\d[\d,]*(?:\.\d+)?)/g,(m,a)=>{
    const sign=/^[+]/.test(a)?'+':/^[−-]/.test(a)?'−':'';
    const n=safeNumber(a.replace(/[+−-]/g,''));
    return n===null?m:sign+money(n,currency,locale);
  });
  // amount before code
  out=out.replace(/([+−-]?\d[\d,]*(?:\.\d+)?)\s+USD\b/g,(m,a)=>{
    const sign=/^[+]/.test(a)?'+':/^[−-]/.test(a)?'−':'';
    const n=safeNumber(a.replace(/[+−-]/g,''));
    return n===null?m:sign+money(n,currency,locale);
  });
  // leftover standalone currency code in visible copy
  if(currency!=='USD') out=out.replace(/\bUSD\b/g,currency==='IQD'?'د.ع':currency);
  return out;
}

function buildReverse(){
  const reverse=new Map();
  ['ar','en'].forEach(code=>{
    const t=I18N[code]&&I18N[code].t;
    if(!t)return;
    Object.entries(t).forEach(([key,value])=>{
      if(typeof value==='string') reverse.set(value,key);
    });
  });
  return reverse;
}

function translateExact(text,lang,reverse){
  const trimmed=text.trim();
  if(!trimmed)return text;
  let replacement=null;
  const key=reverse.get(trimmed);
  if(key&&I18N[lang]&&I18N[lang].t&&I18N[lang].t[key]) replacement=I18N[lang].t[key];
  if(!replacement&&manual[lang]&&manual[lang][trimmed]) replacement=manual[lang][trimmed];
  if(!replacement)return text;
  const start=text.indexOf(trimmed);
  return text.slice(0,start)+replacement+text.slice(start+trimmed.length);
}

function shouldSkip(node){
  const p=node.parentElement;
  if(!p)return true;
  return !!p.closest('script,style,option,select,textarea,input,[data-locale-no-transform]');
}

function applyText(root=document.body){
  if(!root)return;
  const {lang,currency,meta}=state();
  const reverse=buildReverse();
  const walker=document.createTreeWalker(root,NodeFilter.SHOW_TEXT);
  const nodes=[];
  while(walker.nextNode())nodes.push(walker.currentNode);
  nodes.forEach(node=>{
    if(shouldSkip(node))return;
    if(!originalText.has(node)) originalText.set(node,node.nodeValue||'');
    const base=originalText.get(node)||'';
    let next=translateExact(base,lang,reverse);
    next=currencyText(next,currency,meta.locale);
    translatedText.set(node,next);
    if(node.nodeValue!==next)node.nodeValue=next;
  });
}

function syncControls(){
  const {lang,currency}=state();
  const ls=document.getElementById('settingsLang');
  const cs=document.getElementById('settingsCurrency');
  if(ls&&[...ls.options].some(o=>o.value===lang))ls.value=lang;
  if(cs&&[...cs.options].some(o=>o.value===currency))cs.value=currency;
}

function applyDocument(){
  if(applying)return;
  applying=true;
  try{
    const {lang,meta}=state();
    document.documentElement.lang=lang;
    document.documentElement.dir=meta.dir;
    document.body&&document.body.setAttribute('dir',meta.dir);
    syncControls();
    applyText(document.body);
    if(typeof window.renderLocale==='function'){
      try{window.renderLocale()}catch(_){ }
    }
  }finally{applying=false;}
}

function saveFromControls(){
  const ls=document.getElementById('settingsLang');
  const cs=document.getElementById('settingsCurrency');
  if(ls)localStorage.setItem('flosi-lang',ls.value);
  if(cs)localStorage.setItem('flosi-currency',cs.value);
  const meta=localeMeta[(ls&&ls.value)||'ar']||localeMeta.ar;
  localStorage.setItem('flosi-dir',meta.dir);
  localStorage.setItem('flosi-locale',meta.locale);
  applyDocument();
}

function bind(){
  document.addEventListener('click',e=>{
    if(e.target.closest('#settingsSaveLocale')) setTimeout(saveFromControls,0);
  });
  document.addEventListener('change',e=>{
    if(e.target&&['settingsLang','settingsCurrency'].includes(e.target.id)){
      // live preview across the whole app without committing until Save.
      const ls=document.getElementById('settingsLang'),cs=document.getElementById('settingsCurrency');
      if(ls)localStorage.setItem('flosi-lang-preview',ls.value);
      if(cs)localStorage.setItem('flosi-currency-preview',cs.value);
    }
  });
}

function startObserver(){
  observer=new MutationObserver(mutations=>{
    if(applying)return;
    let needs=false;
    for(const m of mutations){
      if(m.type==='childList'&&m.addedNodes.length){needs=true;break;}
    }
    if(needs)requestAnimationFrame(applyDocument);
  });
  observer.observe(document.body,{childList:true,subtree:true});
}

// Migration: old preview defaulted to USD even when the Arabic experience was intended.
if(!localStorage.getItem('flosi-lang'))localStorage.setItem('flosi-lang','ar');
if(!localStorage.getItem('flosi-currency'))localStorage.setItem('flosi-currency','IQD');

bind();
applyDocument();
startObserver();
})();
