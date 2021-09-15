package com.wx.miaosha.controller;

import com.alibaba.druid.util.StringUtils;
import com.wx.miaosha.controller.viewobject.UserVO;
import com.wx.miaosha.error.BusinessException;
import com.wx.miaosha.error.EmBusinessError;
import com.wx.miaosha.response.CommonReturnType;
import com.wx.miaosha.service.UserService;
import com.wx.miaosha.service.model.UserModel;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Random;
//允许跨域请求

@Controller
@RequestMapping("/user")
public class UserController extends BaseController{
    @Resource
    private UserService userService;
    @Resource
    private HttpServletRequest httpServletRequest;

    //登录接口
    @RequestMapping(value = "/login",method = RequestMethod.POST,consumes = BaseController.CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                                  @RequestParam(name = "password")String password) throws BusinessException {
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        UserModel userModel = userService.validateLogin(telphone,toMD5(password));
        //登录成功将登录凭证和用户数据存入session
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        //System.out.println(userModel);
        return CommonReturnType.create(null);
    }
    //注册接口
    @RequestMapping(value = "/register",method = RequestMethod.POST,consumes = BaseController.CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "otpCode")String otpCode,
                                     @RequestParam(name = "telphone")String telphone,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")Byte gender,
                                     @RequestParam(name = "password")String password,
                                     @RequestParam(name = "age")Integer age) throws BusinessException {
        String inSessionOtpCode = (String) httpServletRequest.getSession().getAttribute("telphone");
        System.out.println(inSessionOtpCode);
        //判断验证码是否正确
        if (!StringUtils.equals(inSessionOtpCode,otpCode)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setGender(gender);
        //因为加密后才向model封装，空字符串变成了加密字符串，service层的校验不起作用
        if (StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"密码不能为空");
        }
        userModel.setEncrptPassword(toMD5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }
    //用户获取otp短信接口
    @RequestMapping(value = "/getotp",method = RequestMethod.POST,consumes = BaseController.CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone")String telphone) {
        //需要一定规则生成验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);
        //将OTP验证码同对应用户的手机号关联，使用httpsession的方式绑定手机号与OTPCDOE
        httpServletRequest.getSession().setAttribute("telphone",otpCode);
        //将OTP验证码通过短信通道发送给用户，省略
        System.out.println("telphone=" + telphone + "&otpCode=" + otpCode);
        return CommonReturnType.create(null);

    }
    //根据id查询单个用户
    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);
        //若是获取的对应用户信息不存在
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        //将核心领域模型用户对象转化为可供UI使用的viewobject
        UserVO userVO = convertVOFromModel(userModel);
        return CommonReturnType.create(userVO);
    }

    //MD5加密方法，借助apache工具类DigestUtils实现
    public static String toMD5(String str) {
        return DigestUtils.md5Hex(str);
    }
    //将userModel转为userVO
    private UserVO convertVOFromModel(UserModel userModel) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }
}
