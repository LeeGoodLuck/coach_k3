var prefix = "/withdraw/withdraw";
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
		                        applyType: $('#searchName').val()
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
								field : 'applyId',
								title : 'id',
								align : 'center',
		                        valign : 'center',
							},
							{
								field : 'applyType',
								title : '提现人类型',
								align : 'center',
		                        valign : 'center',
								formatter : function(value, row, index) {
									if (row.applyType == 0) {
										return '教练';
									} else if (row.applyType == 1) {
										return '代理商';
									} else if (row.applyType == 2) {
										return '顶级代理商';
									}
								}
							},
							{
								field : 'account',
								title : '账号',
								valign : 'left',
							},
							{
								field : 'name',
								title : '名称',
								valign : 'center',
							},
							{
								field : 'amountApply',
								title : '申请金额',
								valign : 'center',
							},
							{
								field : 'applyTime',
								title : '申请时间',
								valign : 'center',
							},
							{
								field : 'disposeState',
								title : '状态',
								align : 'center',
		                        valign : 'center',
								formatter : function(value, row, index) {
									if (row.disposeState == 0) {
										return '申请中';
									} else if (row.disposeState == 1) {
										return '提现成功';
									} else if (row.disposeState == 2) {
										return '提现失败';
									}
								}
							},
							{
								field : 'checkTime',
								title : '审核时间',
								valign : 'center',
							},
							{
								field : 'remarks',
								title : '备注',
								valign : 'center',
							},
							{
								title : '操作',
								field : 'applyId',
								align : 'center',
		                        valign : 'center',
								formatter : function(value, row, index) {
									if (row.disposeState == 0) {
										var e = '<a class="btn btn-primary btn-sm ' + s_edit_h + '" href="#" mce_href="#" title="编辑" onclick="edit(\''
										+ row.applyId
										+ '\')">处理</a> ';
										return e;
									}
									return '';
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
		title : '审核提现',
		maxmin : true,
		shadeClose : true, // 点击遮罩关闭层
		area : [ '800px', '90%' ],
		content : prefix + '/edit/' + id // iframe的url
	});
}
