package com.offcn.user.controller;

import ch.qos.logback.core.util.StringCollectionUtil;
import com.offcn.dycommon.response.AppResponse;
import com.offcn.user.po.TMemberAddress;
import com.offcn.user.service.UserService;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "用户信息")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserInfoController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserService userService;

    @ApiOperation(value = "获取用户地址")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "用户令牌",name = "accesstoken",required = true)
    })
    @GetMapping("/findAddressList")
    public AppResponse findAddressList(String accesstoken){
        String memberId = stringRedisTemplate.opsForValue().get(accesstoken);
        if(StringUtils.isEmpty(memberId)){
            return AppResponse.fail("请登录");
        }
        List<TMemberAddress> addressList = userService.findAddressList(Integer.parseInt(memberId));
        return AppResponse.ok(addressList);
    }
}
