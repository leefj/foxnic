# foxnic

#### 介绍
foxnic 是一套用于Web应用开发的组件与支持框架。包含 common、sql、dao、springboot-support、geneartor 几个子模块。通过这些底层支持，在加速Web应用的开发同时，使 Web应用更具业务应对的灵活性。在 foxnic 基础上构建的 [foxnic-web](https://gitee.com/LeeFJ/foxnic-web) ，为 Web 应用提供现成可落地的解决方案。[演示系统](http://eam-demo.rainbooow.com:26788/login.html)

#### 模块说明

#### foxnic-commom
通用工具，字符串处理、加解密、流处理、类型转换、Bean工具等。

#### foxnic-sql 
提供SQL语句解决方案，灵活拼接语句。可直观地看到正在执行的语句，语句输出（变量代入）与执行（绑定变量）分离。

#### foxnic-dao
基于 SpringJDBC 构建的 SQL 语句执行与数据返回框架。

#### foxnic-springboot
对 Spring 以及SpringBoot 进行扩展，包括启动、配置、Mvc、Rest接口等诸多实现。

#### foxnic-generator
代码生成框架，可生成 实体、FeignProxy、Controller、Service、Html页面等代码。

