package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.graph.adapter.input.rest.SubgraphContributionRepresentation
import org.orkg.graph.domain.SubgraphContribution
import org.springframework.data.domain.Page
import java.util.Optional

interface SubgraphContributionRepresentationAdapter {
    fun Optional<SubgraphContribution>.mapToSubgraphContributionRepresentation(): Optional<SubgraphContributionRepresentation> =
        map { it.toSubgraphContributionRepresentation() }

    fun Page<SubgraphContribution>.mapToSubgraphContributionRepresentation(): Page<SubgraphContributionRepresentation> =
        map { it.toSubgraphContributionRepresentation() }

    fun SubgraphContribution.toSubgraphContributionRepresentation(): SubgraphContributionRepresentation =
        SubgraphContributionRepresentation(createdBy, createdAt)
}
