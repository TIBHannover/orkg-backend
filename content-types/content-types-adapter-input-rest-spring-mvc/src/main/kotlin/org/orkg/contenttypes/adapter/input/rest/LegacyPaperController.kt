package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.mapping.PaperResourceWithPathRepresentationAdapter
import org.orkg.contenttypes.input.LegacyPaperUseCases
import org.orkg.graph.adapter.input.rest.PaperResourceWithPathRepresentation
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/papers", produces = [MediaType.APPLICATION_JSON_VALUE])
class LegacyPaperController(
    private val service: LegacyPaperUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : PaperResourceWithPathRepresentationAdapter {
    @GetMapping(params = ["linked_to"])
    fun findAllPapersRelatedToResource(
        @RequestParam("linked_to", required = true) id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<PaperResourceWithPathRepresentation> =
        service.findAllPapersRelatedToResource(id, pageable)
            .mapToPaperResourceWithPathRepresentation(capabilities)

    @GetMapping(params = ["linkedTo"])
    fun findAllPapersRelatedToResourceLegacy(
        @RequestParam("linkedTo", required = true) id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<PaperResourceWithPathRepresentation> =
        findAllPapersRelatedToResource(id, pageable, capabilities)
}
