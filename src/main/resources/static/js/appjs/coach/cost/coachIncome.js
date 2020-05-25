var prefix = "/coach/cost";

$(function () {
    load();
});

function load() {
    $('#exampleTable')
        .bootstrapTable(
            {
                method: 'get', // 服务器数据的请求方式 get or post
                url: prefix + "/coachList", // 服务器数据的加载地址
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
                showFooter:true,
                // "server"
                queryParams: function (params) {
                    return {
                        // 说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
                        limit: params.limit,
                        offset: params.offset,
                        endDate: $('#endTime').val(),
                        starDate: $('#startTime').val(),
                        coachName: $('#coachName').val()
                    };
                },


                // //请求服务器数据时，你可以通过重写参数的方式添加一些额外的参数，例如 toolbar 中的参数 如果
                // queryParamsType = 'limit' ,返回参数必须包含
                // limit, offset, search, sort, order 否则, 需要包含:
                // pageSize, pageNumber, searchText, sortName,
                // sortOrder.
                // 返回false将会终止请求
                columns: [
                    {
                        field: 'coachId',
                        title: 'id',
                        align: 'center',
                        valign: 'center',
                    },
                    {
                        field: 'account',
                        title: '账号',
                        align: 'center',
                        valign: 'center',
                    },
                    {
                        field: 'coachName',
                        title: '教练名称',
                        align: 'center',
                        valign: 'center',
                    },
                    {
                        field: 'tel',
                        title: '联系电话',
                        align: 'center',
                        valign: 'center',
                    },
                    {
                        field: 'divide',
                        title: '分销比例',
                        align: 'center',
                        valign: 'center',
                        formatter : function(value, row, index) {
                            return value + '%';
                        }
                    },
                    {
                        field: 'incomeAmount',
                        title: '收入金额',
                        align: 'center',
                        valign: 'center',
                    },
                    {
                        title : '操作',
                        field : 'coachId',
                        align : 'center',
                        valign : 'center',
                        formatter : function(value, row, index) {
                            var e = '<a class="btn btn-primary btn-sm ' + s_edit_h + '" href="#" mce_href="#" title="详情" onclick="check(\''
                                + row.coachId
                                + '\')"><i class="fa fa-check"></i></a> ';
                            return e ;
                        }
                    }
                ]
            });
}

function check(id) {
    var startTime = $('#startTime').val();
    var endTime = $('#endTime').val();
    var url =  '/agentCheck/5/' + id;
    if(startTime != null && startTime != '' && endTime != null && endTime != ''){
        url = url +  "/"+startTime+ "/"+endTime;
    }
    layer.open({
        type : 2,
        title : '查看收入详情',
        maxmin : true,
        shadeClose : true, // 点击遮罩关闭层
        area : [ '800px', '90%' ],
        content : prefix + url // iframe的url
    });
}

function reLoad() {
    var startTime = $('#startTime').val();
    var endTime = $('#endTime').val();
    if(startTime == "" && endTime != ''){
        alert("请输入开始时间");
        return;
    }
    if(startTime != '' && endTime == ""){
        alert("请输入结束时间");
        return;
    }
    $('#exampleTable').bootstrapTable('refresh');

}
