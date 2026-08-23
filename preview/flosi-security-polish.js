(()=>{
'use strict';
if(window.__FLOSI_SECURITY_POLISH__)return;
window.__FLOSI_SECURITY_POLISH__=true;

function polishPasswordHint(){
  const all=[...document.querySelectorAll('div,p,small,span')];
  const hint=all.find(el=>{
    const t=(el.textContent||'').trim();
    return t.includes('PBKDF2')&&t.includes('6')&&t.length<220;
  });
  if(!hint||hint.dataset.flosiHintPolished==='1')return;
  hint.dataset.flosiHintPolished='1';
  hint.style.cssText='display:flex;align-items:flex-start;gap:9px;margin:11px 0 2px;padding:10px 12px;border:1px solid #e5daf7;border-radius:14px;background:#faf7ff;color:#5e526d;font-size:10px;font-weight:500;line-height:1.75;text-align:start;box-shadow:none;';
  const text=hint.textContent.trim();
  hint.textContent='';
  const icon=document.createElement('span');
  icon.textContent='i';
  icon.setAttribute('aria-hidden','true');
  icon.style.cssText='width:21px;height:21px;flex:0 0 21px;border-radius:50%;display:grid;place-items:center;background:#efe6ff;color:#7b44ef;font:700 11px Inter,system-ui,sans-serif;margin-top:1px;';
  const copy=document.createElement('span');
  copy.textContent=text;
  copy.style.cssText='flex:1;min-width:0;color:#5e526d;';
  hint.append(icon,copy);
}

const observer=new MutationObserver(()=>polishPasswordHint());
function start(){polishPasswordHint();observer.observe(document.body,{childList:true,subtree:true});}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',start,{once:true});else start();
})();
