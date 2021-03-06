$().ready(function() {
	validateRule();
});

$.validator.setDefaults({
	submitHandler : function() {
		save();
	}
});
function save() {
	$.ajax({
		cache : true,
		type : "POST",
		url : "/coach/agent/saveTopAgent",
		data : $('#signupForm').serialize(),// 你的formid
		async : false,
		error : function(request) {
			parent.layer.alert("Connection error");
		},
		success : function(data) {
			if (data.code == 0) {
				parent.layer.msg("操作成功");
				parent.reLoad();
				var index = parent.layer.getFrameIndex(window.name); // 获取窗口索引
				parent.layer.close(index);

			} else {
				parent.layer.alert(data.msg)
			}
		}
	});

}

function clearNoNum(obj){ 
    obj.value = obj.value.replace(/[^\d.]/g,"");  //清除“数字”和“.”以外的字符  
    obj.value = obj.value.replace(/\.{2,}/g,"."); //只保留第一个. 清除多余的  
    obj.value = obj.value.replace(".","$#$").replace(/\./g,"").replace("$#$","."); 
    obj.value = obj.value.replace(/^(\-)*(\d+)\.(\d\d).*$/,'$1$2.$3');//只能输入两个小数  
    if(obj.value.indexOf(".")< 0 && obj.value !=""){//以上已经过滤，此处控制的是如果没有小数点，首位不能为类似于 01、02的金额 
        obj.value= parseFloat(obj.value); 
    } 
} 

function validateRule() {
	var icon = "<i class='fa fa-times-circle'></i> ";
	$("#signupForm").validate({
		rules : {
			topAgentName : {
				required : true,
				remote : {
					url : "/coach/agent/exitTopAgentName", // 后台处理程序
					type : "post", // 数据发送方式
					dataType : "json", // 接受数据格式
					data : { // 要传递的数据
						topAgentName : function() {
							return $("#topAgentName").val();
						}
					}
				}
			},
			contacts : {
				required : true
			},
			tel : {
				required : true
			},
			account : {
				required : true
			},
			divide : {
				required : true
			}
		},
		messages : {

			topAgentName : {
				required : icon + "请输入名称",
				remote : icon + "名称已经存在"
			},
			contacts : {
				required : icon + "请输入联系人"
			},
			tel : {
				required : icon + "请输入电话"
			},
			account : {
				required : icon + "请输入微信号"
			},
			divide : {
				required : icon + "请输入分销比例"
			},
		}
	})
}
