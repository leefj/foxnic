package com.github.foxnic.springboot.api.swagger;


import com.fasterxml.classmate.TypeResolver;
import com.github.foxnic.commons.concurrent.task.SimpleTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import springfox.documentation.PathProvider;
import springfox.documentation.schema.AlternateTypeRuleConvention;
import springfox.documentation.spi.service.RequestHandlerCombiner;
import springfox.documentation.spi.service.RequestHandlerProvider;
import springfox.documentation.spi.service.contexts.Defaults;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.plugins.AbstractDocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;
import springfox.documentation.spring.web.plugins.SpringIntegrationPluginNotPresentInClassPathCondition;
import springfox.documentation.spring.web.scanners.ApiDocumentationScanner;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Builds and executes all DocumentationConfigurer instances found in the
 * application context, at the end of all {@link #getPhase phases} in {@link SmartLifecycle} .
 * <p>
 * If no instances DocumentationConfigurer are found a default one is created and executed.
 */
@Component("FoxnicDocumentationPluginsBootstrapper")
@Conditional(SpringIntegrationPluginNotPresentInClassPathCondition.class)
public class FoxnicDocumentationPluginsBootstrapper
        extends AbstractDocumentationPluginsBootstrapper
        implements SmartLifecycle, ApplicationListener<ApplicationStartedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoxnicDocumentationPluginsBootstrapper.class);
    private static final String SPRINGFOX_DOCUMENTATION_AUTO_STARTUP = "springfox.documentation.auto-startup";
    private final Environment environment;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Autowired
    @SuppressWarnings("ParameterNumber")
    public FoxnicDocumentationPluginsBootstrapper(
            DocumentationPluginsManager documentationPluginsManager,
            List<RequestHandlerProvider> handlerProviders,
            DocumentationCache scanned,
            ApiDocumentationScanner resourceListing,
            TypeResolver typeResolver,
            Defaults defaults,
            PathProvider pathProvider,
            Environment environment) {
        super(
                documentationPluginsManager,
                handlerProviders,
                scanned,
                resourceListing,
                defaults,
                typeResolver,
                pathProvider);

        this.environment = environment;
    }

    @Override
    public boolean isAutoStartup() {
//        String autoStartupConfig =
//                environment.getProperty(
//                        SPRINGFOX_DOCUMENTATION_AUTO_STARTUP,
//                        "true");
//        return Boolean.parseBoolean(autoStartupConfig);
//        暂不启动 Swagger 扫描，还有蛮多错误
//        return false;
        return  SwaggerGlobalConfig.isAutoStartup();
    }

    @Override
    public void stop(Runnable callback) {
        callback.run();
    }

    /**
     * 使用并行的线程扫描，以提高启动速度
     * */
    @Override
    public void start() {
        // 不在此处启动扫描，以便提高启动速度
    }

//    @Override
    public void startInternal() {
        if (initialized.compareAndSet(false, true)) {
            LOGGER.debug("Documentation plugins bootstrapped");
            super.bootstrapDocumentationPlugins();
            LOGGER.info("Swagger Documentation plugins is ready");
        }
    }

    @Override
    public void stop() {
        initialized.getAndSet(false);
        getScanned().clear();
    }

    @Override
    public boolean isRunning() {
        return initialized.get();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    @Autowired(required = false)
    public void setCombiner(RequestHandlerCombiner combiner) {
        super.setCombiner(combiner);
    }

    @Override
    @Autowired(required = false)
    public void setTypeConventions(List<AlternateTypeRuleConvention> typeConventions) {
        super.setTypeConventions(typeConventions);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {

        if(this.isAutoStartup()) {
            // 启动 Swagger 扫描
            SimpleTaskManager.doParallelTask(new Runnable() {
                @Override
                public void run() {
                    startInternal();
                }
            });
        }

    }
}
