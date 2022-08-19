package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence

import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchFieldsQuery
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.services.ResearchFieldService
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class ResearchFieldAdapter(
    val findResearchFields: ResearchFieldService
) : FindResearchFieldsQuery {
    override fun withBenchmarks(): List<ResearchField> =
        findResearchFields.withBenchmarks()
}
