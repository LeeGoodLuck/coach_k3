var prefix = "/coach/subAgent";
$(function () {
    load();
});

function load() {
    $('#exampleTable')
        .bootstrapTable(
            {
                method: 'get', // 服务器数据的请求方式 get or post
                url: prefix + "/details", // 服务器数据的加载地址
                // showRefresh : true,
                // showToggle : true,
                // showColumns : true,
                iconSize: 'outline',
                toolbar: '#exampleToolbar',
                striped: true, // 设置为true会有隔行变色效果
                dataType: "json", // 服务器返回的数据类型
                pagination: true, // 设置为true会在底部显示分页条
                // queryParamsType : "limit",
                // //设置为limit则会发送符合RESTFull格式的参数
                singleSelect: false, // 设置为true将禁止多选
                // contentType : "application/x-www-form-urlencoded",
                // //发送到服务器的数据编码类型
                pageSize: 10, // 如果设置了分页，每页数据条数
                pageNumber: 1, // 如果设置了分布，首页页码
                // search : true, // 是否显示搜索框
                showColumns: false, // 是否显示内容下拉框（选择显示的列）
                sidePagination: "server", // 设置在哪里进行分页，可选值为"client" 或者
                // "server"
                queryParams: function (params) {
                    return {
                        // 说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
                        limit: params.limit,
                        offset: params.offset,
                        coachName: $('#searchName').val(),
                        tel: $('#searchTel').val(),
                        agentId: $('#agentId').val()

                    };
                },
                columns: [
                    {
                        field: 'coachId',
                        title: 'id',
                        align: 'center',
                        valign: 'center',
                    },
                    {
                        field: 'coachName',
                        title: '教练名称',
                        valign: 'center',
                    },
                    {
                        field: 'tel',
                        title: '联系电话',
                        valign: 'center',
                    },
                    // {
                    //     field: 'account',
                    //     title: '微信号',
                    //     valign: 'center',
                    // },
                    {
                        field: 'divide',
                        title: '分销比例',
                        align: 'center',
                        valign: 'center',
                        formatter: function (value, row, index) {
                            return value + '%';
                        }
                    },
                    {
                        field: 'status',
                        title: '状态',
                        align: 'center',
                        valign: 'center',
                        formatter: function (value, row, index) {
                            if (row.status == 0) {
                                return '<span >禁用</span>'; //class="label label-danger"
                            } else if (row.status == 1) {
                                return '<span >正常</span>';  //class="label label-primary"
                            }
                        }
                    },
                    {
                        field: 'countDevice',
                        title: '设备统计',
                        valign: 'center',
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
                    }
                    ]
            });
}

function reLoad() {
    $('#exampleTable').bootstrapTable('refresh');
}

