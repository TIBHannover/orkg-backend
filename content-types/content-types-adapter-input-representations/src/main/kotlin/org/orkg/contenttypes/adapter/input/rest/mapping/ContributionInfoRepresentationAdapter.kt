package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.ContributionInfoRepresentation
import org.orkg.contenttypes.domain.ContributionInfo
import org.springframework.data.domain.Page
import java.util.Optional

interface ContributionInfoRepresentationAdapter {
    fun Optional<ContributionInfo>.mapToContributionInfoRepresentation(): Optional<ContributionInfoRepresentation> =
        map { it.toContributionInfoRepresentation() }

    fun Page<ContributionInfo>.mapToContributionInfoRepresentation(): Page<ContributionInfoRepresentation> =
        map { it.toContributionInfoRepresentation() }

    fun ContributionInfo.toContributionInfoRepresentation(): ContributionInfoRepresentation =
        ContributionInfoRepresentation(id, label, paperTitle, paperYear, paperId)
}
