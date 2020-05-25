//用户登陆
$(function () {
    var userType = getUrlParam("state");
    var code = getUrlParam("code");

    document.title = titleStr + "--科目三";

    $("#incomeBreakdown").attr("href","collectMoney.html?state="+userType);

    //根据用户信息显示隐藏视图
    if (userType == 3) {
        $("#paymentDetails").hide();
        $("#resultInquiry").hide();
    } else if (userType == 2) {
        //学员
        $("#income").hide();
        $("#coachManagement").hide();
    } else if (userType == 1) {
        //教练
        $("#coachManagement").hide();
        $("#paymentDetails").hide();
        $("#resultInquiry").hide();
    }

    //获得用户信息
    getUserInfo(code, userType);
});

//获得用户信息
function getUserInfo(code, state) {
    var jsonData = {"code": code};
    $.ajax({
        url: wechat_auth_url,
        type: TYPE_POST,
        data: jsonData,
        dataType: DATA_TYPE_JSON,
        success: function (data) {
            if (state === "1") {
                coachLogin(data);
            } else if (state === "2") {
                //学员登陆
                studentLogin(data);
            } else {
                //运营商登陆
                carrierLogin(data);
            }
        }
    });
}

//教练登陆
function coachLogin(userData) {
    var requestData = {
        "openid": userData.openid,
        "unionid": userData.unionid,
        "wechat_name": userData.wechat_name,
        "headimg": userData.headimg
    };
    $.ajax({
        url: coachLogin_url,
        type: TYPE_POST,
        data: requestData,
        dataType: DATA_TYPE_JSON,
        success: function (data) {
            if (data.msg === "请绑定教练账号") {
                alert(data.msg);
                //跳转到账号绑定界面
                $(location).attr('href', 'accountBinding.html?openid=' + requestData.openid + "&unionid=" + requestData.unionid + "&wechat_name=" + requestData.wechat_name + "&headimg=" + requestData.headimg + "&userType=" + COACH);
            }
            if (data.code === 0) {
                console.log(JSON.stringify(data));
                window.localStorage.setItem(COACH_KEY, JSON.stringify(data))
            }
        },
        error: function (msg) {
            alert(JSON.stringify(msg));
        }
    });
}

//学员登陆
function studentLogin(userData) {
    var requestData = {
        "openid": userData.openid,
        "unionid": userData.unionid,
        "wechat_name": userData.wechat_name,
        "headimg": userData.headimg
    };
    $.ajax({
        url: studentLogin_url,
        type: TYPE_POST,
        data: requestData,
        dataType: DATA_TYPE_JSON,
        success: function (data) {
            /*if (data.msg === "还未绑定账号") {
                alert(data.msg);
                //跳转到账号绑定界面
                $(location).attr('href', 'accountBinding.html?openid=' + requestData.openid + "&unionid=" + requestData.unionid + "&wechat_name=" + requestData.wechat_name + "&headimg=" + requestData.headimg + "&userType=" + STUDENT);
            }*/
            if (data.code === 0) {
                window.localStorage.setItem(STUDENT_KEY, JSON.stringify(data));
            } else {
                alert(data.msg);
            }
        },
        error: function (msg) {
            alert(JSON.stringify(msg));
        }
    });
}

//运营商登陆
function carrierLogin(userData) {
    var requestData = {
        "openid": userData.openid,
        "unionid": userData.unionid,
        "wechat_name": userData.wechat_name,
        "headimg": userData.headimg
    };
    $.ajax({
        url: agentLogin_url,
        type: TYPE_POST,
        data: requestData,
        dataType: DATA_TYPE_JSON,
        success: function (data) {
            if (data.msg === "还未绑定账号") {
                alert(data.msg);
                //跳转到账号绑定界面
                $(location).attr('href', 'accountBinding.html?openid=' + requestData.openid + "&unionid=" + requestData.unionid + "&wechat_name=" + requestData.wechat_name + "&headimg=" + requestData.headimg + "&userType=" + AGENT);
            }

            if (data.code === 0) {
                window.localStorage.setItem(AGENT_KEY, JSON.stringify(data));
            }
        },
        error: function (msg) {
            alert(JSON.stringify(msg));
        }
    });
}
