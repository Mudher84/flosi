(()=>{
'use strict';
if(window.__FLOSI_LOCALE_COHERENCE__)return;
window.__FLOSI_LOCALE_COHERENCE__=true;

let applying=false;
let queued=false;

function selectedLang(){
  return localStorage.getItem('flosi-lang-preview')||localStorage.getItem('flosi-lang')||'ar';
}
function table(){return window.FLOSI_I18N||{}}
function meta(lang){
  const t=table();
  const pack=t[lang]||t.ar||{};
  const rtl=new Set(['ar','fa','ur','he']);
  return {dir:pack.dir||(rtl.has(lang)?'rtl':'ltr')};
}
function reverseMap(){
  const reverse=new Map();
  Object.values(table()).forEach(pack=>{
    const tr=pack&&pack.t;
    if(!tr)return;
    Object.entries(tr).forEach(([key,value])=>{
      if(typeof value==='string'&&value.trim())reverse.set(value.trim(),key);
    });
  });
  return reverse;
}
function skip(node){
  const p=node.parentElement;
  return !p||!!p.closest('script,style,textarea,input,option,[data-locale-no-transform]');
}
function translateNode(node,lang,reverse){
  if(skip(node))return;
  const raw=node.nodeValue||'';
  const trimmed=raw.trim();
  if(!trimmed)return;
  const key=reverse.get(trimmed);
  const target=table()[lang]&&table()[lang].t&&key?table()[lang].t[key]:null;
  if(!target||target===trimmed)return;
  const at=raw.indexOf(trimmed);
  node.nodeValue=raw.slice(0,at)+target+raw.slice(at+trimmed.length);
}
function apply(){
  queued=false;
  if(applying||!document.body)return;
  applying=true;
  try{
    const lang=selectedLang();
    const m=meta(lang);
    document.documentElement.lang=lang;
    document.documentElement.dir=m.dir;
    document.body.setAttribute('dir',m.dir);
    const reverse=reverseMap();
    const walker=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT);
    const nodes=[];
    while(walker.nextNode())nodes.push(walker.currentNode);
    nodes.forEach(n=>translateNode(n,lang,reverse));
  }finally{applying=false}
}
function schedule(){
  if(queued)return;
  queued=true;
  requestAnimationFrame(()=>requestAnimationFrame(apply));
}

document.addEventListener('change',e=>{
  if(e.target&&['settingsLang','languageSelect'].includes(e.target.id))schedule();
},true);
document.addEventListener('click',e=>{
  if(e.target&&e.target.closest&&e.target.closest('#settingsSaveLocale,#saveLocaleBtn'))setTimeout(schedule,0);
},true);

if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',schedule,{once:true});else schedule();
new MutationObserver(ms=>{
  if(applying)return;
  if(ms.some(m=>m.type==='childList'&&m.addedNodes.length))schedule();
}).observe(document.documentElement,{childList:true,subtree:true});
})();
