package org.orkg.dataimport.testing.fixtures

import org.orkg.common.uuidConstraint
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.testing.spring.restdocs.DocumentationContextProvider
import org.springframework.boot.test.context.TestComponent
import org.springframework.restdocs.constraints.Constraint
import kotlin.reflect.KClass

@TestComponent
class DataImportDocumentationContextProvider : DocumentationContextProvider {
    override val typeMappings: Map<KClass<*>, String> get() = mapOf(
        CSVID::class to "string",
        JobId::class to "integer",
    )

    override fun applyConstraints(constraints: MutableList<Constraint>, type: KClass<*>) {
        when (type) {
            CSVID::class -> constraints.add(uuidConstraint)
        }
    }
}
