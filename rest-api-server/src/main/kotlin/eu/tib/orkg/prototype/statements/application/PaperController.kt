package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.application.service.CreatePaperRequest
import eu.tib.orkg.prototype.statements.application.service.PaperService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/papers/")
class PaperController(
    private val paperService: PaperService
) : BaseController() {
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @RequestBody paper: CreatePaperRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam("mergeIfExists", required = false, defaultValue = "false") mergeIfExists: Boolean
    ): ResponseEntity<ResourceRepresentation> {
        val resource = paperService.addPaperContent(paper, mergeIfExists, authenticatedUserId())
        val location = uriComponentsBuilder.path("api/resources/").buildAndExpand(resource.id).toUri()
        return ResponseEntity.created(location).body(resource)
    }
}
