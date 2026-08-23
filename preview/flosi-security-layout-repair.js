(()=>{
'use strict';
if(window.__FLOSI_SECURITY_LAYOUT_REPAIR__)return;
window.__FLOSI_SECURITY_LAYOUT_REPAIR__=true;

const STYLE_ID='flosiSecurityLayoutRepairStyle';
function ensureStyle(){
  if(document.getElementById(STYLE_ID))return;
  const s=document.createElement('style');
  s.id=STYLE_ID;
  s.textContent=`
  [data-flosi-password-overlay]{position:fixed!important;inset:0!important;z-index:260!important;align-items:center!important;justify-content:center!important;padding:18px!important;background:rgba(22,17,29,.56)!important;backdrop-filter:blur(10px)!important;-webkit-backdrop-filter:blur(10px)!important;overflow:auto!important}
  [data-flosi-password-overlay].open{display:flex!important}
  [data-flosi-password-sheet]{position:relative!important;inset:auto!important;width:min(100%,460px)!important;max-width:460px!important;height:auto!important;min-height:0!important;max-height:calc(100dvh - 36px)!important;overflow:auto!important;margin:auto!important;padding:20px!important;background:#fff!important;border:1px solid rgba(255,255,255,.9)!important;border-radius:26px!important;box-shadow:0 28px 70px rgba(25,16,38,.28)!important;color:var(--text,#17131f)!important;transform:none!important}
  [data-flosi-password-sheet]>*{position:relative!important;inset:auto!important;float:none!important;visibility:visible!important;opacity:1!important;max-width:none!important}
  [data-flosi-password-title]{display:block!important;margin:0 0 16px!important;font-size:18px!important;line-height:1.5!important;color:var(--text,#17131f)!important}
  [data-flosi-password-field]{display:grid!important;gap:6px!important;margin:11px 0!important;width:100%!important}
  [data-flosi-password-field] label{display:block!important;font-size:9px!important;color:var(--muted,#918a9c)!important;font-weight:700!important}
  [data-flosi-password-field] input{display:block!important;width:100%!important;height:48px!important;border:1px solid #e5ddef!important;background:#faf9fc!important;border-radius:14px!important;padding:0 13px!important;outline:0!important;color:var(--text,#17131f)!important}
  [data-flosi-password-field] input:focus{border-color:#aa7cff!important;box-shadow:0 0 0 4px rgba(123,68,239,.08)!important;background:#fff!important}
  [data-flosi-password-note]{display:flex!important;align-items:flex-start!important;gap:8px!important;position:static!important;width:100%!important;height:auto!important;min-height:0!important;margin:12px 0 0!important;padding:9px 11px!important;border:1px solid #eadffd!important;border-radius:12px!important;background:#faf7ff!important;color:#62596f!important;font-size:9px!important;font-weight:500!important;line-height:1.7!important;text-align:start!important;box-shadow:none!important;overflow:visible!important;white-space:normal!important}
  [data-flosi-password-note]::before{content:'i'!important;display:grid!important;place-items:center!important;position:static!important;flex:0 0 18px!important;width:18px!important;height:18px!important;margin-top:0!important;border-radius:50%!important;background:#efe6ff!important;color:var(--p,#7b44ef)!important;font-family:Inter,system-ui,sans-serif!important;font-size:10px!important;font-weight:700!important}
  [data-flosi-password-actions]{display:grid!important;grid-template-columns:1fr 1fr!important;gap:10px!important;width:100%!important;margin-top:16px!important}
  [data-flosi-password-actions] button{display:block!important;width:100%!important;height:46px!important;border:0!important;border-radius:14px!important;font-size:10px!important;font-weight:700!important}
  [data-flosi-password-save]{background:linear-gradient(145deg,#955bff,#7138eb)!important;color:#fff!important}
  [data-flosi-password-cancel]{background:#f1eef6!important;color:#615a69!important}
  @media(max-width:520px){[data-flosi-password-overlay]{padding:12px!important}[data-flosi-password-sheet]{padding:18px!important;border-radius:23px!important}}
  `;
  document.head.appendChild(s);
}

function text(el){return (el?.textContent||'').replace(/\s+/g,' ').trim()}
function findSheet(title,note){
  let n=note?.parentElement;
  while(n&&n!==document.body){
    const t=text(n);
    if(t.includes('تعيين كلمة المرور')&&t.includes('حفظ')&&t.includes('إلغاء')&&n.querySelectorAll('input').length>=2)return n;
    n=n.parentElement;
  }
  n=title?.parentElement;
  while(n&&n!==document.body){
    const t=text(n);
    if(t.includes('حفظ')&&t.includes('إلغاء')&&n.querySelectorAll('input').length>=2)return n;
    n=n.parentElement;
  }
  return null;
}
function commonParent(a,b,limit){
  if(!a||!b)return null;
  let n=a.parentElement;
  while(n&&n!==limit&&n!==document.body){if(n.contains(b))return n;n=n.parentElement}
  return null;
}
function repair(){
  ensureStyle();
  const candidates=[...document.querySelectorAll('h1,h2,h3,h4,b,strong,div,span')];
  const title=candidates.find(el=>text(el)==='تعيين كلمة المرور');
  const note=candidates.find(el=>text(el).includes('PBKDF2')||text(el).includes('6 أحرف على الأقل'));
  if(!title&&!note)return;
  const sheet=findSheet(title,note);if(!sheet)return;
  sheet.setAttribute('data-flosi-password-sheet','');
  if(title)title.setAttribute('data-flosi-password-title','');
  const overlay=sheet.parentElement;if(overlay&&overlay!==document.body)overlay.setAttribute('data-flosi-password-overlay','');

  const passwords=[...sheet.querySelectorAll('input[type="password"],input')].slice(0,2);
  passwords.forEach(input=>{
    let wrap=input.parentElement;
    while(wrap&&wrap!==sheet&&wrap.querySelectorAll('input').length>1)wrap=wrap.parentElement;
    if(wrap&&wrap!==sheet)wrap.setAttribute('data-flosi-password-field','');
  });

  const buttons=[...sheet.querySelectorAll('button')];
  const save=buttons.find(b=>text(b)==='حفظ');
  const cancel=buttons.find(b=>text(b)==='إلغاء');
  const actions=commonParent(save,cancel,sheet);
  if(actions)actions.setAttribute('data-flosi-password-actions','');
  save?.setAttribute('data-flosi-password-save','');
  cancel?.setAttribute('data-flosi-password-cancel','');

  if(note){
    note.setAttribute('data-flosi-password-note','');
    note.textContent='6 أحرف على الأقل. كلمة المرور لا تُحفظ كنص صريح، بل يُحفظ مشتق آمن للتحقق.';
  }

  [...sheet.children].forEach(child=>{
    child.style.removeProperty('position');
    child.style.removeProperty('inset');
    child.style.removeProperty('height');
    child.style.removeProperty('min-height');
    child.style.removeProperty('max-height');
  });
}

repair();
const observer=new MutationObserver(()=>requestAnimationFrame(repair));
observer.observe(document.documentElement,{childList:true,subtree:true,attributes:true,attributeFilter:['class','style']});
setTimeout(repair,120);
setTimeout(repair,500);
})();
