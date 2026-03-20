package org.orkg.contenttypes.adapter.output.simcomp

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.adapter.output.simcomp.mapping.PublishedContentTypeRepresentationAdapter
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.output.PaperPublishedRepository
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.treeToValue
import java.util.Optional

@Component
class SimCompPaperPublishedAdapter(
    override val formattedLabelService: FormattedLabelUseCases,
    override val statementService: StatementUseCases,
    private val repository: SimCompThingRepository,
    private val objectMapper: ObjectMapper,
) : PaperPublishedRepository,
    PublishedContentTypeRepresentationAdapter {
    override fun findById(id: ThingId): Optional<List<GeneralStatement>> =
        repository.findById(id, ThingType.PAPER_VERSION)
            .map { it.data["statements"]?.let { objectMapper.treeToValue<List<GeneralStatement>>(it) }.orEmpty() }

    override fun save(paper: PublishedContentType) {
        repository.save(
            id = paper.id,
            type = ThingType.PAPER_VERSION,
            data = paper.toPublishedContentTypeRepresentation(MediaTypeCapabilities.EMPTY),
        )
    }
}
