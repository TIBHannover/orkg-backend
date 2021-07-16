package eu.tib.orkg.prototype.contributions.application

import eu.tib.orkg.prototype.contributions.application.ports.input.RetrieveContributorUseCase
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.ContributorNotFound
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contributors")
class ContributorController(
    val retrieveContributor: RetrieveContributorUseCase
) {
    @GetMapping("/{id}")
    fun getContributorById(@PathVariable id: ContributorId): Contributor =
        retrieveContributor.byId(id)
            .orElseThrow { ContributorNotFound(id) }

    @GetMapping("/")
    fun findContributors(): List<Contributor> {
        return retrieveContributor.findContributors()
    }
}
