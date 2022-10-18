package com.github.foxnic.springboot.api.swagger.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.springboot.api.swagger.source.ControllerSwaggerCompilationUnit;
import com.github.foxnic.springboot.api.swagger.source.SwaggerAnnotationApi;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.swagger.annotations.Api;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.*;

public class TagsHandler {
    private  SwaggerDataHandler dataHandler;

    public TagsHandler(SwaggerDataHandler dataHandler) {
        this.dataHandler=dataHandler;
    }

    private Set<String> getTagNames(ApiDocket docket) {
        Set<String> tagNames=new HashSet<>();
        JSONArray tags = docket.getApiTags();
        if(tags==null) tags=new JSONArray();
        for (int i = 0; i < tags.size() ; i++) {
            tagNames.add(tags.getJSONObject(i).getString("name"));
        }
        return tagNames;
    }

    /**
     * 获得控制器上 @Api 注解信息
     * */
    private SwaggerAnnotationApi getSwaggerAnnotationApi(Class controller) {

        ControllerSwaggerCompilationUnit jcu = dataHandler.getJCU(controller);
        List<AnnotationExpr> anns = jcu.findClassAnnotation(controller.getSimpleName(), Api.class.getSimpleName());
        SwaggerAnnotationApi apiAnSrc = null;
        if (anns != null && anns.size() > 0) {
            apiAnSrc = SwaggerAnnotationApi.fromSource((NormalAnnotationExpr) anns.get(0), jcu);
        }
        //
        Api api = (Api) controller.getAnnotation(Api.class);
        SwaggerAnnotationApi apiAnn = SwaggerAnnotationApi.fromAnnotation(api);
        if (apiAnSrc != null) {
            apiAnn = apiAnSrc;
        }
        return  apiAnn;

    }


    /**
     * 处理 docket 中 tag 相关的部分
     * */
    public void process(ApiDocket docket) {

        Set<Class> modifiedControllers=this.dataHandler.getGroupMeta().getModifiedControllers();
        //搜集已经在文档报文中存在的tag值
        Set<String> tagNames=this.getTagNames(docket);
        //
        JSONArray tags = docket.getApiTags();
        JSONObject paths = docket.getApiPaths();
        //
        Map<Class, String> controllerMainTagMap = new HashMap<>();
        //
        for (String path : paths.keySet()) {
            JSONObject cfg = paths.getJSONObject(path);
            //循环请求方法，并获得参数对应的注解
            for (String httpMethod : cfg.keySet()) {
                //
                JSONObject httpMethodCfg = cfg.getJSONObject(httpMethod);
                HandlerMethod hm = this.dataHandler.getPathsHandler().getHandlerMethod(path, httpMethod);
                Method method = hm.getMethod();
                Class controller = method.getDeclaringClass();

                // 如果未被修改过，则不处理
                if(this.dataHandler.getGroupMeta().getMode()== GroupMeta.ProcessMode.PART_CACHE) {
                    if (!modifiedControllers.contains(controller)) {
                        continue;
                    }
                }

                //
                SwaggerAnnotationApi apiAnn = getSwaggerAnnotationApi(controller);
                //
                String mainTag = controllerMainTagMap.get(controller);

                for (String tag : apiAnn.getTags()) {
                    // 如果 tag 不存在，就加入
                    if (!tagNames.contains(tag)) {
                        tagNames.add(tag);
                        JSONObject tagItem = new JSONObject();
                        tagItem.put("name", tag);
                        tags.add(tagItem);
                        if (mainTag == null) {
                            mainTag = tag;
                            controllerMainTagMap.put(controller, mainTag);
                        }
                    }
                }
                // 调整主标签值，使API文档归集到路径下
                if (mainTag != null) {
                    httpMethodCfg.getJSONArray("tags").set(0, mainTag);
                }
            }
        }

    }
}
