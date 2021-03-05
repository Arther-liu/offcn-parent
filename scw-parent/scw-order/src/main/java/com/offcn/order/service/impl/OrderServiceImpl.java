package com.offcn.order.service.impl;

import com.offcn.dycommon.enums.OrderStatusEnumes;
import com.offcn.dycommon.response.AppResponse;
import com.offcn.order.mapper.TOrderMapper;
import com.offcn.order.po.TOrder;
import com.offcn.order.service.OrderService;
import com.offcn.order.service.PojectServiceFeign;
import com.offcn.order.vo.req.OrderInfoSubmitVo;
import com.offcn.order.vo.resp.TReturn;
import com.offcn.utils.AppDateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PojectServiceFeign pojectServiceFeign;
    @Resource
    TOrderMapper orderMapper;
    @Override
    public TOrder saveOrder(OrderInfoSubmitVo vo) {
        //1.创建一个订单对象
        TOrder order = new TOrder();
        //通过令牌获取用户id
        String memberId = stringRedisTemplate.opsForValue().get(vo.getAccessToken());
        //设置订单的用户id
        order.setMemberid(Integer.parseInt(memberId));
        BeanUtils.copyProperties(vo,order);
        //设置订单号
        String orderNum = UUID.randomUUID().toString().replace("-", "");
        order.setOrdernum(orderNum);
        //设置创建时间
        order.setCreatedate(AppDateUtils.getFormatTime());
        //设置支付状态
        order.setStatus(OrderStatusEnumes.UNPAY.getCode()+"");
        //设置发票状态
        order.setInvoice(vo.getInvoice().toString());
        //远程调用，查询回报的列表
        AppResponse<List<TReturn>> response = pojectServiceFeign.getReturnList(vo.getProjectid());
        List<TReturn> returnList = response.getData();
        TReturn myReturn = null;
        for (TReturn tReturn : returnList) {
            if(tReturn.getId().intValue()==vo.getReturnid().intValue()){
                myReturn=tReturn;
                break;
            }
        }
        //计算总金额 回报的数量*每个回报支持的金额+运费
        Integer money = order.getRtncount()*myReturn.getSupportmoney()+myReturn.getFreight();
        order.setMoney(money);
        orderMapper.insertSelective(order);
        return order;
    }
}
