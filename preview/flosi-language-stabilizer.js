(()=>{
'use strict';
if(window.__FLOSI_LANGUAGE_STABILIZER__)return;
window.__FLOSI_LANGUAGE_STABILIZER__=true;
let timers=[];
function apply(){try{window.FLOSI_APPLY_GLOBAL_LOCALE?.();window.FLOSI_RENDER_LOCALE_PAGE?.();}catch(e){console.error('Flosi locale apply:',e)}}
function settle(){timers.forEach(clearTimeout);timers=[0,60,180,420,900].map(ms=>setTimeout(apply,ms));}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',settle,{once:true});else settle();
window.addEventListener('load',settle,{once:true});
document.addEventListener('change',e=>{if(e.target?.id==='settingsLang')settle();});
document.addEventListener('click',e=>{if(e.target?.closest('[data-go],#settingsSaveLocale,#localeApplySmart'))settle();});
})();
