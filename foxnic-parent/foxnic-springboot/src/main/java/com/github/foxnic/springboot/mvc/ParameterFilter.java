package com.github.foxnic.springboot.mvc;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.springboot.spring.SpringUtil;
import com.github.foxnic.springboot.web.HtmlResponseWrapper;
import org.springframework.boot.web.servlet.ServletContextInitializerBeans;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;


public class ParameterFilter implements Filter {

    private static ParameterFilter parameterFilter;

    public ServletContext servletContext;

    public ServletContext getServletContext() {
        return servletContext;
    }

    public ParameterFilter() {
        parameterFilter = this;
    }

    public static ParameterFilter getInstance() {
        return parameterFilter;
    }

    private List filters = null;

    public List filters() {
        return filters;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        parameterFilter = this;
        servletContext=filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (this.filters == null) {
            this.filters = (List) BeanUtil.getFieldValue(chain, "filters");
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}

