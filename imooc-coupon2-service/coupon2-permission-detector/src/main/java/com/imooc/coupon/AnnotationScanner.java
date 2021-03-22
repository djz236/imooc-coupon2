package com.imooc.coupon;

import com.imooc.coupon.annotation.IgnorePermission;
import com.imooc.coupon.annotation.ImoocCouponPermission;
import com.imooc.coupon.vo.PermissionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 接口全新扫描器
 */
@Slf4j
public class AnnotationScanner {
    private String pathPrefix;
    private static final String IMOOC_COUPON_PKG = "com.imooc.coupon";

    public AnnotationScanner(String pathPrefix) {
        this.pathPrefix =trimPath(pathPrefix);
    }

    /**
     * 保证 path 以/开头、且不以/结尾
     * 如果user->/user,/user/->/user
     * @param pathPrefix
     * @return
     */
    private String trimPath(String pathPrefix) {
        if(StringUtils.isEmpty(pathPrefix)){
            return "";
        }
        if (!pathPrefix.startsWith("/")){
            pathPrefix="/"+pathPrefix;
        }
        if(pathPrefix.endsWith("/")){
            pathPrefix=pathPrefix.substring(0,pathPrefix.length()-1);
        }
        return pathPrefix;
    }

    /**
     * 构造所有Controller 的权限信息
     */
    List<PermissionInfo> scanPermission(
            Map<RequestMappingInfo, HandlerMethod> mappingMap
    ) {
        List<PermissionInfo> result = new ArrayList<>();
        mappingMap.forEach((mapinfo, method) -> result.addAll(
                buildPermission(mapinfo, method)
        ));
        return result;
    }

    /**
     * <h2>构造 Controller 的权限信息</h2>
     *
     * @param mapInfo       {@link RequestMappingInfo} @RequestMapping 对应的信息
     * @param handlerMethod {@link HandlerMethod} @HandlerMethod 对应方法的详情信息，包括方法，类，参数
     * @return
     */
    private List<PermissionInfo> buildPermission(RequestMappingInfo mapInfo,
                                                 HandlerMethod handlerMethod) {
        Method javaMethod = handlerMethod.getMethod();
        Class<?> baseClass = javaMethod.getDeclaringClass();
        //忽略非com.imooc.coupon下的mapping
        if (!isImoocCouponPackage(baseClass.getName())) {
            log.debug("ignore method:{}", javaMethod.getName());
            return Collections.emptyList();
        }
        //判断是否要忽略此方法  此方法接受参数注释类，它是要获取的注释的类型。
        // 此方法返回注释类的指定对象。
        IgnorePermission ignorePermission = javaMethod.getAnnotation(IgnorePermission.class);
        if (null != ignorePermission) {
            log.info("ignore method", javaMethod.getName());
            return Collections.emptyList();
        }
        //取出权限注解
        ImoocCouponPermission couponPermission = javaMethod.getAnnotation(ImoocCouponPermission.class);
        if (null == couponPermission) {
            // 如果没标注 ImoocCouponPermission 且没有 IgnorePermission，在日志中记录
            log.error("lack @ImoocCouponPermission->{}#{}",
                    javaMethod.getDeclaringClass().getName(),
                    javaMethod.getName());
            return Collections.emptyList();
        }
        //取出url
        Set<String> urlSet = mapInfo.getPatternsCondition().getPatterns();
        //取出method
        boolean isAllMethods = false;
        Set<RequestMethod> methodSet = mapInfo.getMethodsCondition().getMethods();
        if (CollectionUtils.isEmpty(methodSet)) {
            isAllMethods = true;
        }
        List<PermissionInfo> infoList = new ArrayList<>();
        for (String url : urlSet) {
            //支持的http method 为全量
            if (isAllMethods) {
                PermissionInfo permissionInfos = buildPermissionInfo(
                        HttpMethodEnum.ALL.name(),
                        javaMethod.getName(),
                        this.pathPrefix + url,
                        couponPermission.readOnly(),
                        couponPermission.description(),
                        couponPermission.extra()
                );
                infoList.add(permissionInfos);
                continue;
            }
            //支持部分 http method
            for (RequestMethod method : methodSet) {
                PermissionInfo info = buildPermissionInfo(
                        method.name(),
                        javaMethod.getName(),
                        this.pathPrefix + url,
                        couponPermission.readOnly(),
                        couponPermission.description(),
                        couponPermission.extra()
                );
                infoList.add(info);
                log.info("permission detected:{}",info);
            }
        }
        return infoList;
    }

    /**
     * 构造单个接口的权限信息
     * @param reqMethod
     * @param javaMethod
     * @param path
     * @param readOnly
     * @param description
     * @param extra
     * @return
     */
    private PermissionInfo buildPermissionInfo(String reqMethod, String javaMethod,
                                               String path, boolean readOnly,
                                               String description,
                                               String extra) {
        PermissionInfo info=new PermissionInfo();
        info.setMethod(reqMethod);
        info.setUrl(path);
        info.setIsRead(readOnly);
        info.setDescription(
                //如果注解中没有描述，则使用方法名称
                StringUtils.isEmpty(description)?javaMethod:description
        );
        info.setExtra(extra);
        return info;
    }

    /**
     * <h2>判断当前类是否在我们定义的包中</h2>
     *
     * @param className
     * @return
     */
    private boolean isImoocCouponPackage(String className) {
        return className.startsWith(IMOOC_COUPON_PKG);
    }
}
