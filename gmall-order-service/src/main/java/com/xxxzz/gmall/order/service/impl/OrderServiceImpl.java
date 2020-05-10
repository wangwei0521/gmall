package com.xxxzz.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.xxxzz.gmall.OrderDetail;
import com.xxxzz.gmall.OrderInfo;
import com.xxxzz.gmall.enums.OrderStatus;
import com.xxxzz.gmall.enums.ProcessStatus;
import com.xxxzz.gmall.order.mapper.OrderDetailMapper;
import com.xxxzz.gmall.order.mapper.OrderInfoMapper;
import com.xxxzz.gmall.util.HttpClientUtil;
import com.xxxzz.gmall.util.RedisUtil;
import com.xxxzz.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {
        //数据不完整！ 差 总金额、订单状态、第三方交易编号、创建时间、过期时间、进程状态
        //总金额
        orderInfo.sumTotalAmount();
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        //第三方交易编号
        // 生成第三方支付编号
        String outTradeNo="XXXZZ"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //创建时间
        orderInfo.setCreateTime(new Date());
        //过期时间 ：当前时间+1
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        //进程状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        orderInfoMapper.insertSelective(orderInfo);

        //订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for(OrderDetail orderDetail : orderDetailList){
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }

        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        //生成流水号的key
        String tradeNoKey="user:"+userId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCode!=null && tradeCode.equals(tradeCodeNo)){
            return  true;
        }else{
            return false;
        }
    }

    @Override
    public void delTradeCode(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey =  "user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if ("1".equals(result)){
            return  true;
        }else {
            return  false;
        }
    }
}
