package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用响应对象定义
 *
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonReponse<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;

    public CommonReponse(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
