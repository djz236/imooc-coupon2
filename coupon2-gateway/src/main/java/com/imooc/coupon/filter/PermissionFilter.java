package com.imooc.coupon.filter;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.permission.PermissionClient;
import com.imooc.coupon.vo.CheckPermissionRequest;
import com.imooc.coupon.vo.CommonReponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限过滤器的实现
 */
@Slf4j
@Component
public class PermissionFilter
        extends AbsSecurityFilter {
    private final PermissionClient permissionClient;

    @Autowired
    public PermissionFilter(PermissionClient permissionClient) {
        this.permissionClient = permissionClient;
    }

    @Override
    protected Boolean interceptCheck(HttpServletRequest request,
                                     HttpServletResponse response) throws Exception {
        // 执行权限校验的逻辑
        // 从 header 中 获取到userId
        String userId1 = "15";// request.getHeader("userId");
        Long userId = Long.valueOf(userId1);
        String uri = request.getRequestURI().substring("/imooc".length());
        String httpMethod = request.getMethod();
        return permissionClient.checkPermission(
                new CheckPermissionRequest(userId, uri, httpMethod)
        );
    }

    @Override
    protected int getHttpStatus() {
        return HttpStatus.OK.value();
    }

    @Override
    protected String getErrorMsg() {
        CommonReponse<Object> response = new CommonReponse<>();
        response.setCode(-2);
        response.setMessage("您没有操作权限");
        return JSON.toJSONString(response);
    }
}
