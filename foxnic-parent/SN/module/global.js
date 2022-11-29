
if(window==top) {
	// debugger
	var defaultsLangs = null, codeLangs = null, language = null;

	window.translate = function (defaults, code, context) {
		// debugger
		if (!context) context = "defaults";

		if (!language) {
			language = localStorage.getItem("lang");
		}

		// debugger
		if( (defaultsLangs ==null || defaultsLangs==null) &&  window.LANGUAGE_DATA && window.LANGUAGE_DATA.length>0) {
			initLanguageItems(window.LANGUAGE_DATA);
		}

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


		if(defaults.indexOf("网络连接")!=-1) {
			console.log(JSON.stringify(defaultsLangs));
			for (var key in defaultsLangs) {
				if (key.indexOf("网络连接") != -1) {
					debugger
				}
			}
		}

		var item = defaultsLangs[context + ":" + defaults];
		var text = null;
		if (!item && code) {
			item = codeLangs[context + ":" + code];
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
		if (!item && window.admin) {
			// debugger
			window.admin.request("/service-system/sys-lang/insert", {
				code: code,
				defaults: defaults,
				context: context
			}, function (data) {
				localStorage.removeItem("language_timestamp");
			});
		}

		return text ? text : "--";
	};

	function loadLanguageItems() {

		// 本地开发时，清除本地缓存
		if(location.href.indexOf("127.0.0.1")>0 || location.href.indexOf("localhost")>0) {
			// 清除多语言数据
			localStorage.removeItem("language_codeLangs");
			localStorage.removeItem("language_defaultsLangs");
			localStorage.removeItem("language_timestamp");
		}

		if (!language) {
			language = localStorage.getItem("lang");
		}



		// var languageTimestamp = localStorage.getItem("language_timestamp");

		//加载语言
		// if (languageTimestamp) {

		// var expire = ((new Date()).getTime() - languageTimestamp) / 1000;

		// if (expire < 60 * 15) {
		codeLangs = localStorage.getItem("language_codeLangs");
		if (codeLangs && codeLangs.length > 2) {
			codeLangs = JSON.parse(codeLangs);
		}

		defaultsLangs = localStorage.getItem("language_defaultsLangs");
		if (defaultsLangs && defaultsLangs.length > 2) {
			defaultsLangs = JSON.parse(defaultsLangs);
		}


		// 	}
		//
		// }

		if ((defaultsLangs == null || codeLangs == null)) {
			if (window.admin) {
				window.admin.request('/service-system/sys-lang/query-list', {lang: language}, function (data) {
					// debugger
					if (!data.success) return;
					data = data.data;
					initLanguageItems(data);
				}, "POST", true);
			} else {
				setTimeout(loadLanguageItems,1);
			}
		}
	}

	function initLanguageItems(data) {
		//var t0=(new Date()).getTime()
		codeLangs = {};
		defaultsLangs = {};
		for (var i = 0; i < data.length; i++) {
			// debugger
			// if (data[i].defaults.indexOf("网络连接") != -1) {
			// 	debugger
			// }
			codeLangs[data[i].context + ":" + data[i].code] = data[i];
			defaultsLangs[data[i].context + ":" + data[i].defaults] = data[i];
		}
		//var t1=(new Date()).getTime()
		localStorage.setItem("language_codeLangs", JSON.stringify(codeLangs));
		localStorage.setItem("language_defaultsLangs", JSON.stringify(defaultsLangs));
		localStorage.setItem("language_timestamp", (new Date()).getTime());
		//var t2=(new Date()).getTime()
		//console.log("T",t1-t0,t2-t1);
	}



	// loadLanguageItems();




} else {
	// debugger
	window.translate = function (defaults, code, context) {
		return top.translate(defaults, code, context);
	}
}




function Log() {}

Log.prototype.type = ["primary", "success", "warn", "error", "info"];

Log.prototype.typeColor = function(type) {
	let color = "";
	switch (type) {
		case "primary":
			color = "#2d8cf0";
			break;
		case "success":
			color = "#19be6b";
			break;
		case "info":
			color = "#909399";
			break;
		case "warn":
			color = "#ff9900";
			break;
		case "error":
			color = "#f03f14";
			break;
		default:
			color = "#35495E";
			break;
	}
	return color;
};

Log.prototype.isArray = function(obj) {
	return Object.prototype.toString.call(obj) === "[object Array]";
};

Log.prototype.print = function(text, textColor, background) {
	if (typeof text === "object") {
		// 如果是對象則調用打印對象方式
		this.isArray(text) ? console.table(text) : console.dir(text);
		return;
	}
	if(!textColor) {
		textColor=this.typeColor("primary");
	}
	if (background) {
		// 如果是打印帶背景的
		console.log(`%c ${text} `, `background:${background}; padding: 2px; border-radius: 4px; color: ${textColor};`);
	} else {
		console.log(
			`%c ${text} `,
			`border: 1px solid ${textColor}; padding: 2px; border-radius: 4px; color: ${textColor};`
		);
	}
};


Log.prototype.pretty = function(type = "primary", title, text,total) {

	if(TypeUtil.isNumber(title)) {
		if (title < 10) title = "0" + title;
	}

	if (typeof text === "object") {
		var content = JSON.stringify(text);
		console.log(
			`%c ${title} %c ${content} %c`,
			`background:${this.typeColor(type)};border:1px solid ${this.typeColor(type)}; padding: 1px; border-radius: 4px 0 0 4px; color: #fff;`,
			`border:1px solid ${this.typeColor(type)}; padding: 1px; border-radius: 0 4px 4px 0; color: ${this.typeColor(type)};`,
			"background:transparent"
		);
		this.isArray(text) ? console.table(text) : console.dir(text);
	} else {
		console.log(
			`%c ${title} %c ${text} %c`,
			`background:${this.typeColor(type)};border:1px solid ${this.typeColor(type)}; padding: 1px; border-radius: 4px 0 0 4px; color: #fff;`,
			`border:1px solid ${this.typeColor(type)}; padding: 1px; border-radius: 0 4px 4px 0; color: ${this.typeColor(type)};`,
			"background:transparent"
		);
	}
};

Log.prototype.log = function(title, ...text) {
	if(text.length<=1) {
		this.pretty("primary", "Log:"+title, text[0],text.length);
		return;
	}
	console.group("Foxnic-Web-Log:"+title);
	var i=0;
	text.forEach(t => {
		i++;
		this.pretty("primary", i, t,text.length);
	});
	console.groupEnd();
};

Log.prototype.success = function(title, ...text) {
	if(text.length<=1) {
		this.pretty("success", "Success:"+title, text[0],text.length);
		return;
	}
	console.group("Foxnic-Web-Success:"+title);
	var i=0;
	text.forEach(t => {
		i++;
		this.pretty("success", i, t,text.length);
	});
	console.groupEnd();
};

Log.prototype.warn = function(title, ...text) {
	if(text.length<=1) {
		this.pretty("warn", "Warn:"+title, text[0],text.length);
		return;
	}
	console.group("Foxnic-Web-Warn:"+title);
	var i=0;
	text.forEach(t => {
		i++;
		this.pretty("warn", i, t,text.length);
	});
	console.groupEnd();
};

Log.prototype.error = function(title, ...text) {
	if(text.length<=1) {
		this.pretty("error", "Error:"+title, text[0],text.length);
		return;
	}
	console.group("Foxnic-Web-Error:"+title);
	var i=0;
	text.forEach(t => {
		i++;
		this.pretty("error", i, t,text.length);
	});
	console.groupEnd();
};

Log.prototype.info = function(title, ...text) {
	if(text.length<=1) {
		this.pretty("info", "Info:"+title, text[0],text.length);
		return;
	}
	console.group("Foxnic-Web-Info:"+title);
	var i=0;
	text.forEach(t => {
		i++;
		this.pretty("info", i, t,text.length);
	});
	console.groupEnd();
};

window.logger=new Log();


QueryString = {
	data: {},
	initial: function() {
		var aPairs, aTmp;
		var queryString = new String(window.location.search);
		queryString = queryString.substr(1, queryString.length); //remove   "?"
		aPairs = queryString.split("&");
		for (var i = 0; i < aPairs.length; i++) {
			aTmp = aPairs[i].split("=");
			this.data[aTmp[0]] = aTmp[1];
		}
	},
	get: function(key) {
		if(!this.data[key]) return null;
		return this.data[key];
	}
}
QueryString.initial();

Cookie = {
	set:function(name,value,seconds) {
		var expireAt = new Date();
		expireAt.setTime(expireAt.getTime()+(seconds*1000));
		var expires = "expires="+d.toGMTString();
		document.cookie = name + "=" + value + "; " + expires;
	},
	get:function(name) {
		name = name + "=";
		var ca = document.cookie.split(';');
		for(var i=0; i<ca.length; i++) {
			var c = ca[i].trim();
			if (c.indexOf(name)==0) return c.substring(name.length,c.length);
		}
		return null;
	}
}

TypeUtil={
	isArray:function (data) {
		return Array.isArray(data);
	},
	isNumber:function (data) {
		if (parseFloat(data).toString() == 'NaN') {
			return false;
		} else {
			return true;
		}
	},
	isInteger:function (data) {
		return Number.isInteger(data);
	},
	isObject:function (data){
		if(!data) return false;
		// return data.constructor === Object;
		return Object.prototype.toString.call(data) === '[object Object]'
	},
	isString:function (data) {
		return typeof(data)=='string';
	}

}


String.prototype.endWith=function(str){
	if(str==null||str==""||this.length==0||str.length>this.length)
		return false;
	if(this.substring(this.length-str.length)==str)
		return true;
	else
		return false;
	return true;
}

String.prototype.startWith=function(str) {
	if(str==null||str==""||this.length==0||str.length>this.length)
		return false;
	if(this.substr(0,str.length)==str)
		return true;
	else
		return false;
	return true;
}

Array.prototype.removeAt = function(index) {
	if (isNaN(index) || index >= this.length) {
		return false;
	}
	this.splice(index, 1);
};

//debugger;
var layuiPath="/assets/libs/layui/";
var apiServerUrl=function () {
	var href=location.href;
	var i=href.indexOf("//");
	i=href.indexOf("/",i+2);
	if(i==-1) {
		i=href.length;
	} else {
		i++;
	}
	href =  href.substring(0,i-1);
	return href;
} ();
var apiurls={
	storage: {
		upload: "/service-storage/sys-file/upload",
		download: "/service-storage/sys-file/download",
		image: "/service-storage/sys-file/image",
		remove: "/service-storage/sys-file/delete",
		getById:"/service-storage/sys-file/get-by-id",
		getByIds:"/service-storage/sys-file/get-by-ids"
	}
}

var Theme={
	index:{
		//顶部菜单文本颜色
		headerMenuTextColor:"#333333",
		//顶部菜单文本选中的颜色
		headerMenuSelectedTextColor:"#009688",
		//顶部菜单文本颜色
		headerMenuDropDownTextColor:"#333333"
	},
	badgeStyles:["layui-badge foxnic-table-badge","layui-badge layui-bg-green foxnic-table-badge","layui-badge layui-bg-cyan foxnic-table-badge","layui-badge layui-bg-blue foxnic-table-badge"]
};




logger.log("layuiPath",layuiPath)
logger.log("apiServerUrl",apiServerUrl)


