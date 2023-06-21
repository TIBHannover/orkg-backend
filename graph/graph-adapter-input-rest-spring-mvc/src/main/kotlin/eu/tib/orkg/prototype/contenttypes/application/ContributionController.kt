package eu.tib.orkg.prototype.contenttypes.application

import eu.tib.orkg.prototype.contenttypes.ContributionRepresentationAdapter
import eu.tib.orkg.prototype.contenttypes.api.ContributionUseCases
import eu.tib.orkg.prototype.statements.application.BaseController
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val CONTRIBUTION_JSON_V2 = "application/vnd.orkg.contribution.v2+json"

@RestController
@RequestMapping("/api/contributions/", produces = [MediaType.APPLICATION_JSON_VALUE])
class ContributionController(
    private val service: ContributionUseCases
) : BaseController(), ContributionRepresentationAdapter {
    @GetMapping("/{id}", produces = [CONTRIBUTION_JSON_V2])
    fun findById(
        @PathVariable id: ThingId
    ): ContributionRepresentation =
        service.findById(id).toContributionRepresentation()

    @GetMapping("/", produces = [CONTRIBUTION_JSON_V2])
    fun findAll(pageable: Pageable): Page<ContributionRepresentation> =
        service.findAll(pageable).mapToContributionRepresentation()
}
