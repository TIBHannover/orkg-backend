package org.orkg.testing.spring.restdocs

import org.springframework.boot.test.context.TestComponent
import org.springframework.restdocs.constraints.Constraint
import kotlin.reflect.KClass

@TestComponent
class DocumentationContext(
    private val documentationContextProviders: List<DocumentationContextProvider>,
) {
    private val cache: MutableMap<KClass<*>, String> = mutableMapOf()

    fun resolveTypeName(type: KClass<*>): String =
        cache.getOrPut(type) {
            documentationContextProviders.forEach { contextProvider ->
                if (contextProvider.typeMappings.containsKey(type)) {
                    return@getOrPut contextProvider.typeMappings[type]!!
                }
            }
            return@getOrPut type.simpleName!!
        }

    fun applyConstraints(constraints: MutableList<Constraint>, type: KClass<*>) {
        documentationContextProviders.forEach {
            it.applyConstraints(constraints, type)
        }
    }

    fun resolveFormat(type: KClass<*>): String? {
        documentationContextProviders.forEach {
            val format = it.resolveFormat(type)
            if (format != null) {
                return format
            }
        }
        return null
    }

    fun hasTypeMapping(type: KClass<*>): Boolean =
        resolveTypeName(type) != type.simpleName
}
