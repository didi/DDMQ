package com.didi.carrera.console.web.controller.validator;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = AnotherFieldEqualsSpecifiedValueValidator.class)
@Documented
public @interface AnotherFieldEqualsSpecifiedValue {
    String message() default "{constraints.fieldmatch}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String fieldName();
    String fieldValue();
    String dependFieldName();

    /**
     * Defines several <code>@AnotherFieldEqualsSpecifiedValue</code> annotations on the same element
     *
     * @see AnotherFieldEqualsSpecifiedValue
     */
    @Target({TYPE, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        AnotherFieldEqualsSpecifiedValue[] value();
    }
}