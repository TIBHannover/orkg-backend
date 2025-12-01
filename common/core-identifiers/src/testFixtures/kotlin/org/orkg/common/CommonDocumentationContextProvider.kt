package org.orkg.common

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.validation.NullableNotBlank
import org.orkg.testing.spring.restdocs.DocumentationContextProvider
import org.springframework.boot.test.context.TestComponent
import org.springframework.restdocs.constraints.Constraint
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.reflect.KClass

@TestComponent
class CommonDocumentationContextProvider : DocumentationContextProvider {
    override val typeMappings: Map<KClass<*>, String> get() = mapOf(
        ThingId::class to "string",
        UUID::class to "string",
        Integer::class to "integer",
        Long::class to "integer",
        URI::class to "string",
        ParsedIRI::class to "string",
        OffsetDateTime::class to "string",
        ContributorId::class to "string",
        ObservatoryId::class to "string",
        OrganizationId::class to "string",
    )

    override fun applyConstraints(constraints: MutableList<Constraint>, type: KClass<*>) {
        when (type) {
            ThingId::class -> constraints.add(thingIdConstraint)
            UUID::class, ContributorId::class, ObservatoryId::class, OrganizationId::class -> constraints.add(uuidConstraint)
        }
        if (constraints.isNotEmpty()) {
            constraints.toList().forEachIndexed { index, constraint ->
                if (constraint.name == NullableNotBlank::class.qualifiedName) {
                    constraints[index] = nullableNotBlankPatternConstraint
                }
            }
        }
    }

    override fun resolveFormat(type: KClass<*>): String? =
        when (type) {
            UUID::class, ContributorId::class, ObservatoryId::class, OrganizationId::class -> "uuid"
            URI::class -> "uri"
            ParsedIRI::class -> "iri"
            Byte::class -> "byte"
            Integer::class -> "int32"
            Long::class -> "int64"
            Float::class -> "float"
            Double::class -> "double"
            OffsetDateTime::class -> "datetime"
            else -> super.resolveFormat(type)
        }
}
