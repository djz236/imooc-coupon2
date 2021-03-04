package com.imooc.coupon.constant;

import java.util.stream.Stream;

/**
 * 有效期
 */
public enum PeriodType {
    REGULAR("固定的(固定日期)", 1),
    SHIFT("变动的(以领取之日开始计算)", 2);
    //成员变量
    /**
     * 描述
     */
    private String description;
    /**
     * code
     */
    private Integer code;

    //私有构造方法
    private PeriodType(String description, Integer code) {
        this.description = description;
        this.code = code;
    }

    //普通方法
    public static String getDescription(Integer code) {
        for (PeriodType p : PeriodType.values()) {
            if (p.getCode() == code) {
                return p.getDescription();
            }
        }
        return null;
    }

    public static PeriodType of(Integer code) {
        return Stream.of(values())
                .filter(bean->bean.getCode().equals(code))
                .findAny()
                .orElseThrow(()->new IllegalArgumentException(code+" not exists"));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
