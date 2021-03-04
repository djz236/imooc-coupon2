package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum CouponCategory {

    MANJIAN("满减券",
            "001"),
    ZHEKOU("折扣券","002"),
    LIJIAN("立减券","003");;
    private String description;
    private String code;
    public static CouponCategory of(String code){
        Objects.requireNonNull(code);
        //Stream.of返回其元素为指定值的顺序有序流。
        //Enum类和enum关键字定义的类型都有values方法，
        // 但是点进去会发现找不到这个方法。
        // 这是因为java编译器在编译这个类（enum关键字定义的类默认继承
        // java.lang.Enum）的时候
        //value()方法可以将枚举类转变为一个枚举类型的数组，
        // 因为枚举中没有下标，我们没有办法通过下标来快速找到需要的枚举类，
        // 这时候，转变为数组之后，我们就可以通过数组的下标，
        // 来找到我们需要的枚举类
        return Stream.of(values())
                .filter(bean->bean.code.equals(code))
                //用findAny()寻找List中符合要求的数据
                .findAny()
                .orElseThrow(()->new IllegalArgumentException(code+"no exists"));
    }
}
