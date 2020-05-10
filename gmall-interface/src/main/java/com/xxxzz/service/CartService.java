package com.xxxzz.service;

import com.xxxzz.gmall.CartInfo;

import java.util.List;

public interface CartService {
    /**
     * 添加购物车
     */
    public void addToCart(String skuId,String userId,Integer skuNum);

    /**
     * 查询购物车列表集合
     * @param userId
     * @return
     */
    public List<CartInfo> getCartList(String userId);

    /**
     * 缓存中没有购物车数据，则从数据库中加载
     * @param userId
     * @return
     */
    public List<CartInfo> loadCartCache(String userId);

    /**
     * 合并购物车
     * @param cartListFromCookie
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId);

    void checkCart(String skuId, String isChecked, String userId);

    /**
     * 获取被选择的购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
}
