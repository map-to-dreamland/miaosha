package com.wx.miaosha.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.wx.miaosha.dao.UserDOMapper;
import com.wx.miaosha.dao.UserPasswordDOMapper;
import com.wx.miaosha.dataobject.UserDO;
import com.wx.miaosha.dataobject.UserPasswordDO;
import com.wx.miaosha.error.BusinessException;
import com.wx.miaosha.error.EmBusinessError;
import com.wx.miaosha.service.UserService;
import com.wx.miaosha.service.model.UserModel;
import com.wx.miaosha.validator.ValidationResult;
import com.wx.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.Validator;
//写最上级那个异常
import java.sql.SQLIntegrityConstraintViolationException;

@Transactional
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserDOMapper userDOMapper;
    @Resource
    private UserPasswordDOMapper userPasswordDOMapper;
    @Resource
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        //调用UserDOMapper获取到对应的用户dataobject
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null) {
            return null;
        }
        //通过用户id获取对应的用户加密密码信息
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        return convertFromDataObject(userDO, userPasswordDO);
    }

    @Override
    public void register(UserModel userModel) throws BusinessException {
        //校验
//        if (userModel == null) {
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
//        }
//        if (StringUtils.isEmpty(userModel.getName())
//                || userModel.getGender() == null
//                || userModel.getAge() == null
//                || StringUtils.isEmpty(userModel.getTelphone())) {
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
//        }
        //validate校验
        ValidationResult validate = validator.validate(userModel);
        if (validate.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,validate.getErrMsg());
        }
        //insertSelective相对于insert方法，不会覆盖掉数据库的默认值
        UserDO userDo = convertUserFromModel(userModel);
        try {
            userDOMapper.insertSelective(userDo);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已存在");
        }
        userModel.setId(userDo.getId());
        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if (userDO == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO, userPasswordDO);
        //对比用户输入的密码加密后是否和数据库一致
        if (!StringUtils.equals(encrptPassword,userModel.getEncrptPassword())) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }

    //将UserModel对象里的数据split成两个dataobject各自的数据
    private UserPasswordDO convertPasswordFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }

    private UserDO convertUserFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }


    //将两个dataobject里的数据转换成一个UserModel对象
    private UserModel convertFromDataObject(UserDO userDO,UserPasswordDO userPasswordDO) {
        if (userDO == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if (userPasswordDO != null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }
}
