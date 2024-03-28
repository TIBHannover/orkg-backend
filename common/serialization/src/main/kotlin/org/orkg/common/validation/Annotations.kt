package org.orkg.common.validation

import java.lang.annotation.Repeatable
import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.Pattern
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
