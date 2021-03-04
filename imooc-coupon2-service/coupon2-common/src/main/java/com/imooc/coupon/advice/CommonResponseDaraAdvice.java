package com.imooc.coupon.advice;

import com.imooc.coupon.annotation.IgnoreResponseAdvice;
import com.imooc.coupon.vo.CommonReponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 对返回响应做处理
 */
@RestControllerAdvice
public class CommonResponseDaraAdvice
        implements ResponseBodyAdvice<Object> {
    /**
     * 判断是否需要对响应进行处理
     *
     * @param methodParameter
     * @param aClass
     * @return
     */
    @Override
    public boolean supports(MethodParameter methodParameter,
                            Class<? extends HttpMessageConverter<?>> aClass) {
        // 如果当前方法所在的类标识了 @IgnoreResponseAdvice 注解, 不需要处理
        if (methodParameter.getDeclaringClass()
                .isAnnotationPresent(IgnoreResponseAdvice.class)) {
            return false;
        }
        // 如果当前方法标识了 @IgnoreResponseAdvice 注解, 不需要处理
        if (methodParameter.getMethod()
                .isAnnotationPresent(IgnoreResponseAdvice.class)) {
            return false;
        }
        // 对响应进行处理, 执行 beforeBodyWrite 方法
        return true;
    }

    /**
     * <h2>响应返回之前的处理</h2>
     * o controller 返回的对象、
     * methodParameter controller 声明方法
     */
    @Override
    public Object beforeBodyWrite(Object o,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass,
                                  ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {
        //定义最终的返回对象
        CommonReponse<Object> response = new CommonReponse<>(0, "");
        // 如果 o 是 null, response 不需要设置 data
        if (null == o) {
            return response;
            //如果o已经是CommonResposne,不需要再次处理
        } else if (o instanceof CommonReponse) {
            response = (CommonReponse<Object>) o;
            // 否则, 把响应对象作为 CommonResponse 的 data 部分
        } else {
            response.setData(o);
        }
        return response;
    }
}
