package com.wx.miaosha.service;

import com.wx.miaosha.error.BusinessException;
import com.wx.miaosha.service.model.UserModel;

public interface UserService {
    //根据id获取用户信息
    UserModel getUserById(Integer id);
    //用户注册
    void register(UserModel userModel) throws BusinessException;
    //用户登录
    UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException;
}
