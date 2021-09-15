package com.wx.miaosha.service;

import com.wx.miaosha.error.BusinessException;
import com.wx.miaosha.service.model.OrderModel;

public interface OrderService {
    //创建订单
    OrderModel createOrder(Integer userId,Integer itemId, Integer amount) throws BusinessException;

}
