package com.xxxzz.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.xxxzz.gmall.CartInfo;
import com.xxxzz.gmall.SkuInfo;
import com.xxxzz.gmall.config.CookieUtil;
import com.xxxzz.service.ManageService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {
    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE = 7 * 24 * 3600;
    @Reference
    private ManageService manageService;

    // 未登录的时候，添加到购物车
    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, Integer skuNum) {
        //从cookie中获取购物车数据
        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = new ArrayList<>();
        boolean ifExist = false;
        if (cartJson != null) {
            cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                //比较添加的商品是否有该商品
                if (cartInfo.getSkuId().equals(skuId)) {//有该商品
                    //增加数量
                    cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
                    // 价格设置
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    ifExist = true;
                    break;
                }
            }
        }
        // //购物车里没有对应的商品 或者 没有购物车
        if (!ifExist) {
            //把商品信息取出来，新增到购物车
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();

            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            cartInfoList.add(cartInfo);
        }
        // 把购物车写入cookie
        String newCartJson = JSON.toJSONString(cartInfoList);
        CookieUtil.setCookie(request, response, cookieCartName, newCartJson, COOKIE_CART_MAXAGE, true);
    }

    /**
     * 查询cookie 中购物车列表
     *
     * @param request
     * @return
     */
    public List<CartInfo> getCartList(HttpServletRequest request) {
        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
        return cartInfoList;
    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, cookieCartName);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        //  取出购物车中的商品
        List<CartInfo> cartList = getCartList(request);
        // 循环比较
        for (CartInfo cartInfo : cartList) {
            if (cartInfo.getSkuId().equals(skuId)) {//id相同的更新选中状态
                cartInfo.setIsChecked(isChecked);
            }
        }
        // 保存到cookie
        String newCartJson = JSON.toJSONString(cartList);
        CookieUtil.setCookie(request, response, cookieCartName, newCartJson, COOKIE_CART_MAXAGE, true);
    }
}
