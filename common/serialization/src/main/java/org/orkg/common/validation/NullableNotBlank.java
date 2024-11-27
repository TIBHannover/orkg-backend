package org.orkg.common.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

// This is a copy from NotBlank of Jakarta Validation, with added Pattern.List annotation.
// It was not converted to Kotlin because of a bug in the Kotlin compiler (tested with 2.0.20).
// See below for the original Kotlin class used before.
@Documented
@Constraint(validatedBy = {})
@Pattern.List(value = {
    @Pattern(regexp = "\\s*\\S[\\s\\S]*", message = "{jakarta.validation.constraints.NotBlank.message}")
})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(NullableNotBlank.List.class)
public @interface NullableNotBlank {

    String message() default "{jakarta.validation.constraints.NotBlank.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        NullableNotBlank[] value();
    }
}

/*
import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.Pattern
import java.lang.annotation.Repeatable
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.annotation.AnnotationTarget.TYPE_PARAMETER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

@Pattern.List(value = [
    Pattern(regexp = """\s*\S[\s\S]*""", message = "{javax.validation.constraints.NotBlank.message}")
])
@Constraint(validatedBy = [])
@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, FIELD, ANNOTATION_CLASS, CONSTRUCTOR, VALUE_PARAMETER, CLASS, TYPE, TYPE_PARAMETER)
@Retention(RUNTIME)
@MustBeDocumented
@Repeatable(Pattern.List::class)
annotation class NullableNotBlank(
    val message: String = "{javax.validation.constraints.NotBlank.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Payload>> = []
) {
    @Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, FIELD, ANNOTATION_CLASS, CONSTRUCTOR, VALUE_PARAMETER, CLASS, TYPE, TYPE_PARAMETER)
    @Retention(RUNTIME)
    @MustBeDocumented
    annotation class List(vararg val value: NullableNotBlank)
}
*/
