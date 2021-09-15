package com.wx.miaosha.service.impl;

import com.wx.miaosha.dao.OrderDOMapper;
import com.wx.miaosha.dao.SequenceDOMapper;
import com.wx.miaosha.dataobject.OrderDO;
import com.wx.miaosha.dataobject.SequenceDO;
import com.wx.miaosha.error.BusinessException;
import com.wx.miaosha.error.EmBusinessError;
import com.wx.miaosha.service.ItemService;
import com.wx.miaosha.service.OrderService;
import com.wx.miaosha.service.UserService;
import com.wx.miaosha.service.model.ItemModel;
import com.wx.miaosha.service.model.OrderModel;
import com.wx.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    ItemService itemService;
    @Resource
    UserService userService;
    @Resource
    SequenceDOMapper sequenceDOMapper;
    @Resource
    OrderDOMapper orderDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException {
        //1.校验下单状态，下单的商品是否存在，用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品信息不存在");
        }
        UserModel userModel = userService.getUserById(userId);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户信息不存在");
        }
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "数量信息不存在");
        }
        //2.落单减库存
        boolean result = itemService.decreaseStock(itemId, amount);
        if (!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        orderModel.setItemPrice(itemModel.getPrice());
        orderModel.setOrderPrice(itemModel.getPrice().multiply(BigDecimal.valueOf(amount)));
        //生成交易流水号
        orderModel.setId(generateOrderNo());

        OrderDO orderDO = this.convertDOFromModel(orderModel);
        orderDOMapper.insertSelective(orderDO);
        //加上商品的销量
        itemService.increaseSales(itemId, amount);
        //4.返回前端
        return orderModel;
    }
    //model转DO
    private OrderDO convertDOFromModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
    //生成序列号
    //使序列号唯一
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo() {
        //订单有16位
        StringBuilder stringBuilder = new StringBuilder();
        //前8位为时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(nowDate);
        //中间6位为自增序列
        //获取当前sequence
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        //拼接
        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6 - sequenceStr.length(); i++) {
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);
        //最后两位为分库分表位,暂时不考虑
        stringBuilder.append("00");
        return stringBuilder.toString();
    }
}
