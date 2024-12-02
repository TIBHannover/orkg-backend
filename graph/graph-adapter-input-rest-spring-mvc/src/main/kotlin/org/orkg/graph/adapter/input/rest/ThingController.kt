package org.orkg.graph.adapter.input.rest

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.ThingRepresentationAdapter
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.RetrieveThingUseCase
import org.orkg.graph.input.StatementUseCases
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/things", produces = [MediaType.APPLICATION_JSON_VALUE])
class ThingController(
    private val service: RetrieveThingUseCase,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
    override val flags: FeatureFlagService
) : ThingRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ThingId, capabilities: MediaTypeCapabilities): ThingRepresentation =
        service.findById(id).mapToThingRepresentation(capabilities).orElseThrow { ThingNotFound(id) }
}
