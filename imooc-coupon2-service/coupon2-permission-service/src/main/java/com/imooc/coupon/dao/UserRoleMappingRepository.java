package com.imooc.coupon.dao;

import com.imooc.coupon.entity.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * UserRoleMapping Dao
 */
public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping,Integer> {
    /**
     * 通过userId 寻找数据记录
     * @param id
     * @return
     */
    UserRoleMapping findByUserId(Long id);
}
