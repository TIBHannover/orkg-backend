package org.orkg.community.testing.fixtures

import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.testing.spring.restdocs.DocumentationContextProvider
import org.springframework.boot.test.context.TestComponent
import kotlin.reflect.KClass

@TestComponent
class CommunityDocumentationContextProvider : DocumentationContextProvider {
    override val typeMappings: Map<KClass<*>, String> get() = mapOf(
        ObservatoryFilterId::class to "string",
        ConferenceSeriesId::class to "string",
    )

    override fun resolveFormat(type: KClass<*>): String? =
        when (type) {
            ObservatoryFilterId::class, ConferenceSeriesId::class -> "uuid"
            else -> super.resolveFormat(type)
        }
}
