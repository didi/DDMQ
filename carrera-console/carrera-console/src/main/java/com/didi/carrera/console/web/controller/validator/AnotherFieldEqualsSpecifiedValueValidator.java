package com.didi.carrera.console.web.controller.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;


public class AnotherFieldEqualsSpecifiedValueValidator implements ConstraintValidator<AnotherFieldEqualsSpecifiedValue, Object> {
    private String fieldName;
    private String expectedFieldValue;
    private String dependFieldName;

    @Override
    public void initialize(final AnotherFieldEqualsSpecifiedValue constraintAnnotation) {
        fieldName          = constraintAnnotation.fieldName();
        expectedFieldValue = constraintAnnotation.fieldValue();
        dependFieldName    = constraintAnnotation.dependFieldName();
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            String fieldValue       = BeanUtils.getProperty(value, fieldName);
            String dependFieldValue = BeanUtils.getProperty(value, dependFieldName);

            if (expectedFieldValue.equals(fieldValue) && dependFieldValue == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addNode(dependFieldName)
                        .addConstraintViolation();
                return false;
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        return true;
    }
}