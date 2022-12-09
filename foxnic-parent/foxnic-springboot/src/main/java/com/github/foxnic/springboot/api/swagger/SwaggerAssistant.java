package com.github.foxnic.springboot.api.swagger;

import com.github.foxnic.commons.log.Logger;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;


public class SwaggerAssistant {

    /**
     * 修复 springfox 3.0.0 存在的 bug
     */
    private static String EXAMPLE_EQUALS_IMPL_CODE = "{ " +
            "        System.err.println(\"equals bug fixed\");\n" +
            "        if ($0 == this) {\n" +
            "            return true;\n" +
            "        }\n" +
            "        if ($0 == null || getClass() != $0.getClass()) {\n" +
            "            return false;\n" +
            "        }\n" +
            "        springfox.documentation.schema.Example example = (springfox.documentation.schema.Example) $0;\n" +
            "        return Objects.equals(this.getId(), example.getId()) &&\n" +
            "                Objects.equals(this.getSummary(), example.getSummary()) &&\n" +
            "                Objects.equals(this.getDescription(), example.getDescription()) &&\n" +
            "                this.getValue().equals(example.getValue()) &&\n" +
            "                this.getExternalValue().equals(example.getExternalValue()) &&\n" +
            "                this.getMediaType().equals(example.getMediaType()) &&\n" +
            "                this.getExtensions().equals(example.getExtensions());" +
            "}";


    private static ClassPool POOL = null;

    /**
     * 修复 springfox 3.0.0 存在的 bug
     */
    private static void fixEqualsBug() {
        try {
            POOL.importPackage("java.util.Objects");
            CtClass type = POOL.get("springfox.documentation.schema.Example");
            //找到对应的方法
            CtMethod equalsMethod = type.getDeclaredMethod("equals");
            equalsMethod.setBody(EXAMPLE_EQUALS_IMPL_CODE);
            type.toClass();
        } catch (Throwable t) {
            Logger.exception("springfox 3.0.0 bug 修复失败", t);
        }
    }


    public static void inject() {
        POOL = new ClassPool();
        POOL.appendClassPath(new LoaderClassPath(SwaggerAssistant.class.getClassLoader()));
        fixEqualsBug();
    }





}
