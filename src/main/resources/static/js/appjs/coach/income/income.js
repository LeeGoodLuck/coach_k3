var prefix = "/coach/income";
$(function() {
	load();
	datepicker();
});

function load() {
	$('#exampleTable')
			.bootstrapTable(
					{
						method : 'get', // 服务器数据的请求方式 get or post
						url : prefix + "/incomeList", // 服务器数据的加载地址
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
		                        starDate: $('#starDate').val(),
		                        endDate: $('#endDate').val()
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
								field : 'id',
								title : 'id',
								align : 'center',
		                        valign : 'center',
							},
							{
								field : 'createTime',
								title : '时间',
								valign : 'center',
							},
							{
								field : 'plateNumber',
								title : '车牌',
								valign : 'center',
							},
							{
								field : 'testDurationStr',
								title : '时长（分）',
								valign : 'center',
							},
							{
								field : 'money',
								title : '收益（元）',
								valign : 'center',
							},
							{
								field : 'separateStatus',
								title : '到账状态',
								valign : 'center',
								formatter : function(value, row, index) {
									if (row.separateStatus == 0) {
										return '<span class="label label-primary">未处理</span>';
									} else if (row.separateStatus == 1) {
										return '<span class="label label-primary">成功</span>';
									} else if (row.separateStatus == 2) {
										return '<span class="label label-primary">失败</span>';
									} else if (row.separateStatus == 3) {
										return '<span class="label label-primary">处理中</span>';
									}
								}
							},
							{
								field : 'agentName',
								title : '代理商',
								valign : 'center',
							}]
					});
}
function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}

function datepicker() {
    $('#starDate').datepicker({
        format: 'yyyy-mm-dd',
        locale: moment.locale('zh-cn')
    }).on('dp.change', function (e) {
        $('#endDate').data('DateTimePicker').minDate(e.date);
    });;
    $('#endDate').datepicker({
        format: 'yyyy-mm-dd',
        locale: moment.locale('zh-cn')
    }).on('dp.change', function (e) {
        $('#starDate').data('DateTimePicker').maxDate(e.date);
    });
}
