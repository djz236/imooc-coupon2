package com.imooc.coupon.dao;

import com.imooc.coupon.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * role Dao
 */
public interface RoleRepository extends JpaRepository<Role,Integer>
{
}
