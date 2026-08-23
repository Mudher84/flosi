(()=>{
'use strict';
if(window.__FLOSI_SECURITY_CONTRAST__)return;
window.__FLOSI_SECURITY_CONTRAST__=true;

function improve(){
  const nodes=[...document.querySelectorAll('div,p,small,span')];
  for(const el of nodes){
    const text=(el.textContent||'').replace(/\s+/g,' ').trim();
    if(!text)continue;
    const isPasswordHint=(text.includes('PBKDF2')||text.startsWith('6 أحرف')||text.includes('6 أحرف على الأقل'))&&text.length<260;
    if(!isPasswordHint)continue;
    el.setAttribute('data-locale-no-transform','');
    Object.assign(el.style,{
      color:'#51485f',
      background:'#f5f1fb',
      border:'1px solid #e7def4',
      borderRadius:'13px',
      padding:'11px 12px',
      fontSize:'10px',
      fontWeight:'600',
      lineHeight:'1.8',
      opacity:'1',
      textAlign:'right',
      display:'block',
      marginTop:'10px',
      marginBottom:'10px'
    });
  }
}

improve();
new MutationObserver(()=>requestAnimationFrame(improve)).observe(document.documentElement,{childList:true,subtree:true});
})();
