package org.orkg.testing.spring.restdocs

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

fun FieldDescriptor.enum(enum: KClass<out Enum<*>>): FieldDescriptor =
    attributes(Attributes.Attribute("enumValues", enum.java.enumConstants.map { it.name }))

inline fun <reified T : Enum<*>> FieldDescriptor.enum(): FieldDescriptor =
    enum(T::class)

// TODO: re-enable once rest-docs-api allows defining reference schemas
//
//  fun allOf(discriminator: String, mapping: Map<String, KClass<*>>): References =
//      references(Criterion.ALL_OF, discriminator, mapping)
//
//  fun anyOf(discriminator: String, mapping: Map<String, KClass<*>>): References =
//      references(Criterion.ANY_OF, discriminator, mapping)
//
//  fun oneOf(discriminator: String, mapping: Map<String, KClass<*>>): References =
//      references(Criterion.ONE_OF, discriminator, mapping)
//
//  private fun references(criterion: Criterion, discriminator: String, mapping: Map<String, KClass<*>>): References =
//      References(
//          criterion = criterion,
//          schemaNames = mapping.values.map { it.simpleName!! },
//          discriminator = Discriminator(discriminator, mapping.mapValues { (_, value) -> value.simpleName!! }),
//      )
