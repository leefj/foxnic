package com.github.foxnic.generator.feature.plugin;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.clazz.FileBuilder;
import com.github.foxnic.generator.feature.FeatureBuilder;

public interface PageControllerMethodAnnotiationPlugin {
	void addMethodAnnotiation(Context ctx,String methodName,FileBuilder fileBuilder,CodeBuilder code);
}
