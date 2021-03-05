package com.offcn.order.controller;

import com.offcn.dycommon.response.AppResponse;
import com.offcn.order.po.TOrder;
import com.offcn.order.service.OrderService;
import com.offcn.order.vo.req.OrderInfoSubmitVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "创建订单模块")
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Resource
    private OrderService orderService;

    @ApiOperation("保存订单")
    @PostMapping("/createOrder")
    public AppResponse<Object> createOrder(@RequestBody OrderInfoSubmitVo vo){
        String memberId = stringRedisTemplate.opsForValue().get(vo.getAccessToken());
        if(memberId==null){
            return AppResponse.fail("请登录");
        }
        try {
            TOrder order = orderService.saveOrder(vo);
            AppResponse response = AppResponse.ok(order);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return AppResponse.fail(null);
        }
    }
}
