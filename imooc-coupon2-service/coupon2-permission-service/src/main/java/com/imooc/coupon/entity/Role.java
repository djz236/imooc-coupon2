package com.imooc.coupon.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 用户角色实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="coupon_role")
public class Role {
    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id",nullable = false)
    private Integer id;
    /**
     * 角色名称
     */
    @Basic
    @Column(name="role_name",nullable = false)
    private String roleName;
    /**
     * 角色标签
     */
    @Basic
    @Column(name="role_tag",nullable = false)
    private String roleTag;

}
