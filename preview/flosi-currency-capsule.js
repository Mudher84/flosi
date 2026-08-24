(()=>{
'use strict';
if(window.__FLOSI_CURRENCY_CAPSULE__)return;window.__FLOSI_CURRENCY_CAPSULE__=true;
const select=document.getElementById('settingsCurrency');
if(!select)return;
const wrap=select.parentElement;
if(!wrap)return;
select.style.display='none';
wrap.style.position='relative';
const btn=document.createElement('button');
btn.type='button';
btn.id='flosiCurrencyCapsule';
btn.style.cssText='width:100%;height:50px;border:1px solid #a774ff;border-radius:999px;background:#fff;color:#17131f;padding:0 18px;display:flex;align-items:center;justify-content:space-between;gap:12px;font:inherit;cursor:pointer;box-shadow:0 7px 20px rgba(93,59,141,.07);direction:rtl';
const label=document.createElement('span');
label.textContent=select.value;
label.style.cssText='flex:1;text-align:center;font-weight:500';
const arrow=document.createElement('span');
arrow.textContent='⌄';
arrow.style.cssText='color:#7b44ef;font-size:16px;transition:transform .18s ease';
btn.append(label,arrow);
const menu=document.createElement('div');
menu.id='flosiCurrencyMenu';
menu.style.cssText='position:fixed;z-index:9999;display:none;background:#fff;border:1px solid #e7dcf7;border-radius:20px;padding:7px;box-shadow:0 22px 55px rgba(52,33,78,.18);max-height:260px;overflow:auto;scrollbar-width:none;-ms-overflow-style:none;direction:ltr';
const style=document.createElement('style');
style.textContent='#flosiCurrencyMenu::-webkit-scrollbar{display:none} #flosiCurrencyMenu button{font:inherit}';
document.head.appendChild(style);
function build(){menu.innerHTML='';[...select.options].forEach(opt=>{const item=document.createElement('button');item.type='button';item.textContent=opt.textContent;item.dataset.value=opt.value;item.style.cssText='width:100%;min-height:40px;border:0;border-radius:13px;background:transparent;padding:9px 14px;text-align:center;color:#17131f;cursor:pointer;display:block';if(opt.value===select.value){item.style.background='linear-gradient(135deg,#f2eaff,#ebe0ff)';item.style.color='#7b44ef';item.style.fontWeight='700';}item.onclick=()=>{select.value=opt.value;label.textContent=opt.textContent;select.dispatchEvent(new Event('change',{bubbles:true}));close();};menu.appendChild(item);});}
function place(){const r=btn.getBoundingClientRect();menu.style.left=r.left+'px';menu.style.top=(r.bottom+6)+'px';menu.style.width=r.width+'px';}
function open(){build();place();menu.style.display='block';arrow.style.transform='rotate(180deg)';}
function close(){menu.style.display='none';arrow.style.transform='';}
btn.onclick=e=>{e.preventDefault();e.stopPropagation();menu.style.display==='block'?close():open();};
document.addEventListener('click',e=>{if(!menu.contains(e.target)&&e.target!==btn)close();});
window.addEventListener('resize',()=>{if(menu.style.display==='block')place();});
window.addEventListener('scroll',()=>{if(menu.style.display==='block')place();},{passive:true});
wrap.appendChild(btn);document.body.appendChild(menu);
})();