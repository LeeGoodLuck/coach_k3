// 以下为官方示例
$().ready(function() {
	validateRule();
	// $("#signupForm").validate();
});

$.validator.setDefaults({
	submitHandler : function() {
		update();
	}
});
function update() {
//	$.ajax({
//		cache : true,
//		type : "POST",
//		url : "/softmanager/softmanager/updateSoft",
//		data : $('#signupForm').serialize(),// 你的formid
//		async : false,
//		error : function(request) {
//			alert("Connection error");
//		},
//		success : function(data) {
//			if (data.code == 0) {
//				parent.layer.msg(data.msg);
//				parent.reLoad();
//				var index = parent.layer.getFrameIndex(window.name); // 获取窗口索引
//				parent.layer.close(index);
//
//			} else {
//				parent.layer.msg(data.msg);
//			}
//
//		}
//	});
	
	var form = new FormData(document.getElementById("signupForm"));
	 $.ajax({
	     url:"/softmanager/softmanager/updateSoft",
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
				required : true
			}
		},
		messages : {

			softwareVer : {
				required : icon + "请输入软件版本号"
			}
		}
	})
}
