package com.imooc.coupon;

import com.imooc.coupon.permission.PermissionClient;
import com.imooc.coupon.vo.CommonReponse;
import com.imooc.coupon.vo.CreatePathRequest;
import com.imooc.coupon.vo.PermissionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限注册组件
 */
@Slf4j
public class PermissionRegistry {
    /**
     * 权限服务SDK客户端
     */
    private PermissionClient permissionClient;
    /**
     * 服务名称
     */
    private String serviceName;

    public PermissionRegistry(PermissionClient permissionClient,
                              String serviceName) {
        this.permissionClient = permissionClient;
        this.serviceName = serviceName;
    }

    /**
     * 权限注册
     *
     * @return
     */
    boolean register(List<PermissionInfo> infoList) {
        if (CollectionUtils.isEmpty(infoList)) {
            return false;
        }
        List<CreatePathRequest.PathInfo> pathInfos =
                infoList.stream().map(info -> CreatePathRequest.PathInfo.builder()
                        .pathPattern(info.getUrl())
                        .httpMethod(info.getMethod())
                        .pathName(info.getDescription())
                        .serviceName(serviceName)
                        .opMode(info.getIsRead() ? OpModeEnum.READ.name() :
                                OpModeEnum.WRITE.name())
                        .build())
                        .collect(Collectors.toList());
        CommonReponse<List<Integer>> response = permissionClient.createPath(
                new CreatePathRequest(pathInfos)
        );
        if (!CollectionUtils.isEmpty(response.getData())) {
            log.info("register path info:{}", response.getData());
            return true;
        }
        return false;
    }
}
