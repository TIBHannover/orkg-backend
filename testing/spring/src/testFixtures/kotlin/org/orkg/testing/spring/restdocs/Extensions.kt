package org.orkg.testing.spring.restdocs

import com.epages.restdocs.apispec.Criterion
import com.epages.restdocs.apispec.Discriminator
import com.epages.restdocs.apispec.References
import org.springframework.restdocs.constraints.Constraint
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.snippet.Attributes
import kotlin.reflect.KClass

fun FieldDescriptor.references(schemaClass: KClass<*>): FieldDescriptor =
    attributes(Attributes.Attribute("schemaName", schemaClass.simpleName!!))

fun FieldDescriptor.referencesPageOf(schemaClass: KClass<*>): FieldDescriptor =
    attributes(Attributes.Attribute("schemaName", "PageOf${schemaClass.simpleName!!}s"))

inline fun <reified T> FieldDescriptor.references(): FieldDescriptor =
    references(T::class)

inline fun <reified T> FieldDescriptor.referencesPageOf(): FieldDescriptor =
    referencesPageOf(T::class)

fun FieldDescriptor.arrayItemsType(type: String): FieldDescriptor =
    attributes(Attributes.Attribute("itemsType", type))

fun FieldDescriptor.enumValues(values: List<String>): FieldDescriptor =
    attributes(Attributes.Attribute("enumValues", values))

fun FieldDescriptor.enumValues(enum: Class<out Enum<*>>): FieldDescriptor =
    enumValues(enum.enumConstants.map { it.name })

fun FieldDescriptor.enumValues(enum: KClass<out Enum<*>>): FieldDescriptor =
    enumValues(enum.java)

inline fun <reified T : Enum<*>> FieldDescriptor.enumValues(): FieldDescriptor =
    enumValues(T::class)

fun FieldDescriptor.constraints(vararg constraints: Constraint): FieldDescriptor =
    constraints(constraints.toList())

fun FieldDescriptor.constraints(constraints: List<Constraint>): FieldDescriptor {
    @Suppress("UNCHECKED_CAST")
    attributes.getOrPut("validationConstraints") { mutableListOf<Constraint>() } as MutableList<Constraint> += constraints
    return this
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
