(()=>{
'use strict';
// Password authentication was removed from Flosi security settings.
window.__FLOSI_SECURITY_RESET__=true;

// Browser-native <select> alignment is inconsistent across Chromium/Android.
// Use a centered visual label over the select so the chosen value is truly
// centered inside the whole rounded box, independently from the picker arrow.
if(!document.getElementById('flosi-select-center-fix')){
  const style=document.createElement('style');
  style.id='flosi-select-center-fix';
  style.textContent=`
    .localeSelectWrap{position:relative!important}
    #settingsLang.localeSelect,
    #settingsCurrency.localeSelect{
      appearance:none!important;
      -webkit-appearance:none!important;
      color:transparent!important;
      caret-color:transparent!important;
      padding-left:56px!important;
      padding-right:56px!important;
      background-image:
        linear-gradient(45deg,transparent 50%,#7b44ef 50%),
        linear-gradient(135deg,#7b44ef 50%,transparent 50%),
        linear-gradient(180deg,#fff,#faf8ff)!important;
      background-position:
        36px 22px,
        42px 22px,
        0 0!important;
      background-size:7px 7px,7px 7px,100% 100%!important;
      background-repeat:no-repeat!important;
    }
    #settingsLang.localeSelect option,
    #settingsCurrency.localeSelect option{
      color:#17131f!important;
      background:#fff!important;
    }
    .flosiCenteredSelectValue{
      position:absolute!important;
      inset:0!important;
      display:flex!important;
      align-items:center!important;
      justify-content:center!important;
      padding:0 58px!important;
      pointer-events:none!important;
      color:#17131f!important;
      font:inherit!important;
      line-height:1!important;
      text-align:center!important;
      white-space:nowrap!important;
      overflow:hidden!important;
      text-overflow:ellipsis!important;
      z-index:2!important;
    }
  `;
  document.head.appendChild(style);
}

function bindCenteredSelect(select){
  if(!select||select.dataset.flosiCentered==='1')return;
  const wrap=select.closest('.localeSelectWrap')||select.parentElement;
  if(!wrap)return;
  let label=wrap.querySelector('.flosiCenteredSelectValue');
  if(!label){
    label=document.createElement('span');
    label.className='flosiCenteredSelectValue';
    wrap.appendChild(label);
  }
  const sync=()=>{
    const option=select.options[select.selectedIndex];
    label.textContent=option ? option.textContent : select.value;
  };
  select.dataset.flosiCentered='1';
  select.addEventListener('change',()=>requestAnimationFrame(sync));
  select.addEventListener('input',()=>requestAnimationFrame(sync));
  const observer=new MutationObserver(()=>requestAnimationFrame(sync));
  observer.observe(select,{attributes:true,childList:true,subtree:true});
  sync();
}

function installCenteredSelectors(){
  bindCenteredSelect(document.getElementById('settingsLang'));
  bindCenteredSelect(document.getElementById('settingsCurrency'));
}

installCenteredSelectors();
document.addEventListener('DOMContentLoaded',installCenteredSelectors,{once:true});
setTimeout(installCenteredSelectors,0);
})();
