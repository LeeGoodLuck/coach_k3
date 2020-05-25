// var baseUrl = "http://jpjlxy.top/";//coach.hizhenxing.com:8069 jpjlxy.top 192.168.0.168  47.95.121.103
// var baseUrl = "http://jiangxibaoyilai.cn/";//coach.hizhenxing.com:8069 jpjlxy.top 192.168.0.168  47.95.121.103       #江西
var baseUrl = "http://zxyoupin.com/";//coach.hizhenxing.com:8069 jpjlxy.top 192.168.0.168  47.95.121.103       #广州
// var baseUrl = "http://coach.kaimatekeji.com/";//coach.hizhenxing.com:8069 jpjlxy.top 192.168.0.168  47.95.121.103       #河北
// var baseUrl = "http://yxjk.ynfer.com/";//coach.hizhenxing.com:8069 jpjlxy.top 192.168.0.168  47.95.121.103       #云南

var titleStr = "金牌教练";

//微信登陆
var wechat_auth_url = baseUrl + "coach_background/coach/wechat_auth.do";
var coachLogin_url = baseUrl + "coach_background/coach/login.do";
var studentLogin_url = baseUrl + "coach_background/student/login.do";
var agentLogin_url = baseUrl + "coach_background/agent/login.do";
//代理商账号绑定
var agentBindAccount_url = baseUrl + "coach_background/agent/bindAccount.do";
//教练账号绑定
var coachBingAccount_url = baseUrl + "coach_background/coach/bindAccount.do";
//教练收入明细查询
var coachDetail_url= baseUrl+"coach_background/coach/queryIncomeList.do";
//教练管理
var coachFind_url = baseUrl+"coach_background/agent/queryCoach.do";
//考试成绩查询
var resultInquiry_url = baseUrl+"coach_background/student/queryTest.do";
//考试记录查询
var queryTestList_url = baseUrl+"coach_background/student/queryTestList.do";
//教练收入查询(本日收益,本月收益)
var coach_income_url = baseUrl+"coach_background/coach/queryIncome.do";
//设备激活
var deviceActivation_url = baseUrl+"coach_background/agent/activeDevice.do";
//教练登陆到设备
var coach_login_device_url = baseUrl+"coach_background/coach/confirmTest.do";
//学员登陆到设备
var student_login_device_url = baseUrl+"coach_background/student/confirmTest.do";



//用户类型
var COACH = 1;
var STUDENT = 2;
var AGENT = 3;

var COACH_KEY = "COACH_KEY";
var STUDENT_KEY = "STUDENT_KEY";
var AGENT_KEY = "AGENT_KEY";

var TYPE_GET = "get";
var TYPE_POST = "post";
var DATA_TYPE_JSON = "json";

//获得地址栏参数
function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg);  //匹配目标参数
    if (r != null) return unescape(r[2]);
    return null; //返回参数值
}