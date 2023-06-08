package eu.tib.orkg.prototype.community

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface ResearchFieldRepresentationAdapter {
    fun Resource?.toResearchFieldRepresentation(): ResearchFieldRepresentation =
        object : ResearchFieldRepresentation {
            override val id: ThingId? = this@toResearchFieldRepresentation?.id
            override val label: String? = this@toResearchFieldRepresentation?.label
        }
}
