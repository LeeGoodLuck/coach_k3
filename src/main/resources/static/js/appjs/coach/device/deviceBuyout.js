var prefix = "/coach/device";
$(function () {
    load();
});

function load() {
    $('#exampleTable')
        .bootstrapTable(
            {
                method: 'get', // 服务器数据的请求方式 get or post
                url: prefix + "/deviceList", // 服务器数据的加载地址
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
                        deviceNo: $('#searchName').val(),
                        remarks: $('#remarks').val(),
                        deviceTypeId: $('#deviceTypeId_').val(),
                        sellTypeId: 2
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
                columns: [
                    {
                        field: 'deviceId',
                        title: 'id',
                        align: 'center',
                        valign: 'center',
                    },
                    {
                        field: 'deviceNo',
                        title: '设备id',
                        valign: 'center',
                    },
                    {
                        field: 'plateNumber',
                        title: '绑定车牌',
                        valign: 'center',
                    },
                    {
                        field: 'createTime',
                        title: '激活时间',
                        valign: 'center',
                    },
                    {
                        field: 'sellTypeName',
                        title: '销售类型',
                        valign: 'center',
                    },
                    {
                        field: 'coachName',
                        title: '所属设备',
                        valign: 'center',
                    },
                    {
                        field: 'deviceTypeName',
                        title: '设备类型',
                        valign: 'center',
                    },
                    {
                        field: 'freeTime',
                        title: '调试时间',
                        valign: 'center',
                    },
                    {
                        field: 'totalTestTime',
                        title: '考试时长',
                        valign: 'center',
                    },
                    {
                        field: 'totalTestIncome',
                        title: '考试收入',
                        valign: 'center',
                    },
                    {
                        field: 'totalTestPay',
                        title: '已收金额',
                        valign: 'center',
                    },
                    {
                        field: 'lastLoginTime',
                        title: '最后登录时间',
                        valign: 'center',
                    },
                    {
                        field: 'chargeTypeName',
                        title: '收费模式',
                        valign: 'center',
                    },
                    {
                        field: 'deposit',
                        title: '押金',
                        valign: 'center',
                    },
                    {
                        field: 'remarks',
                        title: '备注',
                        valign: 'center',
                    },
                    {
                        field: 'status',
                        title: '状态',
                        align: 'center',
                        valign: 'center',
                        formatter: function (value, row, index) {
                            if (row.status == 0) {
                                return '<span class="label label-danger">禁用</span>';
                            } else if (row.status == 1) {
                                return '<span class="label label-primary">正常</span>';
                            }
                        }
                    },
                    {
                        title: '操作',
                        field: 'deviceId',
                        align: 'center',
                        valign: 'center',
                        formatter: function (value, row, index) {
                            var e = '<a class="btn btn-primary btn-sm ' + s_edit_h + '" href="#" mce_href="#" title="编辑" onclick="edit(\''
                                + row.deviceId
                                + '\')"><i class="fa fa-edit"></i></a> ';
                            var d = '<a class="btn btn-warning btn-sm ' + s_remove_h + '" href="#" title="删除"  mce_href="#" onclick="remove(\''
                                + row.deviceId
                                + '\')"><i class="fa fa-remove"></i></a> ';
                            return e + d;
                        }
                    }]
            });
}

function reLoad() {
    $('#exampleTable').bootstrapTable('refresh');
}

function add() {
    // iframe层
    layer.open({
        type: 2,
        title: '添加设备',
        maxmin: true,
        shadeClose: false, // 点击遮罩关闭层
        area: ['800px', '90%'],
        content: prefix + '/deviceAdd' // iframe的url
    });
}

function remove(id) {
    layer.confirm('确定要删除选中的记录？', {
        btn: ['确定', '取消']
    }, function () {
        $.ajax({
            url: prefix + "/removeDevice",
            type: "post",
            data: {
                'deviceId': id
            },
            success: function (r) {
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
        type: 2,
        title: '修改设备信息',
        maxmin: true,
        shadeClose: true, // 点击遮罩关闭层
        area: ['800px', '90%'],
        content: prefix + '/deviceEdit/' + id // iframe的url
    });
}
