(()=>{
'use strict';
if(window.__FLOSI_HOME_STRICT_I18N__) return;
window.__FLOSI_HOME_STRICT_I18N__=true;
const I=window.FLOSI_I18N||{};
const zh={
 snapshot:'这是你今天的财务概览',netWorth:'净资产',monthGain:'本月 +1,420',safeSpend:'可安心支出',monthForecast:'月末预测',dailyBrief:'每日财务简报',briefBody:'未来3天有两笔付款。本周支出比平时低12%，今天可安心支出47 USD，不会影响你的目标。',incomeMonth:'本月收入',expenseMonth:'本月支出',dueSoon:'即将到期',within3:'3天内',savingsRate:'储蓄率',excellent:'优秀',nextDecisions:'你的下一步决策',showAI:'查看智能分析',pace:'如果保持当前节奏',expectedBalance:'预计月末余额',travelGoal:'旅行目标',upcoming:'即将到来的承诺',viewAll:'查看全部',rent:'房租',after2:'2天后',internet:'互联网订阅',after3:'3天后',digital:'数字订阅',after5:'5天后',intelligence:'Flosi 智能财务',open:'打开',threeNotes:'今天的三条重要提示',note1:'餐饮支出比上个月低18%。',note2:'一个定期订阅本月涨价3 USD。',note3:'如果再储蓄120 USD，你可以提前一个月达到旅行目标。',quick:'快捷操作',movement:'交易',goal:'目标',whatIf:'如果…',recent:'最近交易',today:'今天',transactions:'交易',insights:'智能',me:'我',shopping:'购物',salary:'工资',mainAccount:'主账户'};
function lang(){return document.getElementById('settingsLang')?.value||localStorage.getItem('flosi-lang-preview')||localStorage.getItem('flosi-lang')||'ar'}
function q(s){return document.querySelector(s)}
function qa(s){return [...document.querySelectorAll(s)]}
function set(el,text){if(el&&text!=null)el.textContent=text}
function applyZh(){
 const h=q('#home');if(!h)return;
 set(q('#home .title'),'早上好');set(q('#home .sub'),zh.snapshot);
 const heroLabels=qa('#home .hero > .heroLabel');set(heroLabels[0],zh.netWorth);set(heroLabels[1],zh.monthGain);
 const glass=qa('#home .hero .glass small');set(glass[0],zh.safeSpend);set(glass[1],zh.monthForecast);
 set(q('#home .brief b'),zh.dailyBrief);set(q('#home .brief p'),zh.briefBody);
 const metrics=qa('#home .grid2 .metric');set(metrics[0]?.querySelector('small'),zh.incomeMonth);set(metrics[1]?.querySelector('small'),zh.expenseMonth);set(metrics[2]?.querySelector('small'),zh.dueSoon);set(metrics[3]?.querySelector('small'),zh.savingsRate);set(metrics[2]?.querySelector('.warn'),zh.within3);if(metrics[3]){const g=metrics[3].querySelector('.good');if(g)set(g,zh.excellent)}
 const sections=qa('#home .sectionTitle');
 set(sections[0]?.querySelector('b'),zh.nextDecisions);set(sections[0]?.querySelector('button'),zh.showAI);
 set(q('#home .forecastMain small'),zh.pace);set(q('#home .forecastMain .metricLabel:not(small)'),zh.expectedBalance);set(q('#home .forecast .goalText b'),zh.travelGoal);set(q('#home .forecast .goalText small'),'3,400 / 5,000');
 set(sections[1]?.querySelector('b'),zh.upcoming);set(sections[1]?.querySelector('button'),zh.viewAll);
 const obs=qa('#home .obligation');set(obs[0]?.querySelector('b'),zh.rent);set(obs[0]?.querySelector('small'),zh.after2);set(obs[1]?.querySelector('b'),zh.internet);set(obs[1]?.querySelector('small'),zh.after3);set(obs[2]?.querySelector('b'),zh.digital);set(obs[2]?.querySelector('small'),zh.after5);
 set(sections[2]?.querySelector('b'),zh.intelligence);set(sections[2]?.querySelector('button'),zh.open);set(q('#home .intelHead b'),zh.threeNotes);const notes=qa('#home .intel li');set(notes[0],zh.note1);set(notes[1],zh.note2);set(notes[2],zh.note3);
 set(sections[3]?.querySelector('b'),zh.quick);const acts=qa('#home .actions .action small');set(acts[0],zh.movement);set(acts[1],zh.goal);set(acts[2],zh.whatIf);set(acts[3],zh.transactions);
 set(sections[4]?.querySelector('b'),zh.recent);set(sections[4]?.querySelector('button'),zh.viewAll);const tx=qa('#home #recent .tx');set(tx[0]?.querySelector('b'),zh.shopping);set(tx[0]?.querySelector('small'),'Visa • '+zh.today);set(tx[1]?.querySelector('b'),zh.salary);set(tx[1]?.querySelector('small'),zh.mainAccount);
 const nav=qa('.nav .navLabel');set(nav[0],zh.today);set(nav[1],zh.transactions);set(nav[2],zh.insights);set(nav[3],zh.me);
 const side=qa('.sideNav [data-go]');if(side.length){side[0].innerHTML='⌂ '+zh.today;side[1].innerHTML='⇄ '+zh.transactions;side[2].innerHTML='✦ '+zh.insights;side[3].innerHTML='◎ '+zh.goal;side[4].innerHTML='◉ '+zh.me}
 document.documentElement.lang='zh-CN';document.documentElement.dir='ltr';document.body.dir='ltr';
 document.body.style.fontFamily='"Noto Sans SC","Noto Sans",sans-serif';
}
function apply(){if(lang()==='zh-CN')applyZh()}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',()=>{apply();setTimeout(apply,80);setTimeout(apply,350)},{once:true});else{apply();setTimeout(apply,80);setTimeout(apply,350)}
document.addEventListener('change',e=>{if(e.target?.id==='settingsLang')setTimeout(apply,30)});
document.addEventListener('click',e=>{if(e.target.closest('[data-go]'))setTimeout(apply,30)});
})();