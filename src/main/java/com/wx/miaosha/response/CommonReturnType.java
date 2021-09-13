package com.wx.miaosha.response;

import lombok.Data;

@Data
public class CommonReturnType {
    //表明对应请求的返回处理结果“success”或“fail”
    private String status;
    //若status=success，则data内返回前端需要的json数据
    //若status=fail，则data内使用通用的错误码格式
    private Object data;

    public static CommonReturnType create(Object result) {
        return create(result,"success");
    }
    public static CommonReturnType create(Object result, String status){
        CommonReturnType commonReturnType = new CommonReturnType();
        commonReturnType.setData(result);
        commonReturnType.setStatus(status);
        return commonReturnType;
    }
}
