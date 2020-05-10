package com.xxxzz.gmall;

import com.xxxzz.gmall.enums.OrderStatus;
import com.xxxzz.gmall.enums.PaymentWay;
import com.xxxzz.gmall.enums.ProcessStatus;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderInfo implements Serializable {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    //收货人名称
    @Column
    private String consignee;

    //收货人电话
    @Column
    private String consigneeTel;

    //总金额
    @Column
    private BigDecimal totalAmount;

    //订单状态，用于显示给用户查看。设定初始值。
    @Column
    private OrderStatus orderStatus;

    //订单进度状态，程序控制、 后台管理查看。设定初始值，
    @Column
    private ProcessStatus processStatus;

    //用户Id。从拦截器已放到请求属性中。
    @Column
    private String userId;

    //支付方式（网上支付、货到付款）。页面获取
    @Column
    private PaymentWay paymentWay;

    //默认当前时间+1天
    @Column
    private Date expireTime;

    //收货地址。页面获取
    @Column
    private String deliveryAddress;

    //订单状态。页面获取
    @Column
    private String orderComment;

    //创建时间。设当前时间
    @Column
    private Date createTime;

    //拆单时产生，默认为空
    @Column
    private String parentOrderId;

    //物流编号,初始为空，发货后补充
    @Column
    private String trackingNo;

    //订单详情列表
    @Transient
    private List<OrderDetail> orderDetailList;


    @Transient
    private String wareId;

    //第三方支付编号。按规则生成
    @Column
    private String outTradeNo;

    //计算总价格
    public void sumTotalAmount() {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount = totalAmount.add(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())));
        }
        this.totalAmount = totalAmount;
    }

}
