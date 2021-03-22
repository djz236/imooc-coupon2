package com.imooc.coupon.service;

import com.imooc.coupon.dao.PathRepository;
import com.imooc.coupon.entity.Path;
import com.imooc.coupon.vo.CreatePathRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 路径相关的服务功能实现
 */
@Slf4j
@Service
public class PathService {
    private final PathRepository pathRepository;

    public PathService(PathRepository pathRepository) {
        this.pathRepository = pathRepository;
    }
    public List<Integer> createPath(CreatePathRequest request){
        List<CreatePathRequest.PathInfo> pathInfos = request.getPathInfo();
        List<CreatePathRequest.PathInfo> validRequests= new ArrayList<>();
        //根据微服务名称查询Path 记录
        List<Path> currentPaths =
                pathRepository.findAllByServiceName(pathInfos.get(0).getServiceName());
        if(!CollectionUtils.isEmpty(currentPaths)){
            for(CreatePathRequest.PathInfo pathInfo:pathInfos){
                boolean isValid=true;
                for(Path currentPath:currentPaths){
                    //当前的路径模式 HTTP方法类型和传递进来的路径模式  HTTP方法类型一致
                    if(currentPath.getPathParttern().equals(pathInfo.getPathPattern())
                    &&currentPath.getHttpMethod().equals(pathInfo.getHttpMethod())){
                        isValid=false;
                        break;
                    }
                }
                if(isValid){
                    validRequests.add(pathInfo);
                }
            }
        }else{
            validRequests = pathInfos;
        }
        List<Path> paths = new ArrayList<>(validRequests.size());
        validRequests.forEach(p->paths.add(new Path(
                p.getPathPattern(),
                p.getHttpMethod(),
                p.getPathName(),
                p.getServiceName(),
                p.getOpMode()
        )));
        return pathRepository.saveAll(paths).stream().map(Path::getId)
                .collect(Collectors.toList());
    }
}
