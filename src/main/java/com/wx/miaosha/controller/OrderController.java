package com.wx.miaosha.controller;

import com.wx.miaosha.error.BusinessException;
import com.wx.miaosha.error.EmBusinessError;
import com.wx.miaosha.response.CommonReturnType;
import com.wx.miaosha.service.OrderService;
import com.wx.miaosha.service.model.OrderModel;
import com.wx.miaosha.service.model.UserModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/order")
public class OrderController extends BaseController{
    @Resource
    OrderService orderService;
    @Resource
    HttpServletRequest httpServletRequest;

    //下单请求
    @ResponseBody
    @RequestMapping("/create")
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount) throws BusinessException {
        //获取用户登录信息
        Boolean is_login = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (is_login == null || !is_login) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登录，不能下单");
        }
        UserModel login_user = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");
        orderService.createOrder(login_user.getId(),itemId,amount);
        return CommonReturnType.create(null);
    }
}
