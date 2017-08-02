//获取基础路径
var basePath;
$(function () {
    var location = (window.location + '').split('/');
    basePath = location[0] + '//' + location[2] + '/' + location[3];
});
var COOKIE_LANG = "hostadvisor_lang";

var lang = $.cookie(COOKIE_LANG);
if (lang == undefined) {
    lang = 'cn';
    $.cookie(COOKIE_LANG, lang);
}

//国际化配置
var resources = {
    en: {
        translation: {
            OPTIONMAP: {
                ip: "ip",
                ssCpuUser: "ssCpuUser",
                ssCpuSystem: "ssCpuSystem",
                ssCpuIdle: "ssCpuIdle",
                memUsedPer: "memUsedPer",
                memAvailReal: "memAvailReal",
                memBuffer: "memBuffer",
                memCached: "memCached",
                memTotalReal: "memTotalReal",
                dskPath: "dskPath",
                dskPercent: "dskPercent",
                dskDevice: "dskDevice",
                dskUsed: "dskUsed",
                dskAvail: "dskAvail",
                dskTotal: "dskTotal",
                dskIndex: "dskIndex",
                ifDescr: "ifDescr",
                ifInOctets: "ifInOctets",
                ifOutOctets: "ifOutOctets",
                ifInUcastPkts: "ifInUcastPkts",
                ifOutUcastPkts: "ifOutUcastPkts",
                time: "time",
                OPERATION: "more"
            },
            nav: {
                cpu: 'Cpu',
                mem: 'Memory',
                disk: 'Disk',
                network: 'Network'
            },
            more: {
                title: "More",
                from: "From",
                to: "To",
                apply: "Apply"
            }
        }
    },
    cn: {
        translation: {
            OPTIONMAP: {
                ip: "ip",
                ssCpuUser: "用户CPU百分比",
                ssCpuSystem: "系统CPU百分比",
                ssCpuIdle: "空闲CPU百分比",
                memUsedPer: "内存占用百分比",
                memAvailReal: "已使用内存",
                memBuffer: "Buffered",
                memCached: "Cached",
                memTotalReal: "总内存",
                dskPath: "磁盘路径",
                dskPercent: "磁盘使用百分比",
                dskDevice: "设备路径",
                dskUsed: "已用容量",
                dskAvail: "可用容量",
                dskTotal: "总容量",
                dskIndex: "磁盘编号",
                ifDescr: "网络接口描述",
                ifInOctets: "接口输入的字节数",
                ifOutOctets: "接口输出的字节数",
                ifInUcastPkts: "接口接收的数据包个数",
                ifOutUcastPkts: "接口发送的数据包个数",
                time: "时间",
                OPERATION: "详情"
            },
            nav: {
                cpu: 'Cpu',
                mem: '内存',
                disk: '磁盘',
                network: '网络'
            },
            more: {
                title: "详情",
                from: "从",
                to: "到",
                apply: "确认"
            }
        }
    }
};


//定时器
var TIMER;
//定时器的时间间隔
var TIMEOUT = 5000;
//控制定时器真正的运行与否，0为一直画表。1为只画一次
var TIMER_TYPE = 0;

//详情弹出框
var modalTable = $("#modalTable");


