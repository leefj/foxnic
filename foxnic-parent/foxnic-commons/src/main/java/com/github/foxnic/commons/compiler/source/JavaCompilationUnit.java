package com.github.foxnic.commons.compiler.source;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.printer.Printer;
import com.github.javaparser.printer.configuration.ConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.PrinterConfiguration;
import com.github.javaparser.utils.LineSeparator;

import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class JavaCompilationUnit {

    private static String[] BASIC_PACKAGES={ "java.lang","java.io","java.net","java.awt" };

    private static  JavaParser  javaParser;

    private CompilationUnit compilationUnit;

    private File javaFile;



    public CompilationUnit getCompilationUnit() {
        if(compilationUnit==null) {
            this.init();
        }
        return compilationUnit;
    }

    public static JavaCompilationUnit get(Class clazz,boolean init) {
        return new JavaCompilationUnit(clazz,init);
    }

    private Class javaClass=null;

    private Map<String,Class> importedClasses=new HashMap<>();

    private boolean parsed=false;

    public  JavaCompilationUnit(Class clazz,boolean init) {
        MavenProject mp=new MavenProject(clazz);
        this.javaClass=clazz;
        if(!mp.hasPomFile() && !mp.hasMainSourceDir()) {
            //throw new IllegalArgumentException("project error");
            return;
        };
        this.javaFile = mp.getSourceFile(clazz);
        if(this.javaFile!=null && !this.javaFile.exists()) {
            throw new IllegalArgumentException("class file not exists");
        }
        if(init) {
            init();
        }
    }

    public JavaCompilationUnit(File javaFile,boolean init) {
        if(init) {
            this.javaFile=javaFile;
            init();
        }
    }

    public void init() {
        if(javaParser==null) {
            javaParser=new JavaParser();
        }
        if(this.javaFile==null || !this.javaFile.exists()) return;
        try {
            ParseResult<CompilationUnit> result = javaParser.parse(javaFile);
            compilationUnit = result.getResult().get();
            compilationUnit.setStorage(this.javaFile.toPath());
            this.initImports();
        } catch (Exception e) {
            new RuntimeException(e);
        }
    }

    private void initImports() {
        List<ImportDeclaration> imports=this.find(ImportDeclaration.class);
        for (ImportDeclaration imp : imports) {
            Name name= (Name) imp.getChildNodes().get(0);
            String className=name.toString();
            String[] parts=className.split("\\.");
            String simpleName=parts[parts.length-1];
            Class clazz= ReflectUtil.forName(className,true);
            importedClasses.put(simpleName,clazz);
        }
    }

    public boolean isValid() {
        return this.getCompilationUnit()!=null;
    }

    public  <T extends Node> List<T> find(Class<T> nodeType) {
        return compilationUnit.findAll(nodeType);
    }

    public List<AnnotationExpr> findClassAnnotations(String className) {
        Optional<ClassOrInterfaceDeclaration> opt = null;
        try {
            opt = this.getCompilationUnit().getClassByName(className);
        }catch (Exception e) {
            return null;
        }
        if (opt == null || !opt.isPresent()) return null;
        ClassOrInterfaceDeclaration def = opt.get();
        return def.getAnnotations();
    }

    /**
     * 查找文件内某个类定义的注解
     * */
    public List<AnnotationExpr> findClassAnnotation(String className,String annotationName) {
        List<AnnotationExpr> anns=this.findClassAnnotations(className);
        if(anns==null || anns.isEmpty()) return null;
        List<AnnotationExpr> result=new ArrayList<>();
        for (AnnotationExpr ann : anns) {
            if(ann.getName().getIdentifier().equals(annotationName)) {
                result.add(ann);
            }
        }
        return result;
    }



    /**
     * 按优先级取属性值的表达式
     * */
    public Node getAnnotationPropertyValueExpr(AnnotationExpr ann,String... prop) {
        List<Node>nodes=ann.getChildNodes();
        for (Node node : nodes) {
            if(node instanceof MemberValuePair) {
                MemberValuePair mvp=(MemberValuePair) node;
                for (String p : prop) {
                    if(mvp.getName().getIdentifier().equals(p)) {
                        return mvp.getValue();
                    }
                }
            } else {
                if(nodes.size()==2) {
                    return nodes.get(1);
                }
            }
        }
        return null;
    }


    private static class  LocalPrinterConfiguration extends DefaultPrinterConfiguration {
        public LocalPrinterConfiguration() {
            super();
            Set<ConfigurationOption> opts=new HashSet<>(Arrays.asList(
                    new DefaultConfigurationOption(ConfigOption.PRINT_COMMENTS, true),
                    new DefaultConfigurationOption(ConfigOption.PRINT_JAVADOC, BeanUtil.getFieldValue(ConfigOption.PRINT_JAVADOC,"defaultValue")),
                    new DefaultConfigurationOption(ConfigOption.SPACE_AROUND_OPERATORS, BeanUtil.getFieldValue(ConfigOption.SPACE_AROUND_OPERATORS,"defaultValue")),
                    new DefaultConfigurationOption(ConfigOption.INDENT_CASE_IN_SWITCH, BeanUtil.getFieldValue(ConfigOption.INDENT_CASE_IN_SWITCH,"defaultValue")),
                    new DefaultConfigurationOption(ConfigOption.MAX_ENUM_CONSTANTS_TO_ALIGN_HORIZONTALLY, 1),
                    new DefaultConfigurationOption(ConfigOption.END_OF_LINE_CHARACTER, BeanUtil.getFieldValue(ConfigOption.END_OF_LINE_CHARACTER,"defaultValue")),
                    new DefaultConfigurationOption(ConfigOption.INDENTATION, BeanUtil.getFieldValue(ConfigOption.INDENTATION,"defaultValue"))
            ));
            BeanUtil.setFieldValue(this,"defaultOptions",opts);
        }
    }
    public String getSource() {
        Printer printer = BeanUtil.getFieldValue(compilationUnit,"printer",Printer.class);
        if (compilationUnit.containsData(CompilationUnit.LINE_SEPARATOR_KEY)) {
            LineSeparator lineSeparator = compilationUnit.getLineEndingStyleOrDefault(LineSeparator.SYSTEM);
             PrinterConfiguration config = new LocalPrinterConfiguration();
            // PrinterConfiguration config = printer.getConfiguration();
            config.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.END_OF_LINE_CHARACTER, lineSeparator.asRawString()));
            printer.setConfiguration(config);
            return printer.print(compilationUnit);
        } else {
            return printer.print(compilationUnit);
        }
    }

    public void save() {
        Optional<CompilationUnit.Storage> storage = compilationUnit.getStorage();
        if(storage.isPresent()) {
            storage.get().save();
        }
        compilationUnit.toString();
//        FileUtil.writeText(javaFile,compilationUnit.toString());
    }

    public Class getJavaClass() {
        return javaClass;
    }

    public File getJavaFile() {
        return javaFile;
    }

    public Class getImportedClass(String simpleName) {
        Class cls=importedClasses.get(simpleName);
        if(cls==null){
            for(String basicPackage : BASIC_PACKAGES) {
                cls=ReflectUtil.forName(basicPackage+"."+simpleName,true);
                if(cls!=null) {
                    break;
                }
            }
        }
        return cls;
    }

    public Object readField(FieldAccessExpr expr) {
        NameExpr scope=(NameExpr)expr.getScope();
        String simpleClassName=scope.getName().getIdentifier();
        Class type=this.getImportedClass(simpleClassName);
        if(type==null) {
            throw new RuntimeException("无法识别 "+simpleClassName);
        }
        try {
            Field field = type.getField(expr.getName().getIdentifier());
            field.setAccessible(true);
            Object value=field.get(null);
            return value;
        } catch (Exception e){
            Logger.exception("读取失败",e);
        }
        return null;
    }

    /**
     * 查找属性
     * */
    public FieldDeclaration findField(Field method) {
        return null;
    }

    /**
     * 查找方法
     * */
    public MethodDeclaration findMethod(Method method) {

        List<MethodDeclaration> matchedMethods=new ArrayList<>();
        List<MethodDeclaration> methods=this.find(MethodDeclaration.class);
        for (MethodDeclaration m : methods) {
            if(!m.getName().getIdentifier().equals(method.getName())) continue;
            if(m.getType() instanceof  ClassOrInterfaceType) {
                ClassOrInterfaceType returnType = (ClassOrInterfaceType) m.getType();
                // 如果返回值不匹配则跳过
                if (!returnType.getName().getIdentifier().equals(method.getReturnType().getSimpleName())) continue;
            } else if(m.getType() instanceof VoidType) {
                if(!method.getReturnType().equals(void.class) && !method.getReturnType().equals(Void.class)) {
                    continue;
                }
            }
            if (m.getParameters().size() != method.getParameterCount()) continue;
            boolean isParameterMatch = true;
            for (int i = 0; i < method.getParameters().length; i++) {
                Parameter parameter = method.getParameters()[i];
                com.github.javaparser.ast.body.Parameter param = m.getParameters().get(i);
                ClassOrInterfaceType paramType = (ClassOrInterfaceType) param.getType();
                if (!parameter.getType().getSimpleName().equals(paramType.getName().getIdentifier())) {
                    isParameterMatch = false;
                    break;
                }
            }
            if (!isParameterMatch) {
                continue;
            }
            matchedMethods.add(m);

        }

        if(matchedMethods.size()==0) {
            return null;
        } else if(matchedMethods.size()==1) {
            return matchedMethods.get(0);
        } else {
            throw new RuntimeException(method.toGenericString()+" 匹配到多个方法，要求1个");
        }
    }
}
