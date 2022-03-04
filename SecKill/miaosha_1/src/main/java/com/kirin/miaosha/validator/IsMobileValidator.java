package com.kirin.miaosha.validator;

import  javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import com.kirin.miaosha.util.ValidatorUtil;

public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

	private boolean required = false;
	
	public void initialize(IsMobile constraintAnnotation) {
		required = constraintAnnotation.required();
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(required) { //在必须有值的情况下
			return ValidatorUtil.isMobile(value);
		}else {
			if(StringUtils.isEmpty(value)) { //在不要求有值的情况下
				return true; //空值是允许的
			}else { //有值就给它判断
				return ValidatorUtil.isMobile(value);
			}
		}
	}
}
