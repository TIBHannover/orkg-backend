package eu.tib.orkg.prototype.contributions.application

import eu.tib.orkg.prototype.community.api.RetrieveContributorUseCase
import eu.tib.orkg.prototype.community.domain.model.Contributor
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.ContributorNotFound
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contributors", produces = [MediaType.APPLICATION_JSON_VALUE])
class ContributorController(
    val retrieveContributor: RetrieveContributorUseCase
) {
    @GetMapping("/{id}")
    fun getContributorById(@PathVariable id: ContributorId): Contributor =
        retrieveContributor.findById(id)
            .orElseThrow { ContributorNotFound(id) }
}