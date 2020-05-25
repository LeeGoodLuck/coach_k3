var prefix = "/coach/device";
$(function () {
    load();
});

function load() {
    $('#exampleTable')
        .bootstrapTable(
            {
                method: 'get', // 服务器数据的请求方式 get or post
                url: prefix + "/deviceListRanking", // 服务器数据的加载地址
                iconSize: 'outline',
                toolbar: '#exampleToolbar',
                striped: true, // 设置为true会有隔行变色效果
                dataType: "json", // 服务器返回的数据类型
                pagination: false, // 设置为true会在底部显示分页条
                singleSelect: false, // 设置为true将禁止多选
                pageSize: 10, // 如果设置了分页，每页数据条数
                pageNumber: 1, // 如果设置了分布，首页页码
                // search : true, // 是否显示搜索框
                showColumns: false, // 是否显示内容下拉框（选择显示的列）
                sidePagination: "server", // 设置在哪里进行分页，可选值为"client" 或者
                queryParams : function(params) {
                    return {
                        typeId: $('#typeId').val()
                    };
                },
                columns: [
                    {
                        field: 'deviceId',
                        title: '设备ID',
                        align: 'center',
                        valign: 'center',
                    },
                    {
                        field: 'deviceNo',
                        title: '设备编号',
                        valign: 'center',
                    },
                    {
                        field: 'lastLoginTime',
                        title: '最后登录时间',
                        valign: 'center',
                    },
                    {
                        field: 'totalTestIncome',
                        title: '总收益',
                        valign: 'center',
                    }]
            });
}


