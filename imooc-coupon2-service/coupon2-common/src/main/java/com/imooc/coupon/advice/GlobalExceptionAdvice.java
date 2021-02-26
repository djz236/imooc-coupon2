package com.imooc.coupon.advice;

import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.CommonReponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionAdvice {
    /**
     * 对CouponException 进行统一处理
     *
     * @return
     */
    @ExceptionHandler(value = CouponException.class)
    public CommonReponse<String> handlerCouponException
    (HttpServletRequest req, CouponException ex) {
        CommonReponse<String> reponse =
                new CommonReponse<>(-1, "business error");
        reponse.setData(ex.getMessage());
        return null;
    }
}
