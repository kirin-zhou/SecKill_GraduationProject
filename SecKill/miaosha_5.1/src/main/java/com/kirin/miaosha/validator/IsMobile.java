package com.kirin.miaosha.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER }) //能够标注的范围
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsMobileValidator.class }) //这个注解帮助我们处理逻辑，其中IsMobileValidator.class是真正处理逻辑的类
public @interface IsMobile { //根据已有注解@NotNull，仿写而来
	
	boolean required() default true;
	
	String message() default "手机号码格式错误"; //添加错误信息

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
