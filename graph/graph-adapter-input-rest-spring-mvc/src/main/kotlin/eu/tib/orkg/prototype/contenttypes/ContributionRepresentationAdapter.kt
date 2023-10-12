package eu.tib.orkg.prototype.contenttypes;

import eu.tib.orkg.prototype.contenttypes.api.ContributionRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.Contribution
import java.util.*
import org.springframework.data.domain.Page

interface ContributionRepresentationAdapter {

    fun Optional<Contribution>.mapToContributionRepresentation() : Optional<ContributionRepresentation> =
        map { it.toContributionRepresentation() }

    fun Page<Contribution>.mapToContributionRepresentation() : Page<ContributionRepresentation> =
        map { it.toContributionRepresentation() }

    fun Contribution.toContributionRepresentation() : ContributionRepresentation =
        ContributionRepresentation(id, label, classes, properties, visibility)
}
