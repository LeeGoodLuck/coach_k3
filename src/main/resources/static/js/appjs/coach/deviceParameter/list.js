var prefix = "/coach/deviceParameter";
$(function() {
	load();
});

function load() {
	$('#exampleTable')
			.bootstrapTable(
					{
						method : 'get', // 服务器数据的请求方式 get or post
						url : prefix + "/dataList", // 服务器数据的加载地址
						// showRefresh : true,
						// showToggle : true,
						// showColumns : true,
						iconSize : 'outline',
						toolbar : '#exampleToolbar',
						striped : true, // 设置为true会有隔行变色效果
						dataType : "json", // 服务器返回的数据类型
						pagination : true, // 设置为true会在底部显示分页条
						// queryParamsType : "limit",
						// //设置为limit则会发送符合RESTFull格式的参数
						singleSelect : false, // 设置为true将禁止多选
						// contentType : "application/x-www-form-urlencoded",
						// //发送到服务器的数据编码类型
						pageSize : 10, // 如果设置了分页，每页数据条数
						pageNumber : 1, // 如果设置了分布，首页页码
						// search : true, // 是否显示搜索框
						showColumns : false, // 是否显示内容下拉框（选择显示的列）
						sidePagination : "server", // 设置在哪里进行分页，可选值为"client" 或者
						// "server"
						queryParams : function(params) {
							return {
		                        // 说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
		                        limit: params.limit,
		                        offset: params.offset,
		                        room: $('#room').val(),
		                        province: $('#province').val(),
		                        city: $('#city').val()
		                    };
						},
		                
		                
						// //请求服务器数据时，你可以通过重写参数的方式添加一些额外的参数，例如 toolbar 中的参数 如果
						// queryParamsType = 'limit' ,返回参数必须包含
						// limit, offset, search, sort, order 否则, 需要包含:
						// pageSize, pageNumber, searchText, sortName,
						// sortOrder.
						// 返回false将会终止请求
						columns : [
							{
								field : 'parameterId',
								title : 'id',
								align : 'center',
		                        valign : 'center',
							},
							{
								field : 'province',
								title : '省',
								valign : 'center',
							},
							{
								field : 'city',
								title : '市',
								valign : 'center',
							},
							{
								field : 'room',
								title : '所属考场',
		                        valign : 'center',
							},
							{
								field : 'parameterType',
								title : '参数类型',
								align : 'center',
		                        valign : 'center',
								formatter : function(value, row, index) {
									if (row.parameterType == 0) {
										return '参数';
									} else if (row.parameterType == 1) {
										return '指令';
									} else if(row.parameterType == 2){
										return '线路';
									} else if(row.parameterType == 3){
										return '灯光';
									}
								}
							},
							{
								field : 'createTime',
								title : '上传时间',
		                        valign : 'center',
							},
							{
								field : 'usable',
								title : '可下载标志',
								align : 'center',
								valign : 'center',
								formatter : function(value, row, index) {
									if (row.usable == 0) {
										return '<span class="label label-danger">不可下载</span>';
									} else if (row.usable == 1) {
										return '<span class="label label-primary">可以下载</span>';
									}
								}
							},
							{
								title : '操作',
								field : 'parameterId',
								align : 'center',
		                        valign : 'center',
								formatter : function(value, row, index) {
									var e = '<a class="btn btn-primary btn-sm ' + s_edit_h + '" href="#" mce_href="#" title="修改" onclick="edit(\''
										+ row.parameterId
										+ '\')">修改</a> ';
									var d = '<a class="btn btn-danger btn-sm ' + s_delete_h + '" href="#" mce_href="#" title="删除" onclick="remove(\''
										+ row.parameterId
										+ '\')">删除</a> ';
									return e + d;
								}
							} ]
					});
}
function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}
function edit(id) {
	layer.open({
		type : 2,
		title : '审核',
		maxmin : true,
		shadeClose : true, // 点击遮罩关闭层
		area : [ '800px', '90%' ],
		content : prefix + '/edit/' + id // iframe的url
	});
}
function remove(id) {
	layer.confirm('确定要删除选中的记录？', {
		btn : [ '确定', '取消' ]
	}, function() {
		$.ajax({
			url : prefix + "/delete",
			type : "post",
			data : {
				'parameterId' : id
			},
			success : function(r) {
				if (r.code === 0) {
					layer.msg("删除成功");
					reLoad();
				} else {
					layer.msg(r.msg);
				}
			}
		});
	})

}