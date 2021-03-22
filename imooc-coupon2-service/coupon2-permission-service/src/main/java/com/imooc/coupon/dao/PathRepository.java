package com.imooc.coupon.dao;

import com.imooc.coupon.entity.Path;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * path Dao
 */
public interface PathRepository extends JpaRepository<Path, Integer> {
    /**
     * 根据微服务名称查询Path 记录
     *
     * @param serviceName
     * @return
     */
    List<Path> findAllByServiceName(String serviceName);

    /**
     * 根据路径模式+请求类型 查找数据记录
     */
    Path findByPathPartternAndHttpMethod(String pathParttern,
                                         String httpMethod);
}
