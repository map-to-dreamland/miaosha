package com.wx.miaosha.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@Component
public class ValidatorImpl implements InitializingBean {
    private Validator validator;

    public ValidationResult validate(Object bean) {
        ValidationResult validationResult = new ValidationResult();
        Set<ConstraintViolation<Object>> validateSets = validator.validate(bean);
        if (validateSets.size() > 0) {
            //如果有错
            validationResult.setHasErrors(true);
            validateSets.forEach(validate -> {
                String errMsg = validate.getMessage();
                String propertyName = validate.getPropertyPath().toString();
                validationResult.getErrorMsgMap().put(propertyName,errMsg);
            });
        }
        return validationResult;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //将hibernate validator通过工厂的初始化方式使其实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
