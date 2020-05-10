package com.xxxzz.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xxxzz.gmall.*;
import com.xxxzz.gmall.config.LoginRequire;
import com.xxxzz.gmall.enums.OrderStatus;
import com.xxxzz.gmall.enums.ProcessStatus;
import com.xxxzz.service.CartService;
import com.xxxzz.service.ManageService;
import com.xxxzz.service.OrderService;
import com.xxxzz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;

    @Reference
    private ManageService manageService;

    @RequestMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        // 收货人地址
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        request.setAttribute("userAddressList",userAddressList);
        // 得到选中的购物车列表
        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);

        // 订单信息集合
        List<OrderDetail> orderDetailList=new ArrayList<>(cartCheckedList.size());
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }
        request.setAttribute("orderDetailList",orderDetailList);
        //利用OrderInfo计算总价格
        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        // 生成流水号
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeCode",tradeNo);
        return "trade";
    }

    /**
     * 提交订单
     *
     * 防止表单无刷新重复提交？
     * 1.在订单页面制作一个流水号！将流水号隐藏在页面中，并将其放入缓存
     * 2.用户点击提交的时候，将页面的流水号和缓存中的流水号进行比较
     *   相等：可以提交
     *   不相等：不能提交
     * 3.比较完成之后，将redis的流水号删除
     * @param orderInfo
     * @param request
     * @return
     */
    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        //orderInfo中还缺少userId
        orderInfo.setUserId(userId);
        // 检查流水号是否与redis中的流水号相等
        String tradeNo = request.getParameter("tradeNo");
        boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag){
            request.setAttribute("errMsg","该页面已失效，请重新结算!");
            return "tradeFail";
        }
        // 获取订单详情
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            // 从订单中获取购物skuId，数量，验证库存
            boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!result){
                request.setAttribute("errMsg","商品库存不足，请重新下单！");
                return "tradeFail";
            }

            //先获取sku的实时价格，在验证价格是否匹配
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            //拿商品的实时价格和订单的价格做比较
            int res = skuInfo.getPrice().compareTo(orderDetail.getOrderPrice());
            if(res!=0){
                request.setAttribute("errMsg",orderDetail.getOrderPrice() + "价格不匹配！");
                //重新查询实时价格并修改缓存
                cartService.loadCartCache(userId);
                return "tradeFail";
            }
        }
        String orderId = orderService.saveOrder(orderInfo);
        // 删除tradeNo
        orderService.delTradeCode(userId);
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }
}
