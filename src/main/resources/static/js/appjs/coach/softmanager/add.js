$().ready(function() {
	validateRule();
	
});

$.validator.setDefaults({
	submitHandler : function() {
		save();
	}
});

function save() {
//	$.ajax({
//		cache : true,
//		type : "POST",
//		url : "/softmanager/softmanager/saveSoft",
//		data : $('#signupForm').serialize(),// 你的formid
//		async : false,
//		error : function(request) {
//			parent.layer.alert("Connection error");
//		},
//		success : function(data) {
//			if (data.code == 0) {
//				parent.layer.msg("操作成功");
//				parent.reLoad();
//				var index = parent.layer.getFrameIndex(window.name); // 获取窗口索引
//				parent.layer.close(index);
//
//			} else {
//				parent.layer.alert(data.msg)
//			}
//		}
//	});
	
	var form = new FormData(document.getElementById("signupForm"));
	 $.ajax({
	     url:"/softmanager/softmanager/saveSoft",
	     type:"post",
	     data:form,
	     processData:false,
	     contentType:false,
	     success:function(data){
			if (data.code == 0) {
				parent.layer.msg("操作成功");
				parent.reLoad();
				var index = parent.layer.getFrameIndex(window.name); // 获取窗口索引
				parent.layer.close(index);

			} else {
				parent.layer.alert(data.msg)
			}
	     },
	     error:function(e){
	         alert("错误！！");
	     }
	 });        

}

function validateRule() {
	var icon = "<i class='fa fa-times-circle'></i> ";
	$("#signupForm").validate({
		rules : {
			softwareVer : {
				required : true,
				remote : {
					url : "/softmanager/softmanager/exitSoftwareVer", // 后台处理程序
					type : "post", // 数据发送方式
					dataType : "json", // 接受数据格式
					data : { // 要传递的数据
						softwareVer : function() {
							return $("#softwareVer").val();
						}
					}
				}
			}
		},
		messages : {

			softwareVer : {
				required : icon + "请输入软件版本号",
				remote : icon + "软件版本号已经存在"
			}
		}
	})
}
