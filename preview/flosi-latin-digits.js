(()=>{
'use strict';
if(window.__FLOSI_LATIN_DIGITS__)return;
window.__FLOSI_LATIN_DIGITS__=true;

const digitMap={
  '٠':'0','١':'1','٢':'2','٣':'3','٤':'4','٥':'5','٦':'6','٧':'7','٨':'8','٩':'9',
  '۰':'0','۱':'1','۲':'2','۳':'3','۴':'4','۵':'5','۶':'6','۷':'7','۸':'8','۹':'9'
};
const digitRx=/[٠-٩۰-۹]/g;
const latinize=s=>String(s??'').replace(digitRx,d=>digitMap[d]||d);

function normalizeNode(node){
  if(node.nodeType===Node.TEXT_NODE){
    const p=node.parentElement;
    if(p&&p.closest('script,style'))return;
    const next=latinize(node.nodeValue);
    if(next!==node.nodeValue)node.nodeValue=next;
    return;
  }
  if(node.nodeType!==Node.ELEMENT_NODE)return;
  const el=node;
  if(el.matches('input,textarea')){
    if(typeof el.value==='string')el.value=latinize(el.value);
    if(el.placeholder)el.placeholder=latinize(el.placeholder);
  }
  el.querySelectorAll('input,textarea').forEach(x=>{
    if(typeof x.value==='string')x.value=latinize(x.value);
    if(x.placeholder)x.placeholder=latinize(x.placeholder);
  });
  const w=document.createTreeWalker(el,NodeFilter.SHOW_TEXT);
  while(w.nextNode())normalizeNode(w.currentNode);
}

function normalizeAll(){normalizeNode(document.body)}

const NativeNumberFormat=Intl.NumberFormat;
Intl.NumberFormat=function(locales,options){
  const list=Array.isArray(locales)?locales:[locales||document.documentElement.lang||'en'];
  const normalized=list.map(loc=>{
    const raw=String(loc||'en');
    try{
      const l=new Intl.Locale(raw);
      return new Intl.Locale(l.baseName,{numberingSystem:'latn'}).toString();
    }catch(_){
      return raw.includes('-u-')?raw:raw+'-u-nu-latn';
    }
  });
  return new NativeNumberFormat(normalized,options);
};
Intl.NumberFormat.prototype=NativeNumberFormat.prototype;
Object.setPrototypeOf(Intl.NumberFormat,NativeNumberFormat);

const NativeDateTimeFormat=Intl.DateTimeFormat;
Intl.DateTimeFormat=function(locales,options){
  const list=Array.isArray(locales)?locales:[locales||document.documentElement.lang||'en'];
  const normalized=list.map(loc=>{
    const raw=String(loc||'en');
    try{
      const l=new Intl.Locale(raw);
      return new Intl.Locale(l.baseName,{numberingSystem:'latn'}).toString();
    }catch(_){
      return raw.includes('-u-')?raw:raw+'-u-nu-latn';
    }
  });
  return new NativeDateTimeFormat(normalized,options);
};
Intl.DateTimeFormat.prototype=NativeDateTimeFormat.prototype;
Object.setPrototypeOf(Intl.DateTimeFormat,NativeDateTimeFormat);

normalizeAll();
const observer=new MutationObserver(ms=>{
  for(const m of ms){
    if(m.type==='characterData')normalizeNode(m.target);
    m.addedNodes&&m.addedNodes.forEach(normalizeNode);
  }
});
observer.observe(document.documentElement,{subtree:true,childList:true,characterData:true});
document.addEventListener('input',e=>{
  const el=e.target;
  if(el&&el.matches&&el.matches('input,textarea')){
    const next=latinize(el.value);
    if(next!==el.value){const pos=el.selectionStart;el.value=next;try{el.setSelectionRange(pos,pos)}catch(_){}}
  }
},true);
window.FLOSI_LATINIZE_DIGITS=normalizeAll;
})();
