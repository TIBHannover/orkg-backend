package eu.tib.orkg.prototype.contributions.application

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.application.ContributorNotFound
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contributors")
class ContributorController(
    val service: ContributorService
) {
    @GetMapping("/{id}")
    fun getContributorById(@PathVariable id: ContributorId): Contributor {
        println("LOL")
        return service.findById(id).orElseThrow { ContributorNotFound(id) }
    }
}
