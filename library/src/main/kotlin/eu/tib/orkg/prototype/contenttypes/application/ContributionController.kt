package eu.tib.orkg.prototype.contenttypes.application

import eu.tib.orkg.prototype.contenttypes.api.ContributionRepresentation
import eu.tib.orkg.prototype.contenttypes.api.ContributionUseCases
import eu.tib.orkg.prototype.statements.application.BaseController
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/content-types/contributions/")
class ContributionController(
    private val service: ContributionUseCases
) : BaseController() {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId
    ): ContributionRepresentation = service.findById(id)

    @GetMapping("/")
    fun findAll(pageable: Pageable): Page<ContributionRepresentation> = service.findAll(pageable)
}