$(function () {
    TableInit_cpu(); //初始化cpu表格

    TableInit_mem(); //初始化mem表格

    //初始化disk面板里ip的select
    $.ajax({
        type: 'POST',
        contentType: "application/json",
        data: JSON.stringify({table: 'disk'}),
        url: basePath + '/getListByTable',
        success: function (mess) {
            if (mess != undefined && mess.length > 0) {

                for (var x in mess) {
                    if (mess.hasOwnProperty(x)) {
                        $("#select-diskip").append("<option>" + mess[x].ip + "</option>");
                    }
                }

                TableInit_disk();
            }
        }
    });

    //初始化if面板里ip的select
    $.ajax({
        type: 'POST',
        contentType: "application/json",
        data: JSON.stringify({table: 'network'}),
        url: basePath + '/getListByTable',
        success: function (mess) {
            if (mess != undefined && mess.length > 0) {

                for (var x in mess) {
                    if (mess.hasOwnProperty(x)) {
                        $("#select-ifip").append("<option>" + mess[x].ip + "</option>");
                    }
                }

                TableInit_if();
            }
        }
    });


    $("#select-diskip").change(function () {
        var pa = {query: queryParams_disk()};
        $('#table_disk').bootstrapTable('refresh', pa);
    });

    $("#select-ifip").change(function () {
        var pa = {query: queryParams_if()};
        $('#table_if').bootstrapTable('refresh', pa);
    });

    $("#select-keys").change(function () {
        showMore();
    });

    modalTable.on('hidden.bs.modal', function () {
        if (TIMER != undefined) {
            window.clearInterval(TIMER);
        }
    });

    modalTable.on('show.bs.modal', function () {
        TIMER_TYPE = 0;
    });


    $("#input-time-from").val(getFormatDate(new Date()));
    $("#input-time-to").val(getFormatDate(new Date()));


    $('.chart').easyPieChart({
        easing: 'easeOutBounce',
        onStep: function (from, to, percent) {
            $(this.el).find('.percent').text(Math.round(percent));
        }
    });

    $('.form_datetime').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:00',
        language: lang,
        pickerPosition: "top-right"
    });

    $("#button-apply").click(function () {
        TIMER_TYPE = 1;
        showMore();
    });

    //初始化国际化插件
    $("#select-lang ").find("option[value=" + lang + "]").attr("selected", true);
    initLang(lang);


    $("#select-lang").change(function () {
        var selectLang = $("#select-lang");
        var index = selectLang[0].selectedIndex;
        lang = selectLang[0].options[index].value;
        $.cookie(COOKIE_LANG, lang);

        initLang(lang);

        var $table = $('#table');
        $table.bootstrapTable("changeLocale", lang);
        $table.bootstrapTable("changeTitle", resources[lang].translation.OPTIONMAP);

        var $table_mem = $('#table_mem');
        $table_mem.bootstrapTable("changeLocale", lang);
        $table_mem.bootstrapTable("changeTitle", resources[lang].translation.OPTIONMAP);

        var $table_disk = $('#table_disk');
        $table_disk.bootstrapTable("changeLocale", lang);
        $table_disk.bootstrapTable("changeTitle", resources[lang].translation.OPTIONMAP);

        var $table_if = $('#table_if');
        $table_if.bootstrapTable("changeLocale", lang);
        $table_if.bootstrapTable("changeTitle", resources[lang].translation.OPTIONMAP);

    });

});

function initLang(lang) {
    i18next.init({
        lng: lang,
        resources: resources
    }, function () {
        jqueryI18next.init(i18next, $);

        $('.nav').localize();
        $('.modal').localize();

    });
}

