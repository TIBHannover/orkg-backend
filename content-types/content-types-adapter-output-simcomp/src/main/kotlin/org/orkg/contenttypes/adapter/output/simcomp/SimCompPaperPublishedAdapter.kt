package org.orkg.contenttypes.adapter.output.simcomp

import org.orkg.common.MediaTypeCapabilities
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.output.PaperPublishedRepository
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.StatementRepresentationAdapter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.stereotype.Component

@Component
class SimCompPaperPublishedAdapter(
    override val formattedLabelService: FormattedLabelUseCases,
    override val statementService: StatementUseCases,
    override val flags: FeatureFlagService,
    private val repository: SimCompThingRepository
) : PaperPublishedRepository, StatementRepresentationAdapter {
    override fun save(paper: PublishedContentType) {
        repository.save(
            id = paper.rootId,
            type = ThingType.PAPER_VERSION,
            data = mapOf(
                "statements" to paper.subgraph.mapToStatementRepresentation(MediaTypeCapabilities.EMPTY)
            )
        )
    }
}
