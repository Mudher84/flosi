(()=>{
'use strict';
if(window.__FLOSI_SECURITY_CHANGE_FIX__)return;
window.__FLOSI_SECURITY_CHANGE_FIX__=true;

function text(el){return (el?.textContent||'').replace(/\s+/g,' ').trim()}
function isPasswordEditor(el){
  if(!el)return false;
  const t=text(el);
  return /كلمة المرور|password/i.test(t)&&!!el.querySelector('input[type="password"],input[autocomplete="new-password"],input[autocomplete="current-password"]');
}
function findEditor(){
  const preferred=['#flosiPasswordModal','#flosiSecurityPasswordModal','#securityPasswordModal','.flosiSecurityModal','.securityModal','.modal'];
  for(const selector of preferred){
    for(const el of document.querySelectorAll(selector))if(isPasswordEditor(el))return el;
  }
  return [...document.querySelectorAll('body > div,body > section')].find(isPasswordEditor)||null;
}
function openEditor(modal){
  modal.hidden=false;
  modal.removeAttribute('hidden');
  modal.setAttribute('aria-hidden','false');
  modal.classList.add('open','active','show');
  modal.style.setProperty('display','flex','important');
  modal.style.setProperty('visibility','visible','important');
  modal.style.setProperty('opacity','1','important');
  modal.style.setProperty('pointer-events','auto','important');
  modal.style.setProperty('position','fixed','important');
  modal.style.setProperty('inset','0','important');
  modal.style.setProperty('z-index','9999','important');
  const sheet=modal.querySelector('.flosiSecuritySheet,.securitySheet,.sheet')||modal.firstElementChild;
  if(sheet){
    sheet.style.removeProperty('display');
    sheet.style.setProperty('visibility','visible','important');
    sheet.style.setProperty('opacity','1','important');
  }
  const input=modal.querySelector('input[type="password"],input[autocomplete="current-password"],input[autocomplete="new-password"]');
  if(input)setTimeout(()=>input.focus(),40);
}
function closeEditor(modal){
  modal.classList.remove('open','active','show');
  modal.setAttribute('aria-hidden','true');
  modal.style.setProperty('display','none','important');
}
function bindModalFallback(modal){
  if(modal.dataset.flosiChangeBound==='1')return;
  modal.dataset.flosiChangeBound='1';
  modal.addEventListener('click',e=>{
    if(e.target===modal)closeEditor(modal);
    const b=e.target.closest('button');if(!b)return;
    if(/إلغاء|cancel/i.test(text(b)))closeEditor(modal);
  });
  document.addEventListener('keydown',e=>{if(e.key==='Escape'&&modal.style.display!=='none')closeEditor(modal)});
}
function tryOpen(){
  const modal=findEditor();
  if(!modal)return false;
  bindModalFallback(modal);
  openEditor(modal);
  return true;
}

document.addEventListener('click',e=>{
  const btn=e.target.closest('button');
  if(!btn)return;
  const label=text(btn);
  if(label!=='تغيير'&&!/change password/i.test(label))return;
  if(!btn.closest('#security,[data-screen="security"],.securityScreen,.security-page'))return;
  if(tryOpen()){
    e.preventDefault();
    e.stopImmediatePropagation();
  } else {
    setTimeout(tryOpen,0);
    setTimeout(tryOpen,80);
  }
},true);

const observer=new MutationObserver(()=>{
  const modal=findEditor();if(modal)bindModalFallback(modal);
});
observer.observe(document.documentElement,{childList:true,subtree:true});
})();