var TableInit_cpu = function () {

    $('#table').bootstrapTable({
        locale: lang,
        url: basePath + '/getListByTable',
        method: 'post', //请求方式（*）
        queryParams: queryParams_cpu(),
        toolbar: '#toolbar', //工具按钮用哪个容器
        search: true,
        showRefresh: true,
        striped: true, //是否显示行间隔色
        pagination: true, //是否显示分页（*）
        sidePagination: "client", //分页方式：client客户端分页，server服务端分页
        pageNumber: 1, //初始化加载第一页，默认第一页
        pageSize: 25, //每页的记录行数（*）
        pageList: [25, 50, 100], //可供选择的每页的行数[25, 50, 100 ]（*）
        columns: [
            {
                filed: 'checkItem',
                checkbox: true,
                valign: 'middle',
                align: 'center'
            },
            {
                field: 'ip',
                title: 'IP',
                align: 'center',
                width: '20%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'ssCpuUser',
                title: resources[lang].translation.OPTIONMAP['ssCpuUser'],
                align: 'center',
                width: '20%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'ssCpuSystem',
                title: resources[lang].translation.OPTIONMAP['ssCpuSystem'],
                align: 'center',
                width: '20%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'ssCpuIdle',
                title: resources[lang].translation.OPTIONMAP['ssCpuIdle'],
                align: 'center',
                width: '20%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'time',
                title: resources[lang].translation.OPTIONMAP['time'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle',
                formatter: timeFormatter
            },
            {
                field: 'OPERATION',
                title: resources[lang].translation.OPTIONMAP['OPERATION'],
                align: 'center',
                valign: 'middle',
                width: '10%',
                events: operateEvents,
                formatter: operateFormatter_cpu
            }
        ],
        onLoadSuccess: function () {
        },
        onLoadError: function () {
            $("#form-search").attr("disabled", false);
        }
    });

};

var TableInit_mem = function () {

    $('#table_mem').bootstrapTable({
        locale: lang,
        url: basePath + '/getListByTable',
        method: 'post', //请求方式（*）
        queryParams: queryParams_mem(),
        toolbar: '#toolbar', //工具按钮用哪个容器
        search: true,
        showRefresh: true,
        striped: true, //是否显示行间隔色
        pagination: true, //是否显示分页（*）
        sidePagination: "client", //分页方式：client客户端分页，server服务端分页
        pageNumber: 1, //初始化加载第一页，默认第一页
        pageSize: 25, //每页的记录行数（*）
        pageList: [25, 50, 100], //可供选择的每页的行数[25, 50, 100 ]（*）
        columns: [
            {
                filed: 'checkItem',
                checkbox: true,
                valign: 'middle',
                align: 'center'
            },
            {
                field: 'ip',
                title: resources[lang].translation.OPTIONMAP['ip'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'memUsedPer',
                title: resources[lang].translation.OPTIONMAP['memUsedPer'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle',
                formatter: memUsedFormatter
            },
            {
                field: 'memAvailReal',
                title: resources[lang].translation.OPTIONMAP['memAvailReal'],
                align: 'center',
                width: '20%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'memBuffer',
                title: resources[lang].translation.OPTIONMAP['memBuffer'],
                align: 'center',
                width: '20%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'memCached',
                title: resources[lang].translation.OPTIONMAP['memCached'],
                align: 'center',
                width: '20%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'memTotalReal',
                title: resources[lang].translation.OPTIONMAP['memTotalReal'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'time',
                title: resources[lang].translation.OPTIONMAP['time'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle',
                formatter: timeFormatter
            },
            {
                field: 'OPERATION',
                title: resources[lang].translation.OPTIONMAP['OPERATION'],
                align: 'center',
                valign: 'middle',
                width: '5%',
                events: operateEvents,
                formatter: operateFormatter_mem
            }
        ],
        onLoadSuccess: function () {
        },
        onLoadError: function () {
            $("#form-search").attr("disabled", false);
        }
    });
};

var TableInit_disk = function () {

    $('#table_disk').bootstrapTable({
        locale: lang,
        url: basePath + '/getListByTableAndkeyAndIp',
        method: 'post', //请求方式（*）
        queryParams: queryParams_disk(),
        toolbar: '#toolbar', //工具按钮用哪个容器
        search: true,
        showRefresh: true,
        striped: true, //是否显示行间隔色
        pagination: true, //是否显示分页（*）
        sidePagination: "client", //分页方式：client客户端分页，server服务端分页
        pageNumber: 1, //初始化加载第一页，默认第一页
        pageSize: 25, //每页的记录行数（*）
        pageList: [25, 50, 100], //可供选择的每页的行数[25, 50, 100 ]（*）
        columns: [
            {
                filed: 'checkItem',
                checkbox: true,
                valign: 'middle',
                align: 'center'
            },
            {
                field: 'ip',
                title: resources[lang].translation.OPTIONMAP['ip'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'dskPath',
                title: resources[lang].translation.OPTIONMAP['dskPath'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'dskPercent',
                title: resources[lang].translation.OPTIONMAP['dskPercent'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'dskDevice',
                title: resources[lang].translation.OPTIONMAP['dskDevice'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'dskUsed',
                title: resources[lang].translation.OPTIONMAP['dskUsed'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'dskAvail',
                title: resources[lang].translation.OPTIONMAP['dskAvail'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'dskTotal',
                title: resources[lang].translation.OPTIONMAP['dskTotal'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'dskIndex',
                title: resources[lang].translation.OPTIONMAP['dskIndex'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'time',
                title: resources[lang].translation.OPTIONMAP['time'],
                align: 'center',
                width: '5%',
                sortable: true,
                valign: 'middle',
                formatter: timeFormatter
            },
            {
                field: 'OPERATION',
                title: resources[lang].translation.OPTIONMAP['OPERATION'],
                align: 'center',
                valign: 'middle',
                width: '5%',
                events: operateEvents,
                formatter: operateFormatter_disk
            }
        ],
        onLoadSuccess: function () {
        },
        onLoadError: function () {
            $("#form-search").attr("disabled", false);
        }
    });
};

var TableInit_if = function () {

    $('#table_if').bootstrapTable({
        locale: lang,
        url: basePath + '/getListByTableAndkeyAndIp',
        method: 'post', //请求方式（*）
        queryParams: queryParams_if(),
        toolbar: '#toolbar', //工具按钮用哪个容器
        search: true,
        showRefresh: true,
        striped: true, //是否显示行间隔色
        pagination: true, //是否显示分页（*）
        sidePagination: "client", //分页方式：client客户端分页，server服务端分页
        pageNumber: 1, //初始化加载第一页，默认第一页
        pageSize: 25, //每页的记录行数（*）
        pageList: [25, 50, 100], //可供选择的每页的行数[25, 50, 100 ]（*）
        columns: [
            {
                filed: 'checkItem',
                checkbox: true,
                valign: 'middle',
                align: 'center'
            },
            {
                field: 'ip',
                title: resources[lang].translation.OPTIONMAP['ip'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'ifDescr',
                title: resources[lang].translation.OPTIONMAP['ifDescr'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'ifInOctets',
                title: resources[lang].translation.OPTIONMAP['ifInOctets'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'ifOutOctets',
                title: resources[lang].translation.OPTIONMAP['ifOutOctets'],
                align: 'center',
                width: '10%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'ifInUcastPkts',
                title: resources[lang].translation.OPTIONMAP['ifInUcastPkts'],
                align: 'center',
                width: '15%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'ifOutUcastPkts',
                title: resources[lang].translation.OPTIONMAP['ifOutUcastPkts'],
                align: 'center',
                width: '15%',
                sortable: true,
                valign: 'middle'
            },
            {
                field: 'time',
                title: resources[lang].translation.OPTIONMAP['time'],
                align: 'center',
                width: '20%',
                sortable: true,
                valign: 'middle',
                formatter: timeFormatter
            },
            {
                field: 'OPERATION',
                title: resources[lang].translation.OPTIONMAP['OPERATION'],
                align: 'center',
                valign: 'middle',
                width: '10%',
                events: operateEvents,
                formatter: operateFormatter_if
            }
        ],
        onLoadSuccess: function () {
        },
        onLoadError: function () {
            $("#form-search").attr("disabled", false);
        }
    });
};


//获取查询的参数 cpu
function queryParams_cpu() {
    return {
        table: "cpu"
    };
}

//获取查询的参数 mem
function queryParams_mem() {
    return {
        table: "mem"
    };
}

//获取查询的参数 disk
function queryParams_disk() {
    var $select_diskip = $("#select-diskip");
    var index = $select_diskip[0].selectedIndex;
    var ip = $select_diskip[0].options[index].text;

    return {
        table: 'disk',
        keyname: 'dskPath',
        ip: ip
    };
}

//获取查询的参数 if
function queryParams_if() {
    var $select_ifip = $("#select-ifip");
    var index = $select_ifip[0].selectedIndex;
    var ip = $select_ifip[0].options[index].text;

    return {
        table: 'network',
        keyname: 'ifDescr',
        ip: ip
    };
}

function memUsedFormatter(value, row, index) {
    var memUsedPer = parseFloat(((row.memTotalReal - row.memAvailReal - row.memBuffer - row.memCached) / row.memTotalReal) * 100).toFixed(1);
    return [memUsedPer].join('');
}

function timeFormatter(value, row, index) {
    var date = new Date(value);
    var localeString = date.toLocaleString();
    return [localeString
    ].join('');
}

//详情 cpu
function operateFormatter_cpu(value, row, index) {

    return [
        '<i class="glyphicon glyphicon-signal" id="showcpu"  title="cpu详情" data-toggle="modal" data-target="#modalTable"  style="cursor:pointer"></i>'
    ].join('');
}

//详情 mem
function operateFormatter_mem(value, row, index) {
    return [
        '<i class="glyphicon glyphicon-signal" id="showmem"  title="mem详情" data-toggle="modal" data-target="#modalTable"  style="cursor:pointer"></i>'
    ].join('');
}

//详情 disk
function operateFormatter_disk(value, row, index) {
    return [
        '<i class="glyphicon glyphicon-signal" id="showdisk"  title="disk详情" data-toggle="modal" data-target="#modalTable"  style="cursor:pointer"></i>'
    ].join('');
}

//详情 if
function operateFormatter_if(value, row, index) {
    return [
        '<i class="glyphicon glyphicon-signal" id="showif"  title="if详情" data-toggle="modal" data-target="#modalTable"  style="cursor:pointer"></i>'
    ].join('');
}


function show(d) {
    //设置折线图格式
    var options = {
        xaxis: {
            mode: "time",
            tickLength: 5,
            timezone: "browser"
        },
        lines: {show: true},
        grid: {
            hoverable: true,
            clickable: false,
            tickColor: "#f9f9f9",
            borderWidth: 0,
            borderColor: "#eeeeee"
        },
        colors: ["#357ebd", "#c9302c"],
        tooltip: true,
        tooltipOpts: {
            defaultTheme: false
        }
    };

    $.plot("#cpuline", [d], options);

}

function showMore() {

    var ip = $("#lable-ip").html();
    var table = $("#input-table-hidden").val();

    var $select_keys = $("#select-keys");
    var index = $select_keys[0].selectedIndex;
    var key = $select_keys[0].options[index].value;

    var name = null;
    if (table == 'disk' || table == 'network') {
        name = $("#lable-name").html();
    }

    var from_time = "";
    var to_time = "";
    var group_time = "";

    if (TIMER_TYPE == 1) {

        var from_date = parserDate($("#input-time-from").val());
        var to_date = parserDate($("#input-time-to").val());
        var from_date_time = Date.parse(from_date);
        var to_date_time = Date.parse(to_date);
        var now_date_time = Date.parse(new Date());

        if (from_date_time >= to_date_time) {
            return;
        }

        var from_dvalue = parseInt((now_date_time - from_date_time) / 1000 / 60);
        var to_dvalue = parseInt((now_date_time - to_date_time) / 1000 / 60);

        var group_dvalue = parseInt((to_date_time - from_date_time) / 1000 / 60);

        if (from_dvalue < 1) {
            from_time = "1m";
        } else {
            from_time = from_dvalue + "m";
        }

        if (to_dvalue < 1) {
            to_time = "1m";
        } else {
            to_time = to_dvalue + "m";
        }

        if (group_dvalue < 1) {
            group_time = "30s";
        } else {
            var group_time_dvalue = parseInt(group_dvalue / 60);
            if (group_time_dvalue < 1) {
                group_time = "1m"
            } else {
                group_time = group_time_dvalue + "m";
            }
        }

        console.log(from_time);
        console.log(to_time);
        console.log(group_time);
    }


    var data = {
        ip: ip,
        key: key,
        table: table,
        limit: "60",
        name: name,
        timer_type: TIMER_TYPE,
        from_time: from_time,
        to_time: to_time,
        group_time: group_time
    };

    $.ajax({
        type: 'POST',
        contentType: "application/json",
        data: JSON.stringify(data),
        url: basePath + '/getMore',
        success: function (mess) {
            if (mess != undefined && mess.length > 0) {

                if (table == 'cpu') {
                    var ssCpuUser = mess[0].ssCpuUser;
                    $(".chart").data('easyPieChart').update(parseFloat(ssCpuUser));
                } else if (table == 'mem') {
                    var memTotalReal = parseFloat(mess[0].memTotalReal);
                    var memAvailReal = parseFloat(mess[0].memAvailReal);
                    var memBuffer = parseFloat(mess[0].memBuffer);
                    var memCached = parseFloat(mess[0].memCached);
                    var percent = parseFloat((((memTotalReal - memAvailReal ) - memBuffer - memCached ) / memTotalReal ) * 100);
                    $(".chart").data('easyPieChart').update(percent);
                } else if (table == 'disk') {
                    var dskPercent = mess[0].dskPercent;
                    $(".chart").data('easyPieChart').update(parseFloat(dskPercent));
                }

                if (key != undefined) {
                    var d = [];
                    for (var x in mess) {
                        if (mess.hasOwnProperty(x)) {
                            var value = mess[x][key];
                            var time = new Date(mess[x].time).getTime();
                            var d_temp = [];
                            d_temp.push(time);
                            d_temp.push(value);
                            d.push(d_temp);
                        }
                    }

                    setTimeout(function () {
                        show(d);
                    }, 500);
                }
            }

            //如果是只画一次，就停止
            if (TIMER_TYPE == 1) {
                if (TIMER != undefined) {
                    window.clearInterval(TIMER);
                }
            }


        },
        error: function () {
            //如果是只画一次，就停止
            if (TIMER_TYPE == 1) {
                if (TIMER != undefined) {
                    window.clearInterval(TIMER);
                }
            }
        }
    });
}

function showKeyList() {

    var ip = $("#lable-ip").html();
    var table = $("#input-table-hidden").val();

    var data = {ip: ip, table: table};
    $.ajax({
        type: 'POST',
        contentType: "application/json",
        data: JSON.stringify(data),
        url: basePath + '/getKeyList',
        success: function (mess) {
            if (mess != undefined && mess.length > 0) {

                var $select_keys = $("#select-keys");
                $select_keys.html("");

                for (var x in mess[0]) {
                    if (mess[0].hasOwnProperty(x)) {
                        if (x != "time" && x != "ip" && x != "dskDevice" && x != "dskPath" && x != "ifDescr") {
                            $select_keys.append('<option value ="' + x + '">' + resources[lang].translation.OPTIONMAP[x] + '</option>');
                        }
                    }
                }

                showMore();

                if (TIMER != undefined) {
                    window.clearInterval(TIMER);
                }

                TIMER = window.setInterval('showMore()', TIMEOUT);

            }
        }
    });
}


window.operateEvents = {
    //查看cpu详情
    'click #showcpu': function (e, value, row, index) {
        var ip = row.ip;
        var table = 'cpu';

        $("#pie-chart-div").show();

        $("#select-keys").html("");

        $("#lable-ip").html(ip);
        $("#lable-table").html(resources[lang].translation.nav[table]);
        $("#input-table-hidden").val(table);

        $("#pie-chart-footer").html("cpu使用率");

        $("#lable-name-div").hide();

        showKeyList();
    },
    //查看mem详情
    'click #showmem': function (e, value, row, index) {
        var ip = row.ip;
        var table = 'mem';

        $("#pie-chart-div").show();

        $("#select-keys").html("");

        $("#lable-ip").html(ip);
        $("#lable-table").html(resources[lang].translation.nav[table]);
        $("#input-table-hidden").val(table);
        $("#pie-chart-footer").html("mem使用率");

        $("#lable-name-div").hide();

        showKeyList();

    },
    'click #showdisk': function (e, value, row, index) {
        var ip = row.ip;
        var dskPath = row.dskPath;
        var table = 'disk';

        $("#pie-chart-div").show();

        $("#select-keys").html("");

        $("#lable-ip").html(ip);
        $("#lable-table").html(resources[lang].translation.nav[table]);
        $("#input-table-hidden").val(table);
        $("#pie-chart-footer").html("disk使用率");

        $("#lable-name-div").show();
        $("#lable-name").html(dskPath);

        showKeyList();

    },
    'click #showif': function (e, value, row, index) {
        var ip = row.ip;
        var ifDescr = row.ifDescr;
        var table = 'network';

        $("#pie-chart-div").hide();

        $("#select-keys").html("");

        $("#lable-ip").html(ip);
        $("#lable-table").html(resources[lang].translation.nav[table]);
        $("#input-table-hidden").val(table);
        $("#pie-chart-footer").html("if使用率");

        $("#lable-name-div").show();
        $("#lable-name").html(ifDescr);

        showKeyList();

    }
};

function getFormatDate(date) {
    var seperator1 = "-";
    var seperator2 = ":";
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var hour = date.getHours();
    if (hour >= 0 && hour <= 9) {
        hour = "0" + hour;
    }
    var minute = date.getMinutes();
    if (minute >= 0 && minute <= 9) {
        minute = "0" + minute;
    }

    return date.getFullYear() + seperator1 + month + seperator1 + strDate
        + " " + hour + seperator2 + minute + seperator2 + "00";
}

var parserDate = function (date) {
    var t = Date.parse(date);
    if (!isNaN(t)) {
        return new Date(Date.parse(date.replace(/-/g, "/")));
    } else {
        return new Date();
    }
};

