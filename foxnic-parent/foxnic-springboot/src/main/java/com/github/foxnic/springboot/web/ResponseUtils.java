package com.github.foxnic.springboot.web;

import java.io.PrintWriter;

import javax.servlet.ServletResponse;

import com.alibaba.fastjson.JSON;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.springboot.mvc.Result;
 
public class ResponseUtils {

    /**
     * 使用 response 输出 JSON
     *
     * @param response
     * @param result
     */
    public static void out(ServletResponse response, Result result) {
        PrintWriter out = null;
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            out = response.getWriter();
            out.println(JSON.toJSONString(result));
        } catch (Exception e) {
            Logger.error(e + "输出JSON出错");
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

}
