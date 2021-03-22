package com.imooc.coupon;

import com.imooc.coupon.permission.PermissionClient;
import com.imooc.coupon.vo.PermissionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

/**
 * 权限探测监听器，Spring 容器启动之后自动运行
 */
@Slf4j
@Component
public class PermissionDetectListener implements
        ApplicationListener<ApplicationReadyEvent> {
    private static final String KEY_SERVER_CTX = "server.servlet.context-path";
    private static final String SERVICE_NAME = "pring.application.name";

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext ctx = event.getApplicationContext();
        new Thread(() -> {
            //扫描权限（注解）
            List<PermissionInfo> infoList = scanPermission(ctx);
            //权限注册
            registerPermission(infoList, ctx);
        }).start();
    }

    /**
     * 注册接口权限
     *
     * @param infoList
     * @param ctx
     */
    private void registerPermission(List<PermissionInfo> infoList,
                                    ApplicationContext ctx) {
        log.info("*********************register " +
                "permission**********************");
        PermissionClient permissionClient = ctx.getBean(PermissionClient.class);
        if (null == permissionClient) {
            log.error("no permissionClient bean found");
            return;
        }
        //取出 service name
        String serviceName = ctx.getEnvironment().getProperty(SERVICE_NAME);
        log.info("serviceName:{}", serviceName);
        boolean result = new PermissionRegistry(permissionClient,
                serviceName).register(infoList);
        if (result) {
            log.info("*********************done " +
                    "scanning**********************");
        }
    }

    /**
     * 扫描微服务当中的Controller 接口权限信息
     *
     * @param ctx
     * @return
     */
    private List<PermissionInfo> scanPermission(ApplicationContext ctx) {
        //取出 context 前缀
        String pathPrefix = ctx.getEnvironment().getProperty(KEY_SERVER_CTX);
        //取出 Spring 的映射 bean
        // `RequestMappingHandlerMapping`的判断依据为：该类上标注了@Controller
        // 注解或者@Controller注解  就算作是一个Handler
        RequestMappingHandlerMapping mappingBean =
                (RequestMappingHandlerMapping) ctx.getBean(
                "requestMappingHandlerMapping");
        //扫描权限
        List<PermissionInfo> permissionInfos =
                new AnnotationScanner(pathPrefix).scanPermission(
                mappingBean.getHandlerMethods()
        );
        permissionInfos.forEach(p->log.info("{}",p));
        log.info("{} permission found",permissionInfos.size());
        log.info("*********************done scanning**********************");
        return permissionInfos;
    }
}
