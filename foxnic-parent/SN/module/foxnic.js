layui.define(['settings', 'layer', 'admin', 'form', 'table', 'util', 'upload', "xmSelect", "element"], function (exports) {

    var settings = layui.settings;
    var layer = layui.layer;
    var table = layui.table;
    var admin = layui.admin;
    var upload = layui.upload;
    var form = layui.form;
    var util = layui.util;
    var xmSelect = layui.xmSelect;
    var element = layui.element;
    var dict = {};

    var language = settings.getLang();
    var codeLangs = null;
    var defaultsLangs = null;

    var foxnic = {

        selectBoxInstances:{},
        getSelectBox:function(id){
            if (!id.startWith("#")) id = "#" + id;
            return this.selectBoxInstances[id];
        },
        setSelectBoxUrl:function(id,url,cb){
            var box=this.getSelectBox(id);
            if(box) {
                box.setUrl(url,cb);
            } else {
                $("#"+id).attr("data",url);
            }
        },
        disableSelectBox:function (id,disabled) {
            var box = id;
            if(TypeUtil.isString(id)) {
                box=this.getSelectBox(id);
            }
            if(!box) return;
            box.doingDisabled=true;
            box.update({ disabled: disabled });

            $(box.options.dom).find(".xm-label-block").addClass("xm-label-block-disabled");
            $(box.options.dom).find(".xm-icon-close").hide();
            box.doingDisabled=false;

        },
        selectBoxConfigs:{},
        selectBoxQueryTime:{},
        renderSelectBox: function (cfg,rerender) {
            var me=this;
            if(!cfg) cfg={};
            if (!cfg.el.startWith("#")) cfg.el = "#" + cfg.el;
            // debugger
            var inst = xmSelect.get(cfg.el, true);
            var refreshCallback=cfg.refreshCallback;
            //不重复渲染
            if (!rerender && inst != null) return;

            if(!rerender) {
                this.selectBoxConfigs[cfg.el]=cfg;
            } else {
                cfg=this.selectBoxConfigs[cfg.el];
            }
            cfg.refreshCallback=refreshCallback;
            var el = $(cfg.el);
            if(el.find(".layui-required").length>0) {
                cfg.layVerify = 'required';
            }
            if (!cfg.searchTips) cfg.searchTips = this.translate("请输入关键字",'','cmp:select');
            if(cfg.radio && cfg.clickClose==null) {
                cfg.clickClose=true;
            }
            var data = el.attr("data");
            var url = null;
            try {
                data = JSON.parse(data);
            } catch (e) {
                url = data;
                data = null;
            }


            //本地数据渲染
            if (data != null) {
                if (cfg.transform) {
                    var opts = cfg.transform(data);
                    cfg.data = opts;
                } else {
                    cfg.data = data;
                }
            } else if (url != null) {
                //debugger
                cfg.remoteSearch = true;

                // cfg.filterable=true;


                function query(ps, cb) {
                    //再次重新读取，更改这个值，以便级联
                    var url = el.attr("data");

                    // // debugger;
                    // var key=cfg.el+";URL:"+url+";PS:"+JSON.stringify(ps);
                    // key=key.trim();
                    // var now=(new Date()).getTime();
                    // var t=me.selectBoxQueryTime[key];
                    // if (t) {
                    //     t = now - t;
                    //     //logger.info("请求时间差(" + cfg.el + "):", t);
                    //     if (t < 500) {
                    //         //var data=me.selectBoxQueryValue[key];//={opts:opts,pageCount:r.data.pageCount};
                    //         //cb && cb(data.opts, data.pageCount);
                    //         return;
                    //     }
                    // }
                    // //logger.info(cfg.id,key,now,t)
                    // me.selectBoxQueryTime[key]=now;

                    admin.request(url, ps, function (r) {
                        // debugger//

                        var opts = [];
                        me.getSelectBox(cfg.el)["currentData"]=null;
                        if (r.success) {
                            me.getSelectBox(cfg.el)["currentData"]=r.data.list;
                            if (cfg.paging) {
                                opts = cfg.transform && cfg.transform(r.data.list);
                            } else {
                                opts = cfg.transform && cfg.transform(r.data);
                            }



                        } else {
                            opts = [{name: r.message, value: "-1"}];
                        }

                        if(!opts) {
                            opts=r.data;
                        }

                        if(cfg.beforeListOptionsChange) {
                            opts = cfg.beforeListOptionsChange(opts)
                        }

                        // debugger
                        if (cfg.paging) {
                            cb(opts, r.data.pageCount);
                        } else {
                            cb(opts);
                        }
                        if(cfg.refreshCallback) {
                            cfg.refreshCallback(opts,r.data);
                            cfg.refreshCallback=null; //不再调用第二次
                        }

                        if (window.adjustPopup) {
                            window.adjustPopup();
                        }



                    }, "POST", true);
                }

                if (!cfg.filterable) {

                    var ps={};
                    if (cfg.extraParam) {
                        var ext = {};
                        if (typeof (cfg.extraParam) == 'function') {
                            ext = cfg.extraParam();
                        } else {
                            ext = cfg.extraParam;
                        }
                        for (var key in ext) {
                            ps[key] = ext[key];
                        }
                    }
                    // debugger
                    query(ps, function (r) {
                        cfg.data = r;
                        cfg.remoteSearch=false;
                        // debugger
                        var sel = xmSelect.get(cfg.el, true);
                        var val = null;
                        if(sel) {
                            val = sel.getValue();
                            sel.reset();
                            sel.update({data:cfg.data,remoteSearch:false});
                        } else {
                            xmSelect.render(cfg);
                        }
                        if(val) {
                            sel.setValue(val);
                        }
                    })
                }

                // 这个方法组件会自动触发
                cfg.remoteMethod = function (val, cb, show, pageIndex) {


                    //   if(window.xx) return;
                    //window.xx=true;
                    // debugger;
                    // z++;
                    // logger.warn("select-box:"+cfg.el,"T-"+z);
                    var ps = {searchField: cfg.searchField, searchValue: val, fuzzyField: cfg.searchField};
                    if (cfg.paging) {
                        if (!cfg.pageSize) ps.pageSize = 10;
                        else ps.pageSize = cfg.pageSize;
                    }
                    ps.pageIndex = pageIndex;

                    if (cfg.extraParam) {
                        var ext = {};
                        if (typeof (cfg.extraParam) == 'function') {
                            ext = cfg.extraParam();
                        } else {
                            ext = cfg.extraParam;
                        }
                        for (var key in ext) {
                            ps[key] = ext[key];
                        }
                    }

                    var extraParam = el.attr("extraParam");
                    if(extraParam) {
                        extraParam=JSON.parse(extraParam);
                        for (var key in extraParam) {
                            ps[key] = extraParam[key];
                        }
                    }

                    var box=me.getSelectBox(cfg.el);
                    if(box) {
                        if(!box.doingDisabled) {
                            query(ps, cb);
                        }
                    } else {
                        query(ps, cb);
                    }

                }
            }


            console.log("data", data);
            console.log("opts", opts);

            inst = xmSelect.render(cfg);

            setTimeout(function () {
                //设置值的布局方式
                if (cfg.valueDirection) {
                    var div = $(cfg.el + " .label-content");
                    div.css("flex-direction", cfg.valueDirection);
                }

                //var ctx = $(cfg.el + " xm-select");
                //ctx.css("width","auto");
                //debugger;

            }, 500);
            this.selectBoxInstances[cfg.el]=inst;
            /**
             * 获得全部数据
             * */
            inst.getAllData=function () {
                if(inst.currentData!=null) return inst.currentData;
                else return inst.options.data;
            }
            /**
             * 根据 value 获得原始数据
             * */
            inst.getDataByValue=function (valueField,value){
                var all=inst.getAllData();
                for (var i = 0; i < all.length; i++) {
                    if(all[i][valueField]==value) return all[i];
                }
                return null;
            }
            inst.setUrl =function (url,cb) {
                el.attr("data",url);
                inst.refresh(null,cb);
            }
            inst.refresh=function (param,cb) {
                if(param) {
                    inst.setExtraParam(param);
                }
                fox.renderSelectBox({
                    el:cfg.el,refreshCallback:cb
                },true);
            }
            inst.setExtraParam=function (param) {
                cfg.extraParam = param;
            }
            return inst;
        },
        disableButton:function (el,disable) {
            if(disable) {
                el.addClass("layui-btn-disabled").attr("disabled",true);
            } else {
                el.removeClass("layui-btn-disabled").attr("disabled",false);
            }
        },
        lockForm:function(fm,lock) {
            // debugger;
            var me=this;
            if(lock) {
                function keepCall(fn,t) {
                    fn();
                    var i=0;
                    var t=setInterval(function (){
                        i++;
                        fn();
                        if(i>t) clearInterval(t);
                        logger.info("hha","hhaha")
                    },1);
                }
                fm.find("input").attr("placeholder", "");
                fm.find("input").attr("readonly", "yes");
                fm.find("textarea").attr("placeholder", "");
                fm.find("textarea").attr("readonly", "yes");

                fm.find("input").addClass("layui-input-read-only");
                fm.find("textarea").addClass("layui-input-read-only");

                fm.find("input[type=checkbox]").attr("disabled", "yes");

                keepCall(function () {
                    fm.find(".layui-form-switch").addClass("layui-form-switch-disabled");
                },250);


                fm.find(".layui-form-checked i").addClass("layui-form-chcekbox-disabled");
                keepCall(function () {
                    fm.find(".layui-form-checked i").addClass("layui-form-chcekbox-disabled");
                    fm.find(".layui-form-checked i").attr("style","border-color:#a0a0a0 !important");
                },250);


                fm.find("input[type=radio]").attr("disabled", "yes");
                fm.find("input[input-type=date]").attr("disabled", "yes");
                //
                var buttons = fm.find("button");
                buttons.attr("disabled", "yes");
                buttons.removeClass("layui-btn-disabled");
                buttons.addClass("layui-btn-disabled");
                function lockSelectDelay(inst) {
                    if(inst.currentData || inst.getValue()) {
                        //debugger
                        //inst.update({disabled: true});
                        setTimeout(function (){
                            me.disableSelectBox(inst,true);
                        },100);
                        setTimeout(function (){
                            me.disableSelectBox(inst,true);
                        },1000);
                    } else {
                        setTimeout(function () {
                            lockSelectDelay(inst);
                        }, 100);
                    }
                }
                function disableSelects() {
                    var selects = fm.find("div[input-type=select]");
                    var ts={};
                    for (var i = 0; i < selects.length; i++) {
                        var id = $(selects[i]).attr("id");
                        var inst=xmSelect.get("#" + id, true);
                        if(inst) {
                            lockSelectDelay(inst);
                        }
                    }
                }
                disableSelects();
                //
                function disableUploads() {
                    var foxup = layui.foxnicUpload;
                    if (foxup) {
                        var ups = fm.find("input[input-type=upload]");
                        for (var i = 0; i < ups.length; i++) {
                            var id = $(ups[i]).attr("id");
                            foxup.disable(id);
                        }
                    }
                }
                disableUploads();
                //补刀
                for (var i = 0; i < 20; i++) {
                    setTimeout(disableUploads,500*i);
                }


            } else {
                console.error("暂不支持");
            }
        },
        setSelectValue4QueryApi:function (id,value){
            // debugger;
            var inst=xmSelect.get(id,true);
            if(inst==null) return;
            var opts=[];
            if (value) {
                opts=inst.options.transform(Array.isArray(value)?value:[value]);
                for (var i = 0; i < opts.length; i++) {
                    opts[i].selected=true;
                }
            }
            inst.setValue(opts);
            if (!id.startWith("#")) id = "#" + id;
            if(this.selectBoxConfigs[id]) {
                this.selectBoxConfigs[id].data=opts;
            }
        },
        setSelectValue4Dict:function (id,value,data){
            if(!value) return;
            var me=this;
            function setV() {
                var inst=xmSelect.get(id,true);
                if(inst==null) return;
                var opts=[];
                if(!Array.isArray(value)){
                    try{
                        value=JSON.parse(value);
                    }catch (e){
                        value=value.split(",");
                    }
                }
                var opts=[];
                for (var i = 0; i < value.length; i++) {
                   var name=me.getDictText(data,value[i]);
                   opts.push({name:name,value:value[i],selected:true});
                }
                // debugger;
                inst.setValue(opts);
                if (!id.startWith("#")) id = "#" + id;
                if(me.selectBoxConfigs[id]) {
                    me.selectBoxConfigs[id].data=opts;
                }
            }
            setTimeout(setV,1);
        },
        setSelectValue4Enum:function (id,value,data){
            if(!value) return;
            var me=this;
            function setV() {
                var inst=xmSelect.get(id,true);
                if(inst==null) return;
                var opts=[];
                if(!Array.isArray(value)){
                    try{
                        value=JSON.parse(value);
                        if(!Array.isArray(value)) {
                            value=[value];
                        }
                    }catch (e){
                        value=value.split(",");
                    }
                }
                var opts=[];
                for (var i = 0; i < value.length; i++) {
                    var name=me.getEnumText(data,value[i]);
                    opts.push({name:name,value:value[i]});
                }
                inst.setValue(opts);
                if (!id.startWith("#")) id = "#" + id;
                if(this.selectBoxConfigs && this.selectBoxConfigs[id]) {
                    this.selectBoxConfigs[id].data=opts;
                }
            };
            setTimeout(setV,1);
        },
        /**
         * 渲染分页的表格
         * */
        renderTable: function (cfg) {
            var me=this;
            var tableId = cfg.elem.substring(1);

            if(cfg.even==null) {
                cfg.even = true;
            }

            // debugger;
            if (window.LAYUI_TABLE_WIDTH_CONFIG) {
                //debugger;
                var columnWidthConfig = LAYUI_TABLE_WIDTH_CONFIG[tableId];
                if(columnWidthConfig && columnWidthConfig["row-ops"]) {
                    columnWidthConfig["row-ops"].orderIndex = 99999999;
                }
                if (columnWidthConfig) {
                    cfg.url = settings.base_server + cfg.url;
                    var cols = cfg.cols[0];
                    // var prevFlag = 0, prev = null;
                    //debugger
                    for (var i = 0; cols && i < cols.length; i++) {
                        cols[i].programTitle=cols[i].title;
                        var columnConfig=columnWidthConfig[cols[i].field];
                        if(columnConfig==null) continue;
                        if(TypeUtil.isNumber(columnConfig)) {
                            columnConfig={width:columnConfig};
                        }

                        //if (cols[i].hide) continue;
                        if (cols[i].field==this.translate('空白列')) continue;
                        // if (cols[i].field=='row-ops') continue;
                        // if(cols[i].field=="createTime") {
                        // 	debugger;
                        // 	columnWidthConfig[cols[i].field]=200;
                        // }
                        //var w = columnWidthConfig[cols[i].field];
                        // if (w) {
                        cols[i].width = columnConfig.width;
                        cols[i].orderIndex = columnConfig.orderIndex;
                            // debugger
                            // if(cfg.hide!==null) {
                        cols[i].hide = columnConfig.hide;
                            // }
                        console.log(cols[i].field, columnConfig);
                        // }
                        // if (cols[i].field == this.translate('空白列')) prevFlag = 1;
                        // if (prevFlag == 0) {
                        //     prev = cols[i];
                        // }
                    }
                    //if (prev) prev.width = null;
                }

                if(cols) {
                    cols.sort(function (a, b) {
                        return a.orderIndex - b.orderIndex;
                    });
                }

            }

            //debugger
            if(cfg.page==null || cfg.page.limits == null) {
                if(cfg.page==null) cfg.page={};
                if(cfg.page.limits==null) {
                    var pages = admin.getVar("ui_table_page_levels");
                    if(pages==null) {
                        pages = localStorage.getItem("ui_table_page_levels");
                        if(pages!=null && pages.length>2) {
                            pages=JSON.parse(pages);
                            admin.putVar("ui_table_page_levels",pages);
                        }
                    }
                    cfg.page.limits=pages;
                }
            }


            var data=null;
            var token = settings.getToken();
            if(token) {
                token=token.accessToken;
            }
            var basicConfig = {
                method: 'POST',
                headers: {
                    'Authorization': "Bearer "+token,
                    // tid:null,
                    time:admin.getRequestTimestamp()
                },
                request: {
                    pageName: "pageIndex",
                    limitName: "pageSize"
                },
                beforeRequest:function (opt) {
                    opt.headers.time=admin.getRequestTimestamp();
                    $(cfg.elem).parent().find("table").css("opacity","0.0");
                    // debugger
                },
                afterRequest:function (res) {
                    setTimeout(function (){
                        $(cfg.elem).parent().find("table").animate({
                            opacity:'1.0'
                        },100,null,function (){
                            $(cfg.elem).parent().find("table").css("opacity","1.0");});
                    },100);
                },
                parseData: function (res) { //res 即为原始返回的数据
                    // debugger;

                    if (!res.success) {
                        foxnic.showMessage(res);
                        return null;
                    }
                    data=res.data.list;
                    return {
                        "code": res.code == "00" ? 0 : -1, //解析接口状态
                        "msg": res.message, //解析提示文本
                        "count": res.data.totalRowCount, //解析数据长度
                        "data": res.data.list //解析数据列表
                    };
                },
                page: true
            };
            // debugger;

            //覆盖基础配置
            for (var key in basicConfig) {
                if (cfg[key] != null) continue;
                cfg[key] = basicConfig[key];
            }

            // 处理列
            var cols=[];
            for (var ln = 0; ln < cfg.cols.length; ln++) {
                var lncols=[];
                for (var c = 0; c < cfg.cols[ln].length; c++) {
                    var col=cfg.cols[ln][c];
                    if(col.perm) {
                        if(admin.checkAuth(col.perm)) {
                            lncols.push(col);
                        }
                    } else {
                        lncols.push(col);
                    }
                }
                cols.push(lncols);
            }
            cfg.cols = cols;

            cfg.autoSort = false;

            var userDone = cfg.done;

            function done() {
                if (cfg.footer) {
                    //debugger;
                    renderSearchContent(this);
                    renderFooter(this, cfg.footer);
                }
                if (userDone) userDone(data);
                $("#"+tableId+" th").bind("change", ()=>{
                    // alert("div出发了change事件");
                    console.log('div出发了change事件')
                });
            }

            // 当选择列之后，触发状态保存
            // table.on('toolbar('+tableId+')', function(obj){
            //     form.on('checkbox(LAY_TABLE_TOOL_COLS)', function(data){
            //         var ths = $("#"+tableId+" th .layui-table-cell");
            //         setTimeout(function (){
            //             saveTableSettings4UI(tableId,ths,cols);
            //         },1);
            //     });
            // });

            function renderSearchContent(it){

                //debugger
                function hasVal(val) {
                    if(val==null) return false;
                    if(val==undefined) return false;
                    if(val=="") return false;
                    if(Array.isArray(val)) {
                        if(val.length==0) return false;
                    }
                    return true;
                }

                $(".layui-btn-container").find(".search-content-badge").remove();
                if(!it.where) return;
                if(!it.where.searchValue) return;
                var values=JSON.parse(it.where.searchValue);
                if(!values) values={};

                for(var itm in values) {
                    // debugger
                    if(cfg.ignoreSearchContent){
                        var ignore=cfg.isIgnoreSearchContent(itm);
                        if(ignore) continue;
                    }
                    var label=$("."+itm+"-label").text();
                    if(!hasVal(label)) continue;
                    var v=values[itm];
                    var t="";
                    var inputType=v["inputType"];
                    if(inputType=="button" && !hasVal(v["value"])) {
                        continue;
                    }

                    if("orgId"==itm) {
                        debugger;
                    }

                    if(hasVal(v["label"])) {
                        if(Array.isArray(v.label)) {
                            t=v.label.join(",");
                        } else {
                            t=v.label;
                        }
                        t=t.replace(/,/g,", ");
                    }
                    else if(hasVal(v["value"])) {
                        t=v.value;
                    }
                    else if(hasVal(v["begin"]) && !hasVal(v["end"])) {
                        t="[ "+v.begin+", - ]";
                    }
                    else if(!hasVal(v["begin"]) && hasVal(v["end"])) {
                        t="[ -, "+v.end+" ]";
                    }
                    else  if(hasVal(v["begin"]) && hasVal(v["end"])) {
                        t="[ "+v.begin+", "+v.end+" ]";
                    }
                    if(t.length==0) continue;
                    var badge=$(".layui-btn-container").append('<span var-name="'+itm+'" class="layui-badge-rim search-content-badge">'+label+' : '+t+'</span>');

                }
                //
                $(".layui-btn-container").find(".search-content-badge").click(function (e){
                    var varName=$(this).attr("var-name");
                    $(this).remove();
                    $("#"+varName).val("");
                    $("#"+varName+"-begin").val("");
                    $("#"+varName+"-end").val("");
                    if(xmSelect.get("#"+varName,true)!=null) {
                        xmSelect.get("#"+varName,true).setValue([]);
                    }
                    values[varName]={};
                    //alert(varName);
                    it.where.searchValue=JSON.stringify(values);
                    // debugger
                    var layFilter=$(it.elem).attr("lay-filter");
                    //it.reload();
                    //table.reload("data")
                    table.reload(layFilter, { where : it.where });
                });
            }

            function renderFooter(it, footer) {

                var url = it.url;
                url = url.substring(0, url.lastIndexOf('/'));

                var div = $("#layui-table-page" + it.index);
                var buttons = [];

                if(footer.importExcel){
                    var exportExcelTemplateButton = '<button id="layui-table-page' + it.index + '-footer-download-excel-template"  type="button" class="layui-btn layui-btn-primary layui-btn-xs"><i class="fa fa-download"></i> 下载模版</button>';
                    buttons.push(exportExcelTemplateButton);
                    var importExcelButton = '<button id="layui-table-page' + it.index + '-footer-import-excel"  type="button" class="layui-btn layui-btn-primary layui-btn-xs"><i class="fa fa-cloud-upload"></i> 导入数据</button>&nbsp;&nbsp;';
                    buttons.push(importExcelButton);
                }

                if(footer.exportExcel) {
                    var exportExcelButton = '<button id="layui-table-page' + it.index + '-footer-download-excel"  type="button" class="layui-btn layui-btn-primary layui-btn-xs"><i class="fa fa-cloud-download"></i>  导出数据</button>';
                    buttons.push(exportExcelButton);
                }

                div.append("<div style='float:right;margin-right: 4px'>" + buttons.join("") + "</div>")

                if (footer.importExcel) {
                    $('#layui-table-page' + it.index + '-footer-download-excel-template').click(function () {
                        foxnic.submit(url + "/export-excel-template", it.where);
                    });
                    var ps = footer.importExcel.params;
                    if (ps && (typeof ps === "function")) {
                        ps = ps();
                    }
                    var cb = footer.importExcel.callback;
                    if (!ps) ps = {};
                    foxnic.bindImportButton($('#layui-table-page' + it.index + '-footer-import-excel'), url + "/import-excel", ps, function (r) {
                        cb && cb(r);
                    });
                    // $('#layui-table-page' + it.index + '-footer-import-excel').click(function () {
                    // 	//foxnic.submit(url + "/export-excel-template", it.where);
                    // 	alert("打开文件")
                    // });
                }
                if (footer.exportExcel) {
                    $('#layui-table-page' + it.index + '-footer-download-excel').click(function () {
                        foxnic.submit(url + "/export-excel", it.where);
                    });
                }

            }
            // debugger
            cfg.done = done;
            if (!table.instance) table.instance = [];
            var inst = table.render(cfg);
            table.instance.push(inst)
            return inst;
        },

        /**
         * 字典值转换成标签
         * */
        transDict: function (dictCode, itemCode) {
            var map = dict[dictCode];
            var label = null;
            if (map==null) label = map[itemCode];
            return label == null ? "--" : label;
        },

        translate: function (defaults, code , context) {

            // debugger
            return top.translate(defaults, code , context);

            // debugger
            if(!context) context="defaults";

            //debugger
            if (defaultsLangs == null) {
                //debugger;
                codeLangs = localStorage.getItem("language_codeLangs");
                if (codeLangs && codeLangs.length > 2) {
                    codeLangs = JSON.parse(codeLangs);
                }
                defaultsLangs = localStorage.getItem("language_defaultsLangs");
                if (defaultsLangs && defaultsLangs.length > 2) {
                    defaultsLangs = JSON.parse(defaultsLangs);
                }
            }
            if (defaultsLangs == null) {
                return defaults;
            }
            var item = defaultsLangs[context+":"+defaults];
            var text = null;
            if (!item && code) {
                item = codeLangs[context+":"+code];
            }
            if (item) {
                text = item[language];
                if (text && text != ":ns;") {
                    return text;
                }
            }

            if (!text || text == ":ns;") {
                text = defaults;
            }

            //如果条目不存在，则插入
            if (!item && admin) {
                debugger
                admin.request("/service-system/sys-lang/insert", {code: code, defaults: defaults,context: context}, function (data) {
                    localStorage.removeItem("language_timestamp");
                });
            }

            return text ? text : "--";
        },


        /**
         * 渲染文件导入按钮
         * */
        bindImportButton: function (buttonEl, url, params, callback) {
            url = url.replace("//", "/");
            url = url.replace("http:/", "http://");
            url = url.replace("https:/", "https://");
            var task = null;
            var uploadInst = upload.render({
                elem: buttonEl
                , url: url //改成您自己的上传接口
                , data: params
                , accept: "file"
                , headers: {token: settings.getToken()}
                , before: function (obj) {
                    task = setTimeout(function () {
                        layer.load(2);
                    }, 500);
                    //预读本地文件示例，不支持ie8
                    //obj.preview(function(index, file, result){
                    //  $(imageEl).attr('src', result); //图片链接（base64）
                    //  debugger;
                    //});
                }
                , done: function (res) {
                    clearTimeout(task);
                    layer.closeAll('loading');
                    var next=true;
                    if(callback) {
                        next=callback(res);
                    }
                    if(!next) return;
                    //如果上传失败
                    if (!res.success) {
                        return layer.msg(res.message);
                    }
                }
                , error: function () {
                    clearTimeout(task);
                    layer.closeAll('loading');
                    layer.msg('上传失败');
                    //演示失败状态，并实现重传
                    //var demoText = $('#demoText');
                    //demoText.html('<span style="color: #FF5722;">上传失败</span> <a class="layui-btn layui-btn-xs demo-reload">重试</a>');
                    //demoText.find('.demo-reload').on('click', function(){
                    //  uploadInst.upload();
                    //});
                }
            });

        },

        dateFormat:function(t, f) {
            // debugger;
            if (!t) return "";
            // debugger;
            if(Number.isInteger(t)) {
                return util.toDateString(t, f);
            } else if(f && f.length!=t.length) {
                return util.toDateString(t, f);
            }
            else {
                return t;
            }
        },

        renderFormInputs:function(form) {
            this.renderInputs($(".layui-input"));
            form.render();
        },
        renderSearchInputs:function() {
            this.renderInputs($(".search-input"));
        },

        renderInputs:function(inputs) {

            for (var i = 0; i < inputs.length; i++) {
                var input = $(inputs[i]);
                var inputType = input.attr("input-type");
                if (inputType == "number_input") {
                    var integer = input.attr("integer");
                    var decimal = input.attr("decimal");
                    var allowNegative = input.attr("allow-negative");
                    var minValue = input.attr("min-value");
                    if(minValue=="") minValue=null;
                    var maxValue = input.attr("max-value");
                    if(maxValue=="") maxValue=null;
                    var scale = input.attr("scale");
                    var step = input.attr("step");
                    this.renderAsNumberInput(input, decimal, scale, step, minValue, maxValue);
                    if (integer == "true") {
                        this.limitNumbrInput4Integer(input, allowNegative == "true", minValue, maxValue);
                    } else if (decimal == "true") {
                        this.limitNumberInput4Decimal(input, allowNegative == "true", scale, minValue, maxValue);
                    }
                }
            }

        },
        renderAsNumberInput: function (input, decimal, scale, step, minValue, maxValue) {
            step = parseFloat(step);
            minValue = parseFloat(minValue);
            maxValue = parseFloat(maxValue);
            scale = parseInt(scale);
            if (isNaN(scale)) scale = 2;
            if (!isNaN(step)) {
                function doStep(e) {
                    // debugger;
                    if(input.attr("readonly")) return;
                    var f=$(document.activeElement);
                    if(f && f.length>0 &&  input[0]!=f[0]) return;
                    var v = input.val().trim();
                    v = parseFloat(v);
                    if (e.key == "ArrowDown" || e.deltaY > 0) {
                        if (isNaN(v)) {
                            if (isNaN(maxValue)) {
                                v = 0
                            } else {
                                v = maxValue;
                            }
                        }
                        v -= step;
                        if (!isNaN(minValue)) {
                            if (v < minValue) v = minValue;
                        }
                        if (decimal == "true") {
                            v = Math.round(v * Math.pow(10, scale)) / Math.pow(10, scale);
                        } else {
                            v = Math.round(v);
                        }
                        input.val(v);
                    }
                    if (e.key == "ArrowUp" || e.deltaY < 0) {
                        if (isNaN(v)) {
                            if (isNaN(minValue)) {
                                v = 0
                            } else {
                                v = minValue;
                            }
                        }
                        v += step;
                        if (!isNaN(maxValue)) {
                            if (v > maxValue) v = maxValue;
                        }
                        if (decimal == "true") {
                            v = Math.round(v * Math.pow(10, scale)) / Math.pow(10, scale);
                        } else {
                            v = Math.round(v);
                        }
                        input.val(v);
                    }
                };

                //滚轮上下滚动
                input.hover(function () {
                    if(input.attr("readonly")) return;
                    console.log("hover")
                    addHandler(document, 'mousewheel');
                    addHandler(document, 'DOMMouseScroll');
                }, function () {
                    if(input.attr("readonly")) return;
                    removeHandler(document, 'mousewheel');
                    removeHandler(document, 'DOMMouseScroll');
                });


                function addHandler(element, type) {
                    if (element.addEventListener) {
                        element.addEventListener(type, doStep, false);
                    } else if (element.attachEvent) {
                        element.attachEvent("on" + type, doStep);
                    } else {
                        element["on" + type] = doStep;
                    }
                }

                function removeHandler(element, type) {
                    if (element.removeEventListener) {
                        element.removeEventListener(type, doStep, false);
                    } else if (element.detachEvent) {
                        element.detachEvent("on" + type, doStep);
                    } else {
                        element["on" + type] = null;
                    }
                }

                //键盘上下按键
                input.keyup(doStep);
            }
        },

        /**
         * 仅允许输入框输入整数
         * */
        limitNumbrInput4Integer:function(inputId, negative, minValue, maxValue) {
            var inst = null;
            if (typeof (inputId) == 'string') {
                if (!inputId.startWith("#")) inputId = "#" + inputId;
                inst = $(inputId);
            } else {
                inst = inputId;
            }
            var reg = /[^0-9]/g;
            if (negative) { //如果负数
                reg = /[^0-9-]/g;
            }

            function applyValue(input, val) {
                if(val!=null && val.length>0) {
                    val = parseInt(val);
                }
                input.attr("val", val);
                input.val(val);
            }

            function revertValue(input) {
                input.val(input.attr("val"));
            }

            function limit() {
                var val = $(this).val().trim();
                val = val.replace(reg, '');

                if ("" == val) {
                    applyValue($(this), val);
                    return;
                }

                if (negative) {
                    if ("-" == val || "" == val) {
                        applyValue($(this), val);
                        return;
                    }
                    //减号只能出现在第一位
                    for (var j = 1; val.length > 1 && j < val.length; j++) {
                        if (val.charAt(j) == '-') {
                            revertValue($(this));
                            return;
                        }
                    }
                }

                //检查对否能转换哼整数
                var i = parseInt(val);
                if (isNaN(i)) {
                    revertValue($(this));
                    return;
                }

                //范围控制
                var delayTaskId = inst.attr("delay-task-id");
                if (delayTaskId != "") {
                    clearTimeout(delayTaskId);
                }
                delayTaskId = setTimeout(function () {
                    //debugger
                    if (!isNaN(minValue) && minValue!=null) {
                        if (i < minValue) val = minValue;
                    }

                    if (!isNaN(maxValue) && maxValue != null) {
                        if (i > maxValue) val = maxValue;
                    }
                    applyValue(inst, val);
                }, 1000);
                inst.attr("delay-task-id", delayTaskId);
                //
                applyValue($(this), val);
            }

            //
            inst.keyup(limit).bind("paste", limit).css("ime-mode", "disabled");
        },

        /**
         * 仅允许输入框输入数字
         * */
        limitNumberInput4Decimal:function(inputId, negative, scale, minValue, maxValue) {
            scale = parseInt(scale);
            if (isNaN(scale)) scale = 2;
            var inst = null;
            if (typeof (inputId) == 'string') {
                if (!inputId.startWith("#")) inputId = "#" + inputId;
                inst = $(inputId);
            } else {
                inst = inputId;
            }
            var reg = /[^0-9.]/g;
            if (negative) { //如果负数
                reg = /[^0-9.-]/g;
            }

            function applyValue(input, val) {
                if(val!=null && val.length>0 && !val.endWith(".")) {
                    val = parseFloat(val);
                    val = Math.round(val * Math.pow(10, scale)) / Math.pow(10, scale);
                }
                input.attr("val", val);
                input.val(val);
            }

            function revertValue(input) {
                input.val(input.attr("val"));
            }

            function limit() {
                var val = $(this).val().trim();
                val = val.replace(reg, '');

                if ("" == val) {
                    applyValue($(this), val);
                    return;
                }

                //只能出现一次小数点
                var dots = 0;
                for (var i = 0; i < val.length; i++) {
                    if (val.charAt(i) == '.') {
                        dots++;
                        if (dots >= 2) {
                            revertValue($(this));
                            return;
                        }
                    }
                }

                if (negative) {
                    if ("-" == val || "" == val) {
                        applyValue($(this), val);
                        return;
                    }
                    //减号只能出现在第一位
                    for (var j = 1; val.length > 1 && j < val.length; j++) {
                        if (val.charAt(j) == '-') {
                            revertValue($(this));
                            return;
                        }
                    }
                }
                //
                //检查对否能转换哼整数
                var i = parseFloat(val);
                if (isNaN(i)) {
                    revertValue($(this));
                    return;
                }

                //范围控制
                var delayTaskId = inst.attr("delay-task-id");
                if (delayTaskId != "") {
                    clearTimeout(delayTaskId);
                }
                delayTaskId = setTimeout(function () {
                    //debugger
                    if (!isNaN(minValue)) {
                        if (i < minValue) val = minValue;
                    }

                    if (!isNaN(maxValue)) {
                        if (i > maxValue) val = maxValue;
                    }
                    applyValue(inst, val);
                }, 1000);
                inst.attr("delay-task-id", delayTaskId);

                applyValue($(this), val);
            }

            //
            inst.keyup(limit).bind("paste", limit).css("ime-mode", "disabled");
        },
        badgeLabelIndex:{},
        badgeLabelStyleMap:{},
        getBadgeLabelStyle:function (styles,code,text,field) {
            if(!field) field="default";
            if(!styles) return "layui-badge-rim";
            var s=this.badgeLabelStyleMap[text];
            // debugger
            if(!s) {
                if(styles.type=="array") {
                    // debugger
                    var index=this.badgeLabelIndex[field];
                    if(!index && index!==0) index=-1;
                    index=index+1;
                    s = styles.styles[index % styles.styles.length];
                    this.badgeLabelIndex[field] = index ;
                } else if(styles.type=="map") {
                    s = styles.styles[code];
                }
                this.badgeLabelStyleMap[text]=s;
            }

            return s;
        },
        parseBadgeLabelStyle:function (styles) {
            if(!styles) return null;
            var type="";
            if(TypeUtil.isObject(styles)) {
                return styles;
            }
            if("#BY-THEME"==styles) {
                styles=Theme.badgeStyles;
                type="array";
            } else if(styles.startWith("[") && styles.endWith("]")){
                styles=JSON.parse(styles);
                type="array";
            }
            else if(styles.startWith("{") && styles.endWith("}")){
                styles=JSON.parse(styles);
                type="map";
            } else {
                styles=null;
            }

            return {styles:styles,type:type};

        },

        joinLabel:function (data, key, sep,styles,field) {
            if (data==null) return "";
            var label = "";
            if (!sep) sep = ",";

            if(styles) {
                styles=this.parseBadgeLabelStyle(styles);
                if(styles.type=="map") {
                    var tmp=[];
                    for (var p in styles.styles) {
                        tmp.push(styles.styles[p]);
                    }
                    styles.styles=tmp;
                    styles.type="array";
                }
            }





            if (Array.isArray(data)) {
                var labels = [];
                for (var i = 0; i < data.length; i++) {
                    if (data[i]==null) continue;
                    label = data[i][key];
                    if (label) {
                        if(styles) {
                            labels.push("<span class='" + this.getBadgeLabelStyle(styles,label,label,field) + "'>" + label + "</span>");
                        } else {
                            labels.push(label);
                        }
                    }
                }
                if(styles) {
                    label = labels.join(" ");
                } else {
                    label = labels.join(sep);
                }
            } else {
                label = data[key];
            }
            if (!label) label = "";
            return label;
        },
        getEnumText: function (list, code,styles,field) {
            if(code==null || code=="") return code;
            if (list==null) return code;

            styles=this.parseBadgeLabelStyle(styles);

            for (var i = 0; i < list.length; i++) {
                if (list[i]["code"] == code) {
                    var text=list[i]["text"];
                    if(styles) {
                        return "<span class='" + this.getBadgeLabelStyle(styles,code,text,field) + "'>" +text+"</span>";
                    } else {
                        return text;
                    }
                }
            }


            var codes=null;
            try {
                if(code!=null){
                    if(code.startWith("[") && code.endWith("]")) {
                        codes = JSON.parse(code);
                    } else {
                        codes = code.split(",");
                    }
                }
            } catch (e){}


            // 如果被拆分
            if(codes!=null) {
                // 排除拆分无效的情况
                if(codes.length==1 && codes[0]==code) {
                    return code;
                }
                var texts=[];
                var text=null;
                for (var i = 0; i < codes.length; i++) {
                    text=this.getEnumText(list,codes[i]);
                    if(text) {
                        if(styles) {
                            texts.push("<span class='" + this.getBadgeLabelStyle(styles,codes[i],text,field) + "'>" + text + "</span>");
                        } else {
                            texts.push(text);
                        }
                    }
                }
                if(texts.length>0) {
                    return texts.join(",");
                } else {
                    return "";
                }
            }

            if (code==null) code = "";
            return code;
        },
        /**
         * 获得指定属性的值
         * */
        getProperty:function (data,path,start,styles,field) {
            if(data==null) return "";
            if(start==null) start=0;
            var prop,value=data;

            if(styles) {
                //debugger
                styles=this.parseBadgeLabelStyle(styles);
                if(styles.type=="map") {
                    var tmp=[];
                    for (var p in styles.styles) {
                        tmp.push(styles.styles[p]);
                    }
                    styles.styles=tmp;
                    styles.type="array";
                }
            }


            for (var i = start; i < path.length; i++) {
                prop=path[i];
                value=value[prop];
                if(value==null) return "";
                if(TypeUtil.isArray(value) && value.length>0){
                    var rets=[];
                    for (var j = 0; j < value.length; j++) {
                        var ret=this.getProperty(value[j],path,i+1,styles);
                        if(TypeUtil.isArray(ret)) {
                            for (let k = 0; k < ret.length; k++) {
                                var text=ret[k];
                                if(styles) {
                                    rets.push("<span class='" + this.getBadgeLabelStyle(styles,text,text,field) + "'>" + text + "</span>");
                                } else {
                                    rets.push(text);
                                }
                            }
                        } else {
                            if(styles) {
                                rets.push("<span class='" + this.getBadgeLabelStyle(styles,ret,ret,field) + "'>" + ret + "</span>");
                            } else {
                                rets.push(ret);
                            }
                        }
                    }
                    if(styles) {
                        value = rets.join(" ");
                    } else {
                        value = rets.join(",");
                    }
                    return value;
                } else {
                    if(styles && !TypeUtil.isObject(value) && !TypeUtil.isArray(value)) {
                        value = "<span class='" + this.getBadgeLabelStyle(styles, value, value,field) + "'>" + value + "</span>";
                    }
                }
            }
            return value;
        },
        getDictText: function (list, code,styles,field) {
            if(code==null || code=="") return code;
            if (list==null) return code;

            if(styles) {
                styles=this.parseBadgeLabelStyle(styles);
            }

            for (var i = 0; i < list.length; i++) {
                if (list[i]["code"] == code) {
                    var text=list[i]["text"];
                    if(styles) {
                        return "<span class='" + this.getBadgeLabelStyle(styles, code, text,field) + "'>"+text+"</span>";
                    } else {
                        return text;
                    }
                }
            }
            var codes=null;
            try {
                if(code!=null){
                    if(code.startWith("[") && code.endWith("]")) {
                        codes = JSON.parse(code);
                    } else {
                        codes = code.split(",");
                    }
                }
            } catch (e){
                debugger;
            }
            // 如果被拆分
            if(codes!=null) {
                // 排除拆分无效的情况
                if(codes.length==1 && codes[0]==code) {
                    return code;
                }
                var texts=[];
                var text=null;
                for (var i = 0; i < codes.length; i++) {
                    text=this.getDictText(list,codes[i],styles);
                    if(styles){
                        texts.push("<span class='" + this.getBadgeLabelStyle(styles, codes[i], text,field) + "'>"+text+"</span>");
                    } else {
                        texts.push(text);
                    }
                }
                if(texts.length>0) {
                    if(styles) {
                        return texts.join(" ");
                    } else {
                        return texts.join(",");
                    }
                } else {
                    return "";
                }
            }

            if (code==null) code = "";
            return code;
        },

        /**
         * 绑定Switch开关
         * */
        bindSwitchEvent: function (layFilter, updateApiUrl, idProp, logicProp, callback) {
            form.on('switch(' + layFilter + ')', function (obj) {
                layer.load(2);
                var data = {};
                data[idProp] = obj.elem.value;
                data[logicProp] = obj.elem.checked ? 1 : 0;
                admin.request(updateApiUrl, data, function (result) {
                    layer.closeAll('loading');
                    if (result.success) {
                        //layer.msg(data.message, {icon: 1, time: 500});
                        admin.toast().success(result.message,{time:1000,position:"right-bottom"})
                    } else {
                        layer.msg(result.message, {icon: 2, time: 3000});
                        $(obj.elem).prop('checked', !obj.elem.checked);
                        form.render('checkbox');
                    }
                    callback && callback(result,data,obj);
                }, 'POST');
            });
        },
        getQueryVariable: function (variable) {
            var query = window.location.search.substring(1);
            var vars = query.split("&");
            for (var i = 0; i < vars.length; i++) {
                var pair = vars[i].split("=");
                if (pair[0] == variable) {
                    return pair[1];
                }
            }
            return (false);
        },
        /**
         * 获得下拉框的选中值，参数为参数 name 属性
         * */
        getSelectedValue:function (selectId,muliti) {
            if(selectId.startWith("#")) selectId=selectId.substr(1);
            var inst=xmSelect.get("#"+selectId,true);
            if(inst==null) return "";
            var value=inst.getValue("value");
            if(!muliti) {
                if(value && value.length>0) {
                    value=value[0];
                } else {
                    return "";
                }
            }
            if(TypeUtil.isArray(value)) return value.join(",");
            return value;
        },
        /**
         * 获得复选框的选中清单，参数为参数 name 属性
         * */
        getCheckedValue:function (checkBoxName) {
            var data=[];
            $('input[name="'+checkBoxName+'"]:checked').each(function() {
                data.push($(this).val());
            });
            return data;
        },
        /**
         * 设置复选框的选中清单，参数为参数 name 属性
         * */
        setCheckedValue:function (checkBoxName,value) {
            // debugger;
            if(value==null) return;
            if(!Array.isArray(value)) {
                try {
                    value=JSON.parse(value);
                } catch (e){
                    if(value!=null) {
                     value=value.split(",");
                    } else {
                        value = [];
                    }
                }
            }
            $('input[name="'+checkBoxName+'"]').each(function(a,b,c) {
                var v=$(this).val();
                for (var i = 0; i < value.length; i++) {
                    if(v==value[i]){
                        $(this).attr("checked","yes");
                    } else {
                        $(this).removeAttr("checked");
                    }
                }
            });
            form.render();
        },
        /**
         * 表单验证
         * @param {*} formId 表单所在容器id
         * @returns 是否通过验证
         */
        formVerify:function(formId,data,validateConfig) {
            var stop = null //验证不通过状态
                , verify = layui.form.config.verify //验证规则
                , DANGER = 'layui-form-danger' //警示样式
                , formElem = $('#' + formId) //当前所在表单域
                , verifyElem = formElem.find('*[lay-verify]') //获取需要校验的元素
                , device = layui.device();

            //开始校验
            layui.each(verifyElem, function (_, item) {
                // debugger;
                var othis = $(this)
                    , vers = othis.attr('lay-verify').split('|')
                    , verType = othis.attr('lay-verType') //提示方式
                    , value = othis.val()

                othis.removeClass(DANGER) //移除警示样式

                //遍历元素绑定的验证规则
                layui.each(vers, function (_, thisVer) {
                    var isTrue //是否命中校验
                        , errorText = '' //错误提示文本
                        , isFn = typeof verify[thisVer] === 'function'

                    //匹配验证规则
                    if (verify[thisVer]) {
                        var isTrue = isFn ? errorText = verify[thisVer](value, item) : !verify[thisVer][0].test(value)
                        errorText = errorText || verify[thisVer][1]

                        if (thisVer === 'required') {
                            errorText = othis.attr('lay-reqText') || errorText
                        }

                        //如果是必填项或者非空命中校验，则阻止提交，弹出提示
                        if (isTrue) {
                            //提示层风格
                            if (verType === 'tips') {
                                top.layer.tips(errorText, function () {
                                    if (typeof othis.attr('lay-ignore') !== 'string') {
                                        if (item.tagName.toLowerCase() === 'select' || /^checkbox|radio$/.test(item.type)) {
                                            return othis.next()
                                        }
                                    }
                                    return othis
                                }(), { tips: 1 })
                            } else if (verType === 'alert') {
                                top.layer.alert(errorText, { title: '提示', shadeClose: true })
                            } else {
                                top.layer.msg(errorText, { icon: 5, shift: 6 })
                            }

                            //非移动设备自动定位焦点
                            if (!device.android && !device.ios) {
                                setTimeout(function () {
                                    item.focus()
                                }, 7)
                            }

                            othis.addClass(DANGER)
                            return stop = true
                        }
                    }
                })
                if (stop) return stop
            })

            if (stop) return false

            function getMessage(cfg) {
                if(cfg.inputType=="text_input" || cfg.inputType=="text_area") {
                    return "请填写"+cfg.labelInForm;
                }

                if(cfg.inputType=="upload") {
                    return "请在"+cfg.labelInForm+"上传文件";
                }

                if(cfg.inputType=="date_input") {
                    return "请在"+cfg.labelInForm+"选择日期";
                }

                if(cfg.inputType=="select_box") {
                    return "请选择"+cfg.labelInForm;
                }

                if(cfg.inputType=="radio_box" || cfg.inputType=="check_box") {
                    return "请勾选"+cfg.labelInForm;
                }

                return "请填写"+cfg.labelInForm;

            }

            var message=null;
            layui.each(validateConfig, function (f, cfg) {
                var v=data[f];
                if(cfg.required) {
                    if(v==null) {
                        message=getMessage(cfg);
                        return true;
                    }
                    if(typeof(v)=="string" && v.trim().length==0) {
                        message=getMessage(cfg);
                        return true
                    }
                    if(Array.isArray(v) && v.length==0) {
                        message=getMessage(cfg);
                        return true
                    }
                }
            });

            if(message) {
                top.layer.msg(message, { time: 2000, icon: 5 });
                return false;
            }
            return true
        },
        // searchLayerIndex:-1,
        switchSearchRow: function(limit,cb) {
            var rows=$(".search-inputs");
            if(rows.length<=limit) return;
            var row=$(rows[limit]);
            //debugger
            var ex=row.attr("collapsed");
            for (var i = 0;i < rows.length; i++) {
                if(i>=limit) {
                    if(ex!="1") {
                        $(rows[i]).hide();
                        $(rows[i]).attr("collapsed","1");
                    } else {
                        $(rows[i]).show();
                        $(rows[i]).attr("collapsed","0");
                    }
                }
            }
            if(ex=="1") {
                $(".search-bar").css("box-shadow","1px 1px 50px rgb(0 0 0 / 30%");
                //$(".search-bar").css("border-bottom","#eeeeee solid 1px");
            } else {
                $(".search-bar").css("box-shadow","");
                //$(".search-bar").css("border-bottom","none");
            }

            cb && cb(ex);
        },
        /**
         * 调整搜索相关的尺寸
         * */
        adjustSearchElement:function(t) {
            var me=this;
            if(!t) t=0;
            // var rows=$(".search-inputs");
            // var divs=$(".search-label-div");
            // if(rows.length>1) {
            //     var maxWidth = 0;
            //     for (var i = 0; i < divs.length; i++) {
            //         var div = $(divs[i]);
            //         var w = div.width();
            //         if (maxWidth < w) maxWidth = w;
            //     }
            //     divs.width(maxWidth);
            // }
            t++;
            //debugger;
            var h=$(".search-bar").height();
            $(".search-buttons").css("margin-top",(h-$(".search-buttons").height()-8)+"px");
            var ks=$(window).width()-$(".search-buttons").width()-16;
            $(".search-buttons").css("left",ks+"px");
            $(".search-input-rows").animate({opacity:'1.0'},0.25);
            $(".search-buttons").animate({opacity:'1.0'},0.25);

            if(t< 2) {
                logger.info("adjustSearchElement","快速调整期")
                //渲染后的补充执行
                setTimeout(function () {
                    me.adjustSearchElement(t);
                    //console.log("adjustSearchElement:"+t);
                }, 16 );
            }
            // else if(t<8) {
            //     //渲染后的补充执行
            //     logger.info("adjustSearchElement","补充调整期")
            //     setTimeout(function () {
            //         me.adjustSearchElement(t);
            //         //console.log("adjustSearchElement:"+t);
            //     }, 1000 );
            // }
            // else if(t<100) { // 此分支用于测试
            //     //渲染后的补充执行
            //     logger.info("adjustSearchElement","测试调整期")
            //     setTimeout(function () {
            //         me.adjustSearchElement(t);
            //         //console.log("adjustSearchElement:"+t);
            //     }, 5000 );
            // }
        },
        chooseOrgNode:function (param){
            //fromData,inputEl,buttonEl
            if(param.prepose){
                param=param.prepose(param);
                if(!param) return;
            }
            var title=param.title;
            if(!title) {
                title=this.translate("请选择组织节点",'','org-dialog');
            }

            var value=param.inputEl.val();
            param.chooseOrgNodeCallbackEvent=function(ids,nodes) {
                // debugger;
                param.inputEl.val(ids.join(","));
                console.log("ids="+ids.join(","))
                var names=[];
                for (var i = 0; i < nodes.length; i++) {
                    // debugger
                    names.push(nodes[i].name);
                }
                if (names.length>0) {
                    param.buttonEl.find("span").text(names.join(","));
                } else {
                    param.buttonEl.find("span").text(param.buttonEl.find("span").attr("default-label"));
                }
                if(param.callback) {
                    param.callback(param,{field:param.field,selectedIds:ids,selectedNodes:nodes,fromData:param.fromData,inputEl:param.inputEl,buttonEl:param.buttonEl});
                }
            }
            admin.putTempData("org-dialog-value",value,true);
            admin.putTempData("org-dialog-options",param,true);
            var dialogIndex=admin.popupCenter({
                type:2,
                id:"orgDialog",
                title: title,
                offset: 'auto',
                content: '/business/hrm/organization/org_dialog.html',
                area:["400px","80%"]
            });
            admin.putTempData("org-dialog-index",dialogIndex,true);

        },
        chooseEmployee:function (param){
            //fromData,inputEl,buttonEl
            if(param.prepose){
                param=param.prepose(param);
                if(!param) return;
            }
            var title=param.title;
            if(!title) {
                title=this.translate("请选择人员",'','dialog');
            }
            // debugger;
            var value = null;
            if(param.inputEl) {
                value = param.inputEl.val();
            }
            param.chooseEmployeeCallbackEvent=function(ids,nodes) {
                // debugger;
                if(param.inputEl) {
                    param.inputEl.val(ids.join(","));
                }
                console.log("ids="+ids.join(","))
                var names=[];
                var ns=[];
                for (var i = 0; i < nodes.length; i++) {
                    // debugger
                    names.push(nodes[i].targetName);
                    ns.push({targetId:nodes[i].targetId,targetType:nodes[i].targetType});
                }
                if (names.length>0) {
                    if(param.buttonEl) {
                        param.buttonEl.find("span").text(names.join(","));
                    }
                } else {
                    if(param.buttonEl) {
                        param.buttonEl.find("span").text(param.buttonEl.find("span").attr("default-label"));
                    }
                }
                if(param.callback) {
                    param.callback(param,{field:param.field,selectedIds:ids,selected:ns,fromData:param.fromData,inputEl:param.inputEl,buttonEl:param.buttonEl});
                }
            }
            //debugger
            admin.putTempData("employee-dialog-value",value,true);
            admin.putTempData("employee-dialog-options",param,true);

            admin.post("/service-hrm/hrm-favourite-group-item/remove-all",{temporary:1},function (r){
                var dialogIndex=admin.popupCenter({
                    type:2,
                    id:"orgDialog",
                    title: title,
                    content: '/business/hrm/employee/dialog/emp_dialog.html',
                    offset: 'auto',
                    area:["1150px","90%"]
                });
                admin.putTempData("employee-dialog-index",dialogIndex,true);
            });

        },
        fillDialogButtons:function () {
            // debugger
            this.fillOrgOrPosDialogButtons("org");
            this.fillOrgOrPosDialogButtons("pos");
            this.fillEmployeeDialogButtons("pos");
        },
        fillEmployeeDialogButtons:function () {
            // debugger
            var orgEls=$("button[action-type='emp-dialog']");
            if(orgEls.length==0) return;
            orgEls.find("i").css("opacity",0.0);
            orgEls.find("span").css("opacity",0.0);
            // debugger;
            var map={};
            //
            // var empIds=[];
            // var bpmRoleIds=[];
            //
            for (var i = 0; i <orgEls.length ; i++) {
                var orgEl=$(orgEls[i]);
                var id=orgEl.attr("id");
                id=id.substring(0,id.length-7);
                var input=$("#"+id);
                var value=input.val();
                if(!value) continue;
                if(Array.isArray(value))
                {
                    //暂不处理
                } else {
                    value.trim()
                    if(value.startWith("[") && value.endWith("]")) {
                        value=JSON.parse(value);
                        // for (var j = 0; j < value.length; j++) {
                        //     if(value[j].targetType=="employee") empIds.push(value[j].targetId);
                        //     else if(value[j].targetType=="bpm_role") bpmRoleIds.push(value[j].targetId);
                        // }
                    } else {
                        value=value.split(",");
                        // for (var j = 0; j < value.length; j++) empIds.push(value[j]);
                    }
                    map[id]=value;

                }
            }

            // map["xxx"]=["491963945599369216","491964680416264192"];

            var url="/service-hrm/hrm-favourite-group-item/get-by-id";


            admin.request(url, {id:JSON.stringify(map)},function (r){
                if(r.success) {
                    for(var id in r.data) {
                        $("#"+id+"-button").find("span").text(r.data[id].join(","));
                    }
                }
                orgEls.find("i").animate({opacity:1.0},300,"swing");  //.css("opacity",0.0);
                orgEls.find("span").animate({opacity:1.0},300,"swing");//.css("opacity",0.0);
            },"post",true);

        },
        fillOrgOrPosDialogButtons:function (type) {
            var orgEls=$("button[action-type='"+type+"-dialog']");
            if(orgEls.length==0) return;
            orgEls.find("i").css("opacity",0.0);
            orgEls.find("span").css("opacity",0.0);
            //debugger;
            var allIds=[];
            var map={};
            for (var i = 0; i <orgEls.length ; i++) {
                var orgEl=$(orgEls[i]);
                var id=orgEl.attr("id");
                id=id.substring(0,id.length-7);
                var input=$("#"+id);
                var value=input.val();
                if(!value) continue;
                if(Array.isArray(value))
                {

                } else {
                    value.trim()
                    if(value.startWith("[") && value.endWith("]")) {
                        value=JSON.parse(value);
                    } else {
                        value=value.split(",");
                    }
                    map[id]=value;
                    for (var j = 0; j < value.length; j++) {
                        allIds.push(value[j]);
                    }
                }
            }
            var url=""
            if(type=="org") {
                url="/service-hrm/hrm-organization/get-by-ids";
            }
            if(type=="pos") {
                url="/service-hrm/hrm-position/get-by-ids";
            }

            admin.request(url,allIds,function (r){
                if(r.success) {
                    var datamap={};
                    for (var i = 0; i < r.data.length; i++) {
                        datamap[r.data[i].id]=r.data[i];
                    }
                    //debugger;
                    for(var id in map) {
                        //debugger;
                        var names=[];
                        var ids=map[id];
                        for (var i = 0; i < ids.length; i++) {
                            var org=datamap[ids[i]];
                            //如果人员不存在，就用ID填充
                            if(!org) {
                                names.push(ids[i]);
                                continue;
                            }
                            var name=org.fullName;
                            if(org.shortName) {
                                name=org.shortName;
                            }
                            names.push(name);
                        }
                        $("#"+id+"-button").find("span").text(names.join(","));

                    }
                }
                orgEls.find("i").animate({opacity:1.0},300,"swing");  //.css("opacity",0.0);
                orgEls.find("span").animate({opacity:1.0},300,"swing");//.css("opacity",0.0);
            },"post",true);
        },
        compareDirtyFields(before,after) {
            if(before==null || after == null) return null;
            var fields=[];
            // 用 before 去覆盖 after
            for (var key in before) {
                var bval=before[key];
                var aval=after[key];
                if(bval==null && aval==null) {
                    //无变化
                } else  if(bval!=null && aval==null) {
                    //被修改为null
                    fields.push(key);
                } else  if(bval==null && aval!=null) {
                    //之前为null，被赋值
                    fields.push(key);
                } else  if(bval!=null && aval!=null) {
                    //前后都有值，被修改了
                    if(bval!=aval) {
                        fields.push(key);
                    }
                }
            }
            // 用 after 去覆盖 before
            for (var key in after) {
                //debugger;
                if(fields.indexOf(key)>-1) continue;
                var bval=before[key];
                var aval=after[key];
                if(bval==null && aval==null) {
                    //无变化
                } else  if(bval!=null && aval==null) {
                    //被修改为null
                    fields.push(key);
                } else  if(bval==null && aval!=null) {
                    //之前为null，被赋值
                    fields.push(key);
                } else  if(bval!=null && aval!=null) {
                    //前后都有值，被修改了
                    if(bval!=aval) {
                        fields.push(key);
                    }
                }
            }
            return fields.join(",");
        },
        //表单提交
        submit: function (url, params, method, callback) {
            // debugger
            if (!method) method = "post";
            url = url.replace("//", "/");
            url = url.replace("http:/", "http://");
            url = url.replace("https:/", "https://");

            var waitingTask = setTimeout(function () {
                top.layer.load(2);
            }, 1000);

            var tag="download_tag_"+Math.ceil(Math.random()*(new Date()).getTime());
            top[tag]=function (err) {
                clearTimeout(waitingTask);
                top.layer.closeAll('loading');
                if(err && TypeUtil.isString(err)) {
                    err=JSON.parse(err);
                }
                if(callback) {
                    callback(err);
                } else {
                    top.layer.msg(err.message, {icon: 2, time: 2000});
                }
                delete top[tag];
            }

            window[tag+"_onload"]=function (ifr) {}
            window[tag+"_onerror"]=function (ifr) {
                debugger
            }

            var target = "t-" + (new Date()).getTime();
            var $ifr = $("<div style='position: absolute;left: 0px;top:0px;z-index: 100000'><iframe id='" + target + "' name='" + target + "' style='display:block;width: 100%height:200px' onload='"+tag+"_onload(this)' onerror='"+tag+"_onerror(this)'></iframe></div>")
            $("body").append($ifr);
            // 构造隐藏的form表单
            var $form = $("<form style='display:none' method='" + method + "' target='" + target + "' action='" + url + "'></form>");
            $("body").append($form);
            //添加参数
            if (!params) params = {};

            params.downloadTag=tag;

            for (var p in params) {
                var $input = $("<input name='" + p + "' type='text' value='" + params[p] + "'></input>");
                $form.append($input);
            }
            // 提交表单
            $form.submit();

            //var ifr = document.getElementById(target);
            // var timer = setInterval(function () {
            //     var doc = ifr.contentDocument || ifr.contentWindow.document;
            //     // Check if loading is complete
            //     // var cTag=getCookie(tag);
            //     // debugger;
            //     // if ((doc.readyState == 'complete' || doc.readyState == 'interactive') && cTag=="success") {
            //     if (doc.readyState == 'complete' || doc.readyState == 'interactive') {
            //         // do something
            //         layer.closeAll('loading');
            //         //clearTimeout(task);
            //         //clearInterval(timer);
            //         setTimeout(function () {
            //             //$form.remove();
            //             //$ifr.remove();
            //             // delete top[tag];
            //         }, 1000 * 60 * 10);
            //         console.log("dl:"+doc.innerHTML);
            //         callback && callback({success:true,message:"下载成功"});
            //         // debugger;
            //     }
            // }, 1000);

            //移除元素
            // setTimeout(function(){
            // 	$form.remove();
            // 	$ifr.remove();
            // },1000);
        },
        showMessage:function (result) {
            // debugger
            if(TypeUtil.isString(result)) {
                top.layer.msg(result, {icon: 1, time: 2000});
                return;
            }

            var langctx=null;
            if(result.extra) {
                langctx=result.extra.languageContext;
            }

            var message=this.translate(result.message,null,langctx?langctx:'result:message');
            if(result.subject) {
                message=this.translate(result.subject,null,langctx?langctx:'result:subject')+message;
            }
            if(!message) return;
            var messageLevel=null;
            if(result.extra) {
                messageLevel=result.extra.messageLevel;
            }

            if(result.errors!=null && result.errors.length>0) {
                var errs=[];
                for (var i=0;i<result.errors.length;i++) {
                    var e=result.errors[i];
                    if(e.subject && e.message) {
                        errs.push("&nbsp;&nbsp;"+(i+1)+"."+this.translate(e.subject,null,langctx?langctx:'result:subject') +" : "+ this.translate(e.message));
                    } else  if(!e.subject && e.message) {
                        errs.push("&nbsp;&nbsp;"+(i+1)+"."+this.translate(e.message,null,langctx?langctx:'result:message'));
                    } else  if(e.subject && !e.message) {
                        errs.push("&nbsp;&nbsp;"+(i+1)+"."+this.translate(e.subject,null,langctx?langctx:'result:subject'));
                    }
                }
                if(errs.length>0) {
                    message+="<br><span style='font-weight: bold'>"+this.translate("错误详情",'',langctx?langctx:'dialog')+"  : </span><br>"+errs.join("<br>");
                }
            }

            if(result.solutions!=null && result.solutions.length>0) {
                var errs=[];
                for (var i=0;i<result.solutions.length;i++) {
                    var solution=result.solutions[i];
                    errs.push("&nbsp;&nbsp;"+(errs.length+1)+"."+this.translate(solution,null,langctx?langctx:'result:solution'));
                }
                if(errs.length>0) {
                    message+="<br><span style='font-weight: bold'>"+this.translate("解决方案",'',langctx?langctx:'dialog')+" :</span> <br>"+errs.join("<br>");
                }
            }


            var success=result.success;
            var icon=null;
            var time=null;
            // 设置相应成功与失败的
            if(success) {
                icon = 1;
                if(messageLevel==null) {
                    messageLevel = "notify";
                }
            } else {
                icon = 2;
                if(messageLevel==null) {
                    messageLevel = "read";
                }
            }

            if(messageLevel=="none") return;
            if(messageLevel=="notify") {
                // 每 20 个字1秒
                time= (message.length*1000/20);
                if(time<2000) time=500;
                if(time>=2000 && time<4000) time=2000;
                if(time>4000) {
                    messageLevel="confirm";
                }
            }
            if(messageLevel=="read") {
                // 每 20 个字1秒
                time= (message.length*1000/20);
                if(time<2000) time=2000;
                if(time>8000) {
                    messageLevel="confirm";
                }
            }
            //
            if(messageLevel=="confirm") {
                top.layer.open({
                    icon: icon,
                    title: this.translate('提示信息','',langctx?langctx:'dialog'),
                    content: message
                });
            } else {
                top.layer.msg(message, {icon: icon, time: time});
            }
        }
    };

    var mouseDownTime;
    var mouseDownTarget;
    var mouseX;
    $(document).on("mousedown", function (e) {
        mouseDownTime = (new Date()).getTime();
        mouseX=e.screenX;
        mouseDownTarget=e.target;
    });
    /**
     * 监听layui table 的列宽拖动时间
     * */
    $(document).on("mouseup", function (e) {
        //console.log(1)
        var t = (new Date()).getTime();
        t = t - mouseDownTime;
        //console.log("click",t);
        mouseDownTime = null;
        //if (t < 10) return;
        //通过位移来判断，最好是通过对比数值的方式，如果有变化就保存
        if(Math.abs(mouseX-e.screenX)==0) {
            return;
        }
        mouseX=-1;
        //console.log(2)
        setTimeout(function () {

            var tar = $(mouseDownTarget);
            // debugger
            if (tar.parent().length == 0) return;
            if (tar.parent()[0].nodeName != "TH") return;
            //var table=tar.parents("table");
            //debugger
            var cls = tar.attr("class");
            if (cls == null) return;
            //console.log(cls,t);
            var pars = tar.parents();
            var layFilter = null;

            var tableIndex = -1;
            for (var i = 0; i < pars.length; i++) {
                var p = $(pars[i]);
                layFilter = p.attr("lay-filter");
                if (layFilter && layFilter.startWith("LAY-table-")) {
                    tableIndex = layFilter.split("-")[2];
                    break;
                }
            }
            if (tableIndex == -1) return;

            var inst = table.instance[tableIndex - 1];
            var tableId = inst.config.elem[0].id;
            var cols = inst.config.cols[0];
            if (cls.indexOf("layui-table-cell") == -1 || cls.indexOf("laytable-cell-") == -1) return;
            var ths = $("th .layui-table-cell");
            saveTableSettings4UI(tableId,ths,cols);

        }, 100);

    });

    function saveTableSettings4UI(tableId,ths,cols) {
        //
        var ws = {};
        for (var i = 0; i < ths.length; i++) {
            var th = $(ths[i]);
            var cls=th.parent().attr("class");

            var hide=cls.indexOf("layui-hide")!=-1;
            //debugger;
            if (cols[i] && cols[i].field) {
                var w=th[0].clientWidth;
                if(hide) w=cols[i].title.length * 15 + 50;
                cols[i].width = w;
                var cfg={width:w,hide:hide};
                cfg.title=cols[i].title;
                cfg.orderIndex=cols[i].orderIndex;
                ws[cols[i].field] = cfg;
            }
        }

        saveTableSettings(tableId,ws);
    }

    function saveTableSettings(tableId,ws) {
        var loc = location.href;
        loc = loc.substr(loc.indexOf("//") + 2);
        loc = loc.substr(loc.indexOf("/"));
        if(loc.indexOf("?")>0) {
            loc = loc.substr(0,loc.indexOf("?"));
        }
        console.log("save table", tableId, ws);
        admin.request("/service-system/sys-db-cache/save", {
            value: JSON.stringify(ws),
            area: loc+"#"+tableId,
            catalog: "layui-table-column-width",
            ownerType: "user"
        }, function (data) {
            if(admin.toast()) {
                admin.toast().success("自定义表格设置已同步", {time: 1000, position: "right-bottom",title:"提示"});
            }
        });
    }

    window.saveTableSettings4UI=saveTableSettings4UI;


    // foxnic 提供的事件
    foxnic.events = {};

    // 打开表格自定义窗口
    window.openTableCustomDialog = function (li,options,it) {
        // it.resize();
        li=$(li);
        li.parent().hide();
        //
        // console.log(JSON.stringify(options.cols[0]));
        admin.putVar("custom-table-options",options);
        admin.putVar("custom-table-apply-function",function(configs) {

            var colsToApply = options.cols[0];
            // debugger
            for (var i = 0; i < colsToApply.length; i++) {
                var col=colsToApply[i];
                var field=col.field;
                var cfg=configs.columns[field];
                if(!cfg) continue;
                col.hide=cfg.hide;
                col.title=cfg.title;
                col.width=cfg.width;
                // col.fixed=cfg.fixed;
                col.orderIndex=parseInt(cfg.orderIndex);
                if(isNaN(col.orderIndex)) col.orderIndex=1024;
                // 前两列不考虑
                col.orderIndex+=2;
            }
            colsToApply.sort(function (a,b){
                return a.orderIndex-b.orderIndex;
            });

            it.reload(options.id,options);

            saveTableSettings(options.id,configs.columns)


        });
        admin.popupCenter({
            title: "自定义表格",
            resize: false,
            //offset: [auto,null],
            area: [(630+18)+"px","600px"],
            type: 2,
            id: "table-custom-dialog",
            content: '/business/system/layui/table_custom_dialog.html',
            finish: function () {
                //alert("finish");
            }
        });

    }


    //图片预览支持
    window.previewImage = function (obj) {
        if (window != top) {
            top.previewImage(obj);
            return;
        }
        //只有明确不允许查看，菜不查看，否则就可以查看
        if($(obj).attr("can-preview")=="no") return;

        var it = $(obj).parent();
        //var id=it.attr("id");
        layer.photos({
            shade: 0.6,
            photos: it,
            anim: 5
        });
        return;

        var fileType = it.attr("fileType");
        if (!fileType.startWith("image/")) {
            return;
        }
        var src = obj.src;
        //debugger
        if (src.endWith("?id=undefined") || src.endWith("?id=null") || src.endWith("?id=")) return;
        if (src.indexOf("no-image") != -1) return;
        var img = new Image();
        img.src = obj.src;
        img.onload = function () {
            //debugger

            var fullHeight = $(window).height();
            var fullWidth = $(window).width();
            var ih = img.height + 50 + 2
            var iw = img.width;
            if (ih > fullHeight) {
                ih = (fullHeight - 50 - 2) * 0.9;
                iw = (img.width / img.height) * ih;
            }
            if (iw > fullWidth) {
                iw = fullWidth * 0.9;
                ih = (img.height / img.width) * iw;
            }

            var height = ih + 50 + 2; //获取图片高度
            var width = iw; //获取图片宽度


            var imgHtml = "<img src='" + obj.src + "' style='width: " + iw + "px;height: " + ih + "px' />";
            //弹出层
            layer.open({
                type: 1,
                shade: 0.8,
                offset: 'auto',
                area: [width + 'px', height + 'px'],
                shadeClose: true,//点击外围关闭弹窗
                scrollbar: false,//不现实滚动条
                title: "图片预览", //不显示标题
                content: imgHtml, //捕获的元素，注意：最好该指定的元素要存放在body最外层，否则可能被其它的相对元素所影响
                cancel: function () {
                    //layer.msg('捕获就是从页面已经存在的元素上，包裹layer的结构', { time: 5000, icon: 6 });
                }
            });
        }

    }

    window.fox=foxnic;

    // top.translate=foxnic.translate;
    // debugger

    exports('foxnic', foxnic);

});



