package org.orkg.graph.adapter.input.rest.testing.fixtures

import org.orkg.graph.domain.StatementId
import org.orkg.testing.spring.restdocs.DocumentationContextProvider
import org.springframework.boot.test.context.TestComponent
import org.springframework.restdocs.constraints.Constraint
import kotlin.reflect.KClass

@TestComponent
class GraphDocumentationContextProvider : DocumentationContextProvider {
    override val typeMappings: Map<KClass<*>, String> get() = mapOf(
        StatementId::class to "string",
    )

    override fun applyConstraints(constraints: MutableList<Constraint>, type: KClass<*>) {
        when (type) {
            StatementId::class -> constraints.add(statementIdConstraint)
        }
    }
}
