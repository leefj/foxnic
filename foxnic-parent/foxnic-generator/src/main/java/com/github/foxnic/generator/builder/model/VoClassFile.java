package com.github.foxnic.generator.builder.model;

import com.github.foxnic.api.model.CompositeParameter;
import com.github.foxnic.commons.bean.BeanUtil;

import javax.persistence.Transient;

public class VoClassFile extends PojoClassFile {

	public VoClassFile(PoClassFile poClassFile) {
		super(poClassFile.context,poClassFile.getProject(), poClassFile.getPackageName(), poClassFile.getSimpleName()+"VO");
		this.setSuperTypeFile(poClassFile);

	}

	@Override
	protected void buildOthers() {


		this.code.ln(1,"@Transient");
		this.code.ln(1,"private CompositeParameter $compositeParameter;");

		this.code.ln(1,"/**");
		this.code.ln(1," * 获得解析后的复合查询参数");
		this.code.ln(1," */");
		this.code.ln(1,"@Transient");
		this.code.ln(1,"public CompositeParameter getCompositeParameter() {");
		this.code.ln(2,"if($compositeParameter!=null) return  $compositeParameter;");
		//this.code.ln(2,"if(!\"$composite\".equals(this.getSearchField())) return null;");
		this.code.ln(2,"$compositeParameter=new CompositeParameter(this.getSearchValue(),BeanUtil.toMap(this));");
		this.code.ln(2,"return  $compositeParameter;");
		this.code.ln(1,"}");
		this.addImport(CompositeParameter.class);
		this.addImport(Transient.class);
		this.addImport(BeanUtil.class);



	}

	private PojoProperty idsProperty;

	public void setIdsPropertyName(PojoProperty p) {
		idsProperty=p;
	}

	public PojoProperty getIdsProperty() {
		return idsProperty;
	}







}
