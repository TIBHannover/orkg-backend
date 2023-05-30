package eu.tib.orkg.prototype.contenttypes;

import eu.tib.orkg.prototype.contenttypes.application.ContributionRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.Contribution
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import org.springframework.data.domain.Page

interface ContributionRepresentationAdapter {

    fun Page<Contribution>.mapToContributionRepresentation() : Page<ContributionRepresentation> =
        map { it.toContributionRepresentation() }

    fun Contribution.toContributionRepresentation() : ContributionRepresentation =
        object : ContributionRepresentation {
            override val id: ThingId = this@toContributionRepresentation.id
            override val label: String = this@toContributionRepresentation.label
            override val properties: Map<ThingId, List<ThingId>> = this@toContributionRepresentation.properties
            override val visibility: Visibility = this@toContributionRepresentation.visibility
        }
}
