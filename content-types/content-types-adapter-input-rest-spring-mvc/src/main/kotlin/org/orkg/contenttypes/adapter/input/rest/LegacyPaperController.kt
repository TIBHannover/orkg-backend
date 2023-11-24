package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.LegacyCreatePaperUseCase
import org.orkg.contenttypes.input.LegacyPaperUseCases
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.BaseController
import org.orkg.graph.adapter.input.rest.mapping.PaperResourceWithPathRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.input.PaperResourceWithPathRepresentation
import org.orkg.graph.input.ResourceRepresentation
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.TemplateRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/papers/", produces = [MediaType.APPLICATION_JSON_VALUE])
class LegacyPaperController(
    private val service: LegacyPaperUseCases,
    private val resourceService: ResourceUseCases,
    override val statementService: StatementUseCases,
    override val templateRepository: TemplateRepository,
    override val flags: FeatureFlagService,
) : BaseController(), PaperResourceWithPathRepresentationAdapter, ResourceRepresentationAdapter {

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @RequestBody paper: LegacyCreatePaperUseCase.LegacyCreatePaperRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam("mergeIfExists", required = false, defaultValue = "false") mergeIfExists: Boolean
    ): ResponseEntity<ResourceRepresentation> {
        val id = service.addPaperContent(paper, mergeIfExists, authenticatedUserId())
        val location = uriComponentsBuilder
            .path("api/resources/")
            .buildAndExpand(id)
            .toUri()
        return ResponseEntity.created(location).body(resourceService.findById(id).mapToResourceRepresentation().get())
    }

    @GetMapping("/")
    fun findPaperResourcesRelatedTo(
        @RequestParam("linkedTo", required = true) id: ThingId,
        pageable: Pageable
    ): Page<PaperResourceWithPathRepresentation> =
        service.findPapersRelatedToResource(id, pageable).mapToPaperResourceWithPathRepresentation()
}
