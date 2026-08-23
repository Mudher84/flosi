(()=>{
'use strict';
if(window.__FLOSI_SECURITY_RESET__)return;
window.__FLOSI_SECURITY_RESET__=true;

const RESET_KEYS=/^(flosi-.*(password|passcode|pass|pin|security|biometric|finger|face|lock))/i;

function toast(msg){
  if(typeof window.toast==='function'){window.toast(msg);return}
  const t=document.getElementById('toast');
  if(!t)return;
  t.textContent=msg;t.classList.add('show');setTimeout(()=>t.classList.remove('show'),1800);
}

function clearPreviewSecurity(){
  const keys=[];
  for(let i=0;i<localStorage.length;i++){
    const k=localStorage.key(i);
    if(k&&RESET_KEYS.test(k))keys.push(k);
  }
  keys.forEach(k=>localStorage.removeItem(k));
  sessionStorage.removeItem('flosi-security-unlocked');
  sessionStorage.removeItem('flosi-unlocked');
}

function ensureStyle(){
  if(document.getElementById('flosiSecurityResetStyle'))return;
  const s=document.createElement('style');s.id='flosiSecurityResetStyle';
  s.textContent=`
    .flosiForgotPassword{border:0;background:transparent;color:var(--p);font:inherit;font-size:9px;font-weight:700;padding:6px 2px;cursor:pointer;text-decoration:none}
    .flosiResetModal{position:fixed;inset:0;z-index:310;background:rgba(20,14,29,.58);display:none;align-items:center;justify-content:center;padding:18px;backdrop-filter:blur(10px);-webkit-backdrop-filter:blur(10px)}
    .flosiResetModal.open{display:flex}
    .flosiResetCard{width:min(100%,440px);background:#fff;border-radius:24px;padding:20px;box-shadow:0 28px 70px rgba(25,16,38,.28);text-align:start}
    .flosiResetCard h3{margin:0 0 8px;font-size:18px}.flosiResetCard p{margin:0;color:#756d80;font-size:10px;line-height:1.9}
    .flosiResetWarn{margin-top:12px;padding:11px 12px;border:1px solid #eadcff;background:#faf7ff;border-radius:14px;color:#4e3a6e;font-size:9px;line-height:1.8}
    .flosiResetActions{display:grid;grid-template-columns:1fr 1fr;gap:9px;margin-top:16px}.flosiResetActions button{height:46px;border:0;border-radius:14px;font:inherit;font-size:10px;font-weight:700}
    .flosiResetConfirm{background:linear-gradient(145deg,#955bff,#7138eb);color:#fff}.flosiResetCancel{background:#f1eef6;color:#615a69}
  `;
  document.head.appendChild(s);
}

function ensureModal(){
  let m=document.getElementById('flosiResetPasswordModal');if(m)return m;
  m=document.createElement('div');m.id='flosiResetPasswordModal';m.className='flosiResetModal';m.setAttribute('data-locale-no-transform','');
  m.innerHTML=`<div class="flosiResetCard"><h3>إعادة تعيين كلمة المرور</h3><p>سيتم حذف كلمة المرور وإعدادات البصمة من نسخة العرض فقط، ثم يمكنك تعيين كلمة مرور جديدة.</p><div class="flosiResetWarn">هذا ليس استرجاعاً لكلمة المرور القديمة؛ الكلمة القديمة لا يمكن قراءتها لأنها غير محفوظة كنص.</div><div class="flosiResetActions"><button class="flosiResetConfirm" id="flosiResetConfirm">إعادة التعيين</button><button class="flosiResetCancel" id="flosiResetCancel">إلغاء</button></div></div>`;
  document.body.appendChild(m);
  m.addEventListener('click',e=>{if(e.target===m)m.classList.remove('open')});
  m.querySelector('#flosiResetCancel').onclick=()=>m.classList.remove('open');
  m.querySelector('#flosiResetConfirm').onclick=()=>{
    clearPreviewSecurity();
    m.classList.remove('open');
    toast('تمت إعادة تعيين أمان نسخة العرض. عيّن كلمة مرور جديدة.');
    setTimeout(()=>location.reload(),700);
  };
  return m;
}

function openReset(){ensureModal().classList.add('open')}

function installButton(){
  const security=document.getElementById('security');if(!security)return;
  if(security.querySelector('.flosiForgotPassword'))return;
  const buttons=[...security.querySelectorAll('button')];
  const change=buttons.find(b=>/تغيير|كلمة المرور|password/i.test((b.textContent||'').trim()));
  if(!change)return;
  const btn=document.createElement('button');btn.type='button';btn.className='flosiForgotPassword';btn.textContent='نسيت كلمة المرور؟';btn.onclick=e=>{e.preventDefault();e.stopPropagation();openReset()};
  const host=change.closest('.settingsRow,.secRow,.securityRow,.card,.panel')||change.parentElement;
  if(host)host.appendChild(btn);
}

ensureStyle();ensureModal();installButton();
const obs=new MutationObserver(()=>installButton());
obs.observe(document.body,{childList:true,subtree:true});
})();
