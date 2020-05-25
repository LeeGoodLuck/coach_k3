var prefix = "/coach/coach";
$(function() {
	load();
});

function load() {
	$('#exampleTable')
			.bootstrapTable(
					{
						method : 'get', // 服务器数据的请求方式 get or post
						url : prefix + "/coachList", // 服务器数据的加载地址
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
                                tel: $('#searchTel').val(),
		                        coachName: $('#searchName').val()
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
								field : 'coachId',
								title : 'id',
								align : 'center',
		                        valign : 'center',
							},
							{
								field : 'coachName',
								title : '名称',
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
                                field : 'divideType',
                                title : '分销类型',
                                valign : 'center',
                                formatter : function(value, row, index) {
                                    if (row.divideType == 0) {
                                        return '分销比例';
                                    } else if (row.divideType == 1) {
                                        return '固定金额';
                                    }
                                }
                            },
							{
								field : 'divide',
								title : '分销',
								align : 'center',
		                        valign : 'center',
								formatter : function(value, row, index) {
									if(row.divideType==0){
                                        return value + '%';
                                    }else{
                                        return value ;
									}
								}
							},
							{
								field : 'agentName',
								title : '代理商',
		                        align : 'center',
		                        valign : 'center',
							},
                            {
                                field : 'wechatName',
                                title : '微信名',
                                align : 'center',
                                valign : 'center',
                            },
                            {
                                field : 'countDevice',
                                title : '设备统计',
                                valign : 'center',
                            },
                            {
                                field : 'dayRevenue',
                                title : '今日收益',
                                valign : 'center',
                            },
                            {
                                field : 'sevenRevenue',
                                title : '七天收益',
                                valign : 'center',
                            },
                            {
                                field : 'monthRevenue',
                                title : '本月收益',
                                valign : 'center',
                            },
                            {
                                field : 'totalRevenue',
                                title : '总收益',
                                valign : 'center',
                            },
							// {
							// 	field : 'remarks',
							// 	title : '备注',
		                    //     align : 'center',
		                    //     valign : 'center',
							// },
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
								title : '操作',
								field : 'coachId',
								align : 'center',
		                        valign : 'center',
								formatter : function(value, row, index) {
                                    var f = '<a class="btn btn-primary btn-sm ' + s_remove_h + '" href="#" title="解绑"  mce_href="#" onclick="untying(\''
                                        + row.coachId
                                        + '\')"><i class="fa fa-edit"></i></a> ';
									var e = '<a class="btn btn-primary btn-sm ' + s_edit_h + '" href="#" mce_href="#" title="编辑" onclick="edit(\''
										+ row.coachId
										+ '\')"><i class="fa fa-edit"></i></a> ';
									var d = '<a class="btn btn-warning btn-sm ' + s_remove_h + '" href="#" title="删除"  mce_href="#" onclick="remove(\''
										+ row.coachId
										+ '\')"><i class="fa fa-remove"></i></a> ';
									return e + d + f;
								}
							} ]
					});
}
function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}
$("#excel").click(function () {
    window.location.href = prefix + "/coachExcel";
})
function add() {
	// iframe层
	layer.open({
		type : 2,
		title : '添加教练',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '800px', '90%' ],
		content : prefix + '/coachAdd' // iframe的url
	});
}
function untying(id) {
    layer.confirm('确定要删除选中的绑定信息？', {
        btn : [ '确定', '取消' ]
    }, function() {
        $.ajax({
            url : prefix + "/untyingCoach",
            type : "post",
            data : {
                'coachId' : id
            },
            success : function(r) {
                if (r.code === 0) {
                    layer.msg("解绑成功");
                    reLoad();
                } else {
                    layer.msg(r.msg);
                }
            }
        });
    })

}
function remove(id) {
	layer.confirm('确定要删除选中的记录？', {
		btn : [ '确定', '取消' ]
	}, function() {
		$.ajax({
			url : prefix + "/removeCoach",
			type : "post",
			data : {
				'coachId' : id
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
function edit(id) {
	layer.open({
		type : 2,
		title : '修改教练信息',
		maxmin : true,
		shadeClose : true, // 点击遮罩关闭层
		area : [ '800px', '90%' ],
		content : prefix + '/coachEdit/' + id // iframe的url
	});
}
