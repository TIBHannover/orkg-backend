package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.ContributionRepresentation
import org.orkg.contenttypes.domain.Contribution
import org.springframework.data.domain.Page

interface ContributionRepresentationAdapter {

    fun Optional<Contribution>.mapToContributionRepresentation(): Optional<ContributionRepresentation> =
        map { it.toContributionRepresentation() }

    fun Page<Contribution>.mapToContributionRepresentation(): Page<ContributionRepresentation> =
        map { it.toContributionRepresentation() }

    fun Contribution.toContributionRepresentation(): ContributionRepresentation =
        ContributionRepresentation(id, label, classes, properties, extractionMethod, createdAt, createdBy, visibility, unlistedBy)
}
