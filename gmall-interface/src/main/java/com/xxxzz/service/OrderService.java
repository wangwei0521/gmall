package com.xxxzz.service;

import com.xxxzz.gmall.OrderInfo;

public interface OrderService {

    /**
     * 保存订单
     * @param orderInfo
     * @return
     */
    String saveOrder(OrderInfo orderInfo);

    /**
     * 生成流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 校验流水号
     * @param userId
     * @param tradeCodeNo
     * @return
     */
    boolean checkTradeCode(String userId,String tradeCodeNo);

    /**
     * 删除流水号
     * @param userId
     */
    void delTradeCode(String userId);

    /**
     * 验证库存数量
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId, Integer skuNum);
}
