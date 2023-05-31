package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.PaperResourceWithPathRepresentation
import eu.tib.orkg.prototype.statements.PaperResourceWithPathRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.ResourceRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.CreatePaperUseCase
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.PaperService
import eu.tib.orkg.prototype.statements.services.ResourceService
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
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
class PaperController(
    private val service: PaperService,
    private val resourceService: ResourceService,
    override val statementService: StatementUseCases,
    override val templateRepository: TemplateRepository,
    override val flags: FeatureFlagService,
) : BaseController(), PaperResourceWithPathRepresentationAdapter, ResourceRepresentationAdapter {

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @RequestBody paper: CreatePaperUseCase.CreatePaperRequest,
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
