package com.github.foxnic.generator.feature;

 
import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.clazz.FileBuilder;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;

public abstract class FeatureBuilder {
	
	protected DefaultNameConvertor convertor=new DefaultNameConvertor();
	
	protected BeanNameUtil beanNameUtil=new BeanNameUtil();
	
	public static FeatureBuilder[] BUILDERS= {new GetById() , new QueryList(),new QueryPagedList(),new Insert(),
			new Update() ,new Delete()}; 
 
	public abstract String getMethodName(Context ctx);
	
	public abstract String getApiComment(Context ctx);
 
	public abstract void buildServiceInterfaceMethod(FileBuilder builder,Context ctx,CodeBuilder code);
	
	public abstract void buildServiceImplMethod(FileBuilder builder,Context ctx,CodeBuilder code);
	
 
	public void buildFeignConstant(FileBuilder builder, Context ctx, CodeBuilder code) {
		String cst = getUriConstName();
		String uri=cst.replace('_', '-').toLowerCase();
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx));
		code.ln(1," */");
		code.ln(1,"public static final String "+cst+" = BASIC_PATH + API_CONTEXT_PATH + \"/"+uri+"\";");
	}

	protected String getUriConstName() {
		String cst=beanNameUtil.depart(this.getClass().getSimpleName()).toUpperCase();
		return cst;
	}
	
	public abstract void buildFeignMethod(FileBuilder builder,Context ctx,CodeBuilder code);
	
	public abstract void buildControllerMethod(FileBuilder builder,Context ctx,CodeBuilder code);
 
}
