package orkg.orkg.community.testing.fixtures

import org.orkg.common.uuidConstraint
import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.testing.spring.restdocs.DocumentationContextProvider
import org.springframework.boot.test.context.TestComponent
import org.springframework.restdocs.constraints.Constraint
import kotlin.reflect.KClass

@TestComponent
class CommunityDocumentationContextProvider : DocumentationContextProvider {
    override val typeMappings: Map<KClass<*>, String> get() = mapOf(
        ObservatoryFilterId::class to "string",
        ConferenceSeriesId::class to "string",
    )

    override fun applyConstraints(constraints: MutableList<Constraint>, type: KClass<*>) {
        when (type) {
            ObservatoryFilterId::class, ConferenceSeriesId::class -> constraints.add(uuidConstraint)
        }
    }
}
