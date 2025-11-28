package org.orkg.testing.spring.restdocs

import org.springframework.restdocs.constraints.Constraint
import kotlin.reflect.KClass

interface DocumentationContextProvider {
    val typeMappings: Map<KClass<*>, String> get() = emptyMap()

    fun applyConstraints(constraints: MutableList<Constraint>, type: KClass<*>) = Unit
}
