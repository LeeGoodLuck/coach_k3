var prefix = "/coach/agent";
$(function() {
	load();
});

function load() {
	$('#exampleTable')
			.bootstrapTable(
					{
						method : 'get', // 服务器数据的请求方式 get or post
						url : prefix + "/agent3List", // 服务器数据的加载地址
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
		                        agentName: $('#searchName').val(),
                                tel: $('#tel').val()
		                    };
						},
                        onLoadSuccess: function () {  //加载成功时执行
                            $("#total").html($('#exampleTable').bootstrapTable('getOptions').totalRows)
                        },
		                
						// //请求服务器数据时，你可以通过重写参数的方式添加一些额外的参数，例如 toolbar 中的参数 如果
						// queryParamsType = 'limit' ,返回参数必须包含
						// limit, offset, search, sort, order 否则, 需要包含:
						// pageSize, pageNumber, searchText, sortName,
						// sortOrder.
						// 返回false将会终止请求
						columns : [
							{
								field : 'agentId',
								title : 'id',
								align : 'center',
		                        valign : 'center',
							},
							{
								field : 'agentName',
								title : '代理商名称',
		                        valign : 'center',
							},
							{
								field : 'contacts',
								title : '联系人',
								valign : 'center',
							},
							{
								field : 'tel',
								title : '联系电话',
								valign : 'center',
							},
							{
								field : 'account',
								title : '微信号',
								valign : 'center',
							},
							{
								field : 'divide',
								title : '分销比例',
								align : 'center',
		                        valign : 'center',
								formatter : function(value, row, index) {
									return value + '%';
								}
							},
							{
								field : 'higherAgentName',
								title : '二级代理商',
		                        align : 'center',
		                        valign : 'center',
							},
                            {
                                field : 'agentThreeProfit',
                                title : '总收益',
                                align : 'center',
                                valign : 'center',
                            },
							{
								field : 'remarks',
								title : '备注',
								align : 'center',
								valign : 'center',
							},
							{
								field : 'status',
								title : '状态',
								align : 'center',
		                        valign : 'center',
								formatter : function(value, row, index) {
									if (row.status == 0) {
										return '<span class="label label-danger">禁用</span>';
									} else if (row.status == 1) {
										return '<span class="label label-primary">正常</span>';
									}
								}
							},
							{
								field : 'isDelete',
								title : '是否离职',
								align : 'center',
		                        valign : 'center',
								formatter : function(value, row, index) {
									if (row.isDelete == 1) {
										return '<span class="label label-danger">是</span>';
									} else if (row.isDelete == 0) {
										return '<span class="label label-primary">否</span>';
									}
								}
							},
							{
								title : '操作',
								field : 'agentId',
								align : 'center',
		                        valign : 'center',
								formatter : function(value, row, index) {
									var f = '';
									var e = '';
//									var d = '<a class="btn btn-warning btn-sm ' + s_remove_h + '" href="#" title="删除"  mce_href="#" onclick="remove(\''
//										+ row.agentId
//										+ '\')"><i class="fa fa-remove"></i></a> ';
                                    var d = '';
									var a = '';
									if (row.isDelete == 0) {
										e = '<a class="btn btn-primary btn-sm ' + s_edit_h + '" href="#" mce_href="#" title="编辑" onclick="edit(\''
											+ row.agentId
											+ '\')"><i class="fa fa-edit"></i></a> ';
                                        d = '<a class="btn btn-warning btn-sm ' + s_details_h + '" href="#" mce_href="#" title="详情" onclick="details(\''
                                            + row.agentId
                                            + '\')"><i class="fa fa-clone"></i></a> ';
										a = '<a class="btn btn-warning btn-sm ' + s_quit_h + '" href="#" title="离职"  mce_href="#" onclick="quit(\''
											+ row.agentId
											+ '\')">离职</a> ';
										f = '<a class="btn btn-warning btn-sm ' + s_change_h + '" href="#" title="更换"  mce_href="#" onclick="change(\''
											+ row.agentId
											+ '\')">更换</a> ';
									}
									return e + d + a + f;
								}
							} ]
					});
}
function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}
function details(id) {
    //查询代理名下的教练
    layer.open({
        type: 2,
        title: '代理商名下的教练',
        maxmin: true,
        shadeClose: true, // 点击遮罩关闭层
        area: ['800px', '90%'],
        content: '/coach/subAgent/subDetails/' + id // iframe的url
    });
}
function add() {
	// iframe层
	layer.open({
		type : 2,
		title : '添加三级代理商',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '800px', '90%' ],
		content : prefix + '/agent3Add' // iframe的url
	});
}
function remove(id) {
	layer.confirm('确定要删除选中的记录？', {
		btn : [ '确定', '取消' ]
	}, function() {
		$.ajax({
			url : prefix + "/removeAgent3",
			type : "post",
			data : {
				'agentId' : id
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
function quit(id) {
	layer.confirm('确定要离职？', {
		btn : [ '确定', '取消' ]
	}, function() {
		$.ajax({
			url : prefix + "/quitAgent3",
			type : "post",
			data : {
				'agentId' : id
			},
			success : function(r) {
				if (r.code === 0) {
					layer.msg("离职成功");
					reLoad();
				} else {
					layer.msg(r.msg);
				}
			}
		});
	})
	
}
function edit(id) {
	layer.open({
		type : 2,
		title : '修改三级代理商信息',
		maxmin : true,
		shadeClose : true, // 点击遮罩关闭层
		area : [ '800px', '90%' ],
		content : prefix + '/agent3Edit/' + id // iframe的url
	});
}


function change(id) {
	layer.open({
		type : 2,
		title : '更换三级代理商信息',
		maxmin : true,
		shadeClose : true, // 点击遮罩关闭层
		area : [ '800px', '90%' ],
		content : prefix + '/agent3Change/' + id // iframe的url
	});
}