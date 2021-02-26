package com.imooc.coupon.filter;

import com.sun.xml.internal.bind.v2.TODO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 在所有的过滤器之前进行校验
 * <h1>校验请求中传递的 Token</h1>
 */
@Slf4j
@Component
public class TokenFilter extends AbstractPreZuulFilter{

    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();
        log.info(String.format("%s request to %s",
                request.getMethod(),
                request.getRequestURL().toString()));
        Object token=request.getParameter("token");
        if(null==token){
            log.error("error:token is empty");
            //401 用户没有权限访问
            return fail(401,"error:token is empty");
        }
        //TODO
        return null;
    }

    @Override
    public int filterOrder() {
        return 1;
    }
}
