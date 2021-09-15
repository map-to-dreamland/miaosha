package com.wx.miaosha.service.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderModel {
    //交易单号，例如2019052100001212，使用string类型
    private String id;

    //购买的用户id
    private Integer userId;

    //购买的商品id
    private Integer itemId;

    //购买时商品的单价
    private BigDecimal itemPrice;

    //购买数量
    private Integer amount;

    //购买金额
    private BigDecimal orderPrice;
}
