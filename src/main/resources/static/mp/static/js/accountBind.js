function bind() {
    var accountNumber = $("#accountNumber").val();
    var coach_id = $("#userNumber").val();
    var openid = getUrlParam("openid");
    var unionid = getUrlParam("unionid");
    var wechat_name = getUrlParam("wechat_name");
    var headimg = getUrlParam("headimg");
    var userType = getUrlParam("userType");

    var requestJSON = {
        "openid": openid,
        "unionid": unionid,
        "wechat_name": wechat_name,
        "headimg": headimg,
        "account": accountNumber,
        "coach_id": coach_id,
        "bg_password":coach_id
    };

    if (userType == COACH) {
        $.ajax({
            url: coachBingAccount_url,
            type: TYPE_POST,
            data: requestJSON,
            dataType: DATA_TYPE_JSON,
            success: function (data) {
                if (data.code === -1) {
                    $.toast(data.msg, "forbidden", 2000);
                } else {
                    $.toast(data.msg, 2000);
                }
            }
        });
    }

    if (userType == AGENT) {
        $.ajax({
            url: agentBindAccount_url,
            type: TYPE_POST,
            data: requestJSON,
            dataType: DATA_TYPE_JSON,
            success: function (data) {
                if (data.code === -1) {
                    $.toast(data.msg, "forbidden", 2000);
                } else {
                    $.toast(data.msg, 2000);
                }
            }
        });
    }
}