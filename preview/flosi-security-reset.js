(()=>{
'use strict';
// Password authentication was removed from Flosi security settings.
window.__FLOSI_SECURITY_RESET__=true;

// Keep language/currency values optically centered inside the rounded selector,
// while preserving the purple picker arrow at the inline-start edge.
if(!document.getElementById('flosi-select-center-fix')){
  const style=document.createElement('style');
  style.id='flosi-select-center-fix';
  style.textContent=`
    #settingsLang.localeSelect,
    #settingsCurrency.localeSelect{
      appearance:none!important;
      -webkit-appearance:none!important;
      text-align:center!important;
      text-align-last:center!important;
      direction:ltr!important;
      padding-left:52px!important;
      padding-right:52px!important;
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
      direction:ltr!important;
      text-align:center!important;
    }
  `;
  document.head.appendChild(style);
}
})();
