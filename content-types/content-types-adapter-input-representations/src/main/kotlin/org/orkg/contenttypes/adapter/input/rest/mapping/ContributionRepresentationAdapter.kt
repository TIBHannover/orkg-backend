package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.ContributionRepresentation
import org.orkg.contenttypes.domain.Contribution
import org.springframework.data.domain.Page
import java.util.Optional

interface ContributionRepresentationAdapter {
    fun Optional<Contribution>.mapToContributionRepresentation(): Optional<ContributionRepresentation> =
        map { it.toContributionRepresentation() }

    fun Page<Contribution>.mapToContributionRepresentation(): Page<ContributionRepresentation> =
        map { it.toContributionRepresentation() }

    fun Contribution.toContributionRepresentation(): ContributionRepresentation =
        ContributionRepresentation(id, label, classes, properties, extractionMethod, createdAt, createdBy, visibility, unlistedBy)
}
