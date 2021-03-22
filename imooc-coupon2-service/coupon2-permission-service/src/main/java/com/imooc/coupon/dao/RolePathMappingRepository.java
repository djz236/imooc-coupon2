package com.imooc.coupon.dao;

import com.imooc.coupon.entity.RolePathMapping;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * RolePathMapping Dao
 */
public interface RolePathMappingRepository extends JpaRepository<RolePathMapping,Integer> {
    /**
     * 根据 角色id+路径id 寻找数据记录
     * @param roleId
     * @param pathId
     * @return
     */
    RolePathMapping findByRoleIdAndPathId(Integer roleId,Integer pathId);
}
