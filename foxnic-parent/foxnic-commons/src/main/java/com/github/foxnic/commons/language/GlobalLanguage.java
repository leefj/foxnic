package com.github.foxnic.commons.language;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.reflect.EnumUtil;

/**
 * 全球语言
 * */
public enum GlobalLanguage implements CodeTextEnum {

	af("af","南非语"),
	af_za("af-ZA","南非语"),
	ar("ar","阿拉伯语"),
	ar_ae("ar-AE","阿拉伯语(阿联酋)"),
	ar_bh("ar-BH","阿拉伯语(巴林)"),
	ar_dz("ar-DZ","阿拉伯语(阿尔及利亚)"),
	ar_eg("ar-EG","阿拉伯语(埃及)"),
	ar_iq("ar-IQ","阿拉伯语(伊拉克)"),
	ar_jo("ar-JO","阿拉伯语(约旦)"),
	ar_kw("ar-KW","阿拉伯语(科威特)"),
	ar_lb("ar-LB","阿拉伯语(黎巴嫩)"),
	ar_ly("ar-LY","阿拉伯语(利比亚)"),
	ar_ma("ar-MA","阿拉伯语(摩洛哥)"),
	ar_om("ar-OM","阿拉伯语(阿曼)"),
	ar_qa("ar-QA","阿拉伯语(卡塔尔)"),
	ar_sa("ar-SA","阿拉伯语(沙特阿拉伯)"),
	ar_sy("ar-SY","阿拉伯语(叙利亚)"),
	ar_tn("ar-TN","阿拉伯语(突尼斯)"),
	ar_ye("ar-YE","阿拉伯语(也门)"),
	az("az","阿塞拜疆语"),
	az_az("az-AZ","阿塞拜疆语(拉丁文)"),
	//az_az("az-AZ","阿塞拜疆语(西里尔文)"),
	be("be","比利时语"),
	be_by("be-BY","比利时语"),
	bg("bg","保加利亚语"),
	bg_bg("bg-BG","保加利亚语"),
	bs_ba("bs-BA","波斯尼亚语(拉丁文，波斯尼亚和黑塞哥维那)"),
	ca("ca","加泰隆语"),
	ca_es("ca-ES","加泰隆语"),
	cs("cs","捷克语"),
	cs_cz("cs-CZ","捷克语"),
	cy("cy","威尔士语"),
	cy_gb("cy-GB","威尔士语"),
	da("da","丹麦语"),
	da_dk("da-DK","丹麦语"),
	de("de","德语"),
	de_at("de-AT","德语(奥地利)"),
	de_ch("de-CH","德语(瑞士)"),
	de_de("de-DE","德语(德国)"),
	de_li("de-LI","德语(列支敦士登)"),
	de_lu("de-LU","德语(卢森堡)"),
	dv("dv","第维埃语"),
	dv_mv("dv-MV","第维埃语"),
	el("el","希腊语"),
	el_gr("el-GR","希腊语"),
	en("en","英语"),
	en_au("en-AU","英语(澳大利亚)"),
	en_bz("en-BZ","英语(伯利兹)"),
	en_ca("en-CA","英语(加拿大)"),
	en_cb("en-CB","英语(加勒比海)"),
	en_gb("en-GB","英语(英国)"),
	en_ie("en-IE","英语(爱尔兰)"),
	en_jm("en-JM","英语(牙买加)"),
	en_nz("en-NZ","英语(新西兰)"),
	en_ph("en-PH","英语(菲律宾)"),
	en_tt("en-TT","英语(特立尼达)"),
	en_us("en-US","英语(美国)"),
	en_za("en-ZA","英语(南非)"),
	en_zw("en-ZW","英语(津巴布韦)"),
	eo("eo","世界语"),
	es("es","西班牙语"),
	es_ar("es-AR","西班牙语(阿根廷)"),
	es_bo("es-BO","西班牙语(玻利维亚)"),
	es_cl("es-CL","西班牙语(智利)"),
	es_co("es-CO","西班牙语(哥伦比亚)"),
	es_cr("es-CR","西班牙语(哥斯达黎加)"),
	es_do("es-DO","西班牙语(多米尼加共和国)"),
	es_ec("es-EC","西班牙语(厄瓜多尔)"),
	es_es("es-ES","西班牙语(传统)"),
	// es_es("es-ES","西班牙语(国际)"),
	es_gt("es-GT","西班牙语(危地马拉)"),
	es_hn("es-HN","西班牙语(洪都拉斯)"),
	es_mx("es-MX","西班牙语(墨西哥)"),
	es_ni("es-NI","西班牙语(尼加拉瓜)"),
	es_pa("es-PA","西班牙语(巴拿马)"),
	es_pe("es-PE","西班牙语(秘鲁)"),
	es_pr("es-PR","西班牙语(波多黎各(美))"),
	es_py("es-PY","西班牙语(巴拉圭)"),
	es_sv("es-SV","西班牙语(萨尔瓦多)"),
	es_uy("es-UY","西班牙语(乌拉圭)"),
	es_ve("es-VE","西班牙语(委内瑞拉)"),
	et("et","爱沙尼亚语"),
	et_ee("et-EE","爱沙尼亚语"),
	eu("eu","巴士克语"),
	eu_es("eu-ES","巴士克语"),
	fa("fa","法斯语"),
	fa_ir("fa-IR","法斯语"),
	fi("fi","芬兰语"),
	fi_fi("fi-FI","芬兰语"),
	fo("fo","法罗语"),
	fo_fo("fo-FO","法罗语"),
	fr("fr","法语"),
	fr_be("fr-BE","法语(比利时)"),
	fr_ca("fr-CA","法语(加拿大)"),
	fr_ch("fr-CH","法语(瑞士)"),
	fr_fr("fr-FR","法语(法国)"),
	fr_lu("fr-LU","法语(卢森堡)"),
	fr_mc("fr-MC","法语(摩纳哥)"),
	gl("gl","加里西亚语"),
	gl_es("gl-ES","加里西亚语"),
	gu("gu","古吉拉特语"),
	gu_in("gu-IN","古吉拉特语"),
	he("he","希伯来语"),
	he_il("he-IL","希伯来语"),
	hi("hi","印地语"),
	hi_in("hi-IN","印地语"),
	hr("hr","克罗地亚语"),
	hr_ba("hr-BA","克罗地亚语(波斯尼亚和黑塞哥维那)"),
	hr_hr("hr-HR","克罗地亚语"),
	hu("hu","匈牙利语"),
	hu_hu("hu-HU","匈牙利语"),
	hy("hy","亚美尼亚语"),
	hy_am("hy-AM","亚美尼亚语"),
	id("id","印度尼西亚语"),
	id_id("id-ID","印度尼西亚语"),
	is("is","冰岛语"),
	is_is("is-IS","冰岛语"),
	it("it","意大利语"),
	it_ch("it-CH","意大利语(瑞士)"),
	it_it("it-IT","意大利语(意大利)"),
	ja("ja","日语"),
	ja_jp("ja-JP","日语"),
	ka("ka","格鲁吉亚语"),
	ka_ge("ka-GE","格鲁吉亚语"),
	kk("kk","哈萨克语"),
	kk_kz("kk-KZ","哈萨克语"),
	kn("kn","卡纳拉语"),
	kn_in("kn-IN","卡纳拉语"),
	ko("ko","朝鲜语"),
	ko_kr("ko-KR","朝鲜语"),
	kok("kok","孔卡尼语"),
	kok_in("kok-IN","孔卡尼语"),
	ky("ky","吉尔吉斯语"),
	ky_kg("ky-KG","吉尔吉斯语(西里尔文)"),
	lt("lt","立陶宛语"),
	lt_lt("lt-LT","立陶宛语"),
	lv("lv","拉脱维亚语"),
	lv_lv("lv-LV","拉脱维亚语"),
	mi("mi","毛利语"),
	mi_nz("mi-NZ","毛利语"),
	mk("mk","马其顿语"),
	mk_mk("mk-MK","马其顿语(FYROM)"),
	mn("mn","蒙古语"),
	mn_mn("mn-MN","蒙古语(西里尔文)"),
	mr("mr","马拉地语"),
	mr_in("mr-IN","马拉地语"),
	ms("ms","马来语"),
	ms_bn("ms-BN","马来语(文莱达鲁萨兰)"),
	ms_my("ms-MY","马来语(马来西亚)"),
	mt("mt","马耳他语"),
	mt_mt("mt-MT","马耳他语"),
	nb("nb","挪威语(伯克梅尔)"),
	nb_no("nb-NO","挪威语(伯克梅尔)(挪威)"),
	nl("nl","荷兰语"),
	nl_be("nl-BE","荷兰语(比利时)"),
	nl_nl("nl-NL","荷兰语(荷兰)"),
	nn_no("nn-NO","挪威语(尼诺斯克)(挪威)"),
	ns("ns","北梭托语"),
	ns_za("ns-ZA","北梭托语"),
	pa("pa","旁遮普语"),
	pa_in("pa-IN","旁遮普语"),
	pl("pl","波兰语"),
	pl_pl("pl-PL","波兰语"),
	pt("pt","葡萄牙语"),
	pt_br("pt-BR","葡萄牙语(巴西)"),
	pt_pt("pt-PT","葡萄牙语(葡萄牙)"),
	qu("qu","克丘亚语"),
	qu_bo("qu-BO","克丘亚语(玻利维亚)"),
	qu_ec("qu-EC","克丘亚语(厄瓜多尔)"),
	qu_pe("qu-PE","克丘亚语(秘鲁)"),
	ro("ro","罗马尼亚语"),
	ro_ro("ro-RO","罗马尼亚语"),
	ru("ru","俄语"),
	ru_ru("ru-RU","俄语"),
	sa("sa","梵文"),
	sa_in("sa-IN","梵文"),
	se("se","北萨摩斯语"),
	se_fi("se-FI","北萨摩斯语(芬兰)"),
	// se_fi("se-FI","斯科特萨摩斯语(芬兰)"),
	// se_fi("se-FI","伊那里萨摩斯语(芬兰)"),
	se_no("se-NO","北萨摩斯语(挪威)"),
	// se_no("se-NO","律勒欧萨摩斯语(挪威)"),
	// se_no("se-NO","南萨摩斯语(挪威)"),
	se_se("se-SE","北萨摩斯语(瑞典)"),
	// se_se("se-SE","律勒欧萨摩斯语(瑞典)"),
	// se_se("se-SE","南萨摩斯语(瑞典)"),
	sk("sk","斯洛伐克语"),
	sk_sk("sk-SK","斯洛伐克语"),
	sl("sl","斯洛文尼亚语"),
	sl_si("sl-SI","斯洛文尼亚语"),
	sq("sq","阿尔巴尼亚语"),
	sq_al("sq-AL","阿尔巴尼亚语"),
	sr_ba("sr-BA","塞尔维亚语(拉丁文，波斯尼亚和黑塞哥维那)"),
	// sr_ba("sr-BA","塞尔维亚语(西里尔文，波斯尼亚和黑塞哥维那)"),
	sr_sp("sr-SP","塞尔维亚(拉丁)"),
	// sr_sp("sr-SP","塞尔维亚(西里尔文)"),
	sv("sv","瑞典语"),
	sv_fi("sv-FI","瑞典语(芬兰)"),
	sv_se("sv-SE","瑞典语"),
	sw("sw","斯瓦希里语"),
	sw_ke("sw-KE","斯瓦希里语"),
	syr("syr","叙利亚语"),
	syr_sy("syr-SY","叙利亚语"),
	ta("ta","泰米尔语"),
	ta_in("ta-IN","泰米尔语"),
	te("te","泰卢固语"),
	te_in("te-IN","泰卢固语"),
	th("th","泰语"),
	th_th("th-TH","泰语"),
	tl("tl","塔加路语"),
	tl_ph("tl-PH","塔加路语(菲律宾)"),
	tn("tn","茨瓦纳语"),
	tn_za("tn-ZA","茨瓦纳语"),
	tr("tr","土耳其语"),
	tr_tr("tr-TR","土耳其语"),
	ts("ts","宗加语"),
	tt("tt","鞑靼语"),
	tt_ru("tt-RU","鞑靼语"),
	uk("uk","乌克兰语"),
	uk_ua("uk-UA","乌克兰语"),
	ur("ur","乌都语"),
	ur_pk("ur-PK","乌都语"),
	uz("uz","乌兹别克语"),
	uz_uz("uz-UZ","乌兹别克语(拉丁文)"),
	// uz_uz("uz-UZ","乌兹别克语(西里尔文)"),
	vi("vi","越南语"),
	vi_vn("vi-VN","越南语"),
	xh("xh","班图语"),
	xh_za("xh-ZA","班图语"),
	zh("zh","中文"),
	zh_cn("zh-CN","中文(简体)"),
	zh_hk("zh-HK","中文(香港)"),
	zh_mo("zh-MO","中文(澳门)"),
	zh_sg("zh-SG","中文(新加坡)"),
	zh_tw("zh-TW","中文(繁体)"),
	zu("zu","祖鲁语"),
	zu_za("zu-ZA","祖鲁语");



	private String text;

	private String code;

	private GlobalLanguage(String code,String text)  {
		this.code=code;
		this.text=text;
	}

	public String code() {
		return this.code;
	}

	public String text() {
		return text;
	}

	public static GlobalLanguage parseByCode(String code) {
		GlobalLanguage language=(GlobalLanguage) EnumUtil.parseByCode(GlobalLanguage.values(),code);
		if(language==null) {
			language=GlobalLanguage.valueOf(code);
		}
		return language;
	}

}
