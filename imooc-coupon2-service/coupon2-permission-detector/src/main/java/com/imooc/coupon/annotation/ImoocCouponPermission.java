package com.imooc.coupon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限描述注解：定义Controller 接口的权限
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImoocCouponPermission {
    /**
     * 接口描述信息
     *
     * @return
     */
    String description() default "";

    /**
     * 此接口是否为只读 默认是true
     *
     * @return
     */
    boolean readOnly() default true;

    /**
     * 扩展属性
     * 最好以JSON格式去存储
     *
     * @return
     */
    String extra() default "";
}
