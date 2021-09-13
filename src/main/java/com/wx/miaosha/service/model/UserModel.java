package com.wx.miaosha.service.model;

import lombok.Data;

import javax.validation.constraints.*;


@Data
public class UserModel {
    private Integer id;
    @NotBlank(message = "用户名不能为空")
    private String name;
    private Byte gender;
    @Min(value = 0,message = "请填写有效年龄")
    @Max(value = 120,message = "请填写有效年龄")
    @NotNull(message = "年龄不能为空")
    private Integer age;
    @NotBlank(message = "手机号不能为空")
    private String telphone;
    private String registerMode;
    private String thirdPartyId;
    @NotBlank(message = "密码不能为空")
    private String encrptPassword;
}
