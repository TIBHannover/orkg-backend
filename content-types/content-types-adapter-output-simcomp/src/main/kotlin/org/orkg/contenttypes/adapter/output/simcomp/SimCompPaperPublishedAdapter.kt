package org.orkg.contenttypes.adapter.output.simcomp

import org.orkg.common.MediaTypeCapabilities
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.adapter.output.simcomp.mapping.PublishedContentTypeRepresentationAdapter
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.output.PaperPublishedRepository
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.stereotype.Component

@Component
class SimCompPaperPublishedAdapter(
    override val formattedLabelService: FormattedLabelUseCases,
    override val statementService: StatementUseCases,
    private val repository: SimCompThingRepository,
) : PaperPublishedRepository,
    PublishedContentTypeRepresentationAdapter {
    override fun save(paper: PublishedContentType) {
        repository.save(
            id = paper.id,
            type = ThingType.PAPER_VERSION,
            data = paper.toPublishedContentTypeRepresentation(MediaTypeCapabilities.EMPTY)
        )
    }
}
