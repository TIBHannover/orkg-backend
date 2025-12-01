package org.orkg.testing.spring.restdocs

import com.epages.restdocs.apispec.Criterion
import com.epages.restdocs.apispec.Discriminator
import com.epages.restdocs.apispec.ParameterType
import com.epages.restdocs.apispec.References
import jakarta.validation.Payload
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.restdocs.constraints.Constraint
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.snippet.AbstractDescriptor
import org.springframework.restdocs.snippet.Attributes
import kotlin.reflect.KClass

fun <T : AbstractDescriptor<T>> T.references(schemaClass: KClass<*>): T =
    attributes(Attributes.Attribute("schemaName", schemaClass.simpleName!!))

fun <T : AbstractDescriptor<T>> T.referencesPageOf(schemaClass: KClass<*>): T =
    attributes(Attributes.Attribute("schemaName", "PageOf${schemaClass.simpleName!!}s"))

inline fun <reified T> FieldDescriptor.references(): FieldDescriptor =
    references(T::class)

inline fun <reified T> FieldDescriptor.referencesPageOf(): FieldDescriptor =
    referencesPageOf(T::class)

fun <T : AbstractDescriptor<T>> T.arrayItemsType(type: String): T =
    attributes(Attributes.Attribute("itemsType", type))

fun <T : AbstractDescriptor<T>> T.enumValues(values: List<String>): T =
    attributes(Attributes.Attribute("enumValues", values))

fun <T : AbstractDescriptor<T>> T.enumValues(enum: Class<out Enum<*>>): T =
    enumValues(enum.enumConstants.map { it.name })

fun <T : AbstractDescriptor<T>> T.enumValues(enum: KClass<out Enum<*>>): T =
    enumValues(enum.java)

fun ParameterDescriptor.enumValues(enum: KClass<out Enum<*>>): ParameterDescriptor =
    enumValues(enum.java).references(enum)

fun <T : AbstractDescriptor<T>> T.constraints(vararg constraints: Constraint): T =
    constraints(constraints.toList())

fun <T : AbstractDescriptor<T>> T.constraints(constraints: List<Constraint>): T {
    @Suppress("UNCHECKED_CAST")
    attributes.getOrPut("validationConstraints") { mutableListOf<Constraint>() } as MutableList<Constraint> += constraints
    return this
}

fun <T : AbstractDescriptor<T>> T.format(format: String): T =
    attributes(Attributes.Attribute("format", format))

fun <T : AbstractDescriptor<T>> T.min(min: Int): T =
    constraints(
        Constraint(
            Min::class.qualifiedName,
            mapOf(
                "value" to min,
                "message" to "{jakarta.validation.constraints.Min.message}",
                "groups" to emptyArray<Class<*>>(),
                "payload" to emptyArray<Class<out Payload>>(),
            )
        )
    )

fun <T : AbstractDescriptor<T>> T.max(max: Int): T =
    constraints(
        Constraint(
            Max::class.qualifiedName,
            mapOf(
                "value" to max,
                "message" to "{jakarta.validation.constraints.Max.message}",
                "groups" to emptyArray<Class<*>>(),
                "payload" to emptyArray<Class<out Payload>>(),
            )
        )
    )

fun <T : AbstractDescriptor<T>> T.size(min: Int = 0, max: Int = Integer.MAX_VALUE): T =
    constraints(
        Constraint(
            Size::class.qualifiedName,
            mapOf(
                "min" to min,
                "max" to max,
                "message" to "{jakarta.validation.constraints.Size.message}",
                "groups" to emptyArray<Class<*>>(),
                "payload" to emptyArray<Class<out Payload>>(),
            )
        )
    )

inline fun <reified T> FieldDescriptor.type() =
    type(T::class.java)

fun ParameterDescriptor.type(type: ParameterType) =
    attributes(Attributes.Attribute("type", type))

fun ParameterDescriptor.style(style: String) =
    attributes(Attributes.Attribute("style", style))

fun ParameterDescriptor.explode(explode: Boolean) =
    attributes(Attributes.Attribute("explode", explode))

fun ParameterDescriptor.repeatable(
    type: ParameterType = ParameterType.STRING,
    explode: Boolean = false,
    style: String? = null,
) =
    type(ParameterType.ARRAY)
        .arrayItemsType(type.name)
        .explode(explode)
        .apply { if (style != null) style(style) }

fun ParameterDescriptor.wildcard(examples: Map<String, Any?>? = null): ParameterDescriptor =
    attributes(Attributes.Attribute("wildcard", true)).apply {
        if (examples != null) {
            attributes(Attributes.Attribute("wildcardExamples", examples))
        }
    }

fun allOf(discriminator: String, mapping: Map<String, KClass<*>>): References =
    references(Criterion.ALL_OF, discriminator, mapping)

fun anyOf(discriminator: String, mapping: Map<String, KClass<*>>): References =
    references(Criterion.ANY_OF, discriminator, mapping)

fun oneOf(discriminator: String, mapping: Map<String, KClass<*>>): References =
    references(Criterion.ONE_OF, discriminator, mapping)

private fun references(criterion: Criterion, discriminator: String, mapping: Map<String, KClass<*>>): References =
    References(
        criterion = criterion,
        schemaNames = mapping.values.map { it.simpleName!! },
        discriminator = Discriminator(discriminator, mapping.mapValues { (_, value) -> value.simpleName!! }),
    )

fun allOf(vararg schemas: KClass<*>): References =
    references(Criterion.ALL_OF, schemas)

fun anyOf(vararg schemas: KClass<*>): References =
    references(Criterion.ANY_OF, schemas)

fun oneOf(vararg schemas: KClass<*>): References =
    references(Criterion.ONE_OF, schemas)

private fun references(criterion: Criterion, schemas: Array<out KClass<*>>): References =
    References(
        criterion = criterion,
        schemaNames = schemas.map { it.simpleName!! },
    )

fun <T : AbstractDescriptor<T>> AbstractDescriptor<T>.deprecated(): T =
    description(listOfNotNull("*Deprecated*", description).joinToString(" "))
        .attributes(Attributes.Attribute("deprecated", true))

fun <T : AbstractDescriptor<T>> AbstractDescriptor<T>.deprecated(replaceWith: String): T =
    description(listOfNotNull("*Deprecated*. See `$replaceWith` for replacement.", description).joinToString(" "))
        .attributes(Attributes.Attribute("deprecated", true))
