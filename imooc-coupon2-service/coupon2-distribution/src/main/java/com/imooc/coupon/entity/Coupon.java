package com.imooc.coupon.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.converter.CouponStatusConverter;
import com.imooc.coupon.serialization.CouponSerialize;
import com.imooc.coupon.vo.CouponTemplateSDK;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)//jpa审计功能  实现对列的自动填充
// 用于监听实体类添加或者删除操作的。
@Table(name="coupon")
@JsonSerialize(using = CouponSerialize.class)
public class Coupon {
    /**
     * 自增主键
     */
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id",nullable = false)
    private Integer id;
    /*关联优惠卷模板的主键（逻辑主键）*/
    @Column(name = "template_id", nullable = false)
    private Integer templateId;
    /*领取用户*/
    @Column(name = "user_id", nullable = false)
    private Long userId;
    /*优惠卷码*/
    @Column(name = "coupon_code", nullable = false)
    private String couponCode;
    /**
     * 领取时间
     */
    @CreatedDate
    @Column(name="assign_time",nullable = false)
    private Date assignTime;
    /**
     * 优惠券状态
     */
    @Column(name = "status",nullable = false)
    @Convert(converter= CouponStatusConverter.class)
    private CouponStatus status;
    /*用户优惠卷模板对应的模板信息*/
    @Transient//不映射到数据表
    private CouponTemplateSDK templateSDK;
    public static Coupon invalidCoupon(){
        Coupon coupon = new Coupon();
        coupon.setId(-1);
        return coupon;
    }
    /**
     * <h2>构造优惠卷</h2>
     */
    public Coupon(Integer templateId,
                  Long userId,
                  String couponCode,
                  CouponStatus status){
        this.templateId=templateId;
        this.userId=userId;
        this.couponCode=couponCode;
        this.status=status;
    }
}
