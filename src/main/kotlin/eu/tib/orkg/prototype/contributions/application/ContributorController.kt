package eu.tib.orkg.prototype.contributions.application

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.application.UserNotFound
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contributors")
class ContributorController(
    private val contributorService: ContributorService
) {
    @GetMapping("/{id}")
    fun findContributorByUserId(@PathVariable id: UUID): ResponseEntity<Contributor> {
        val contributor = contributorService.findById(id).orElseThrow { UserNotFound("$id") }
        return ResponseEntity.ok(contributor)
    }

    @GetMapping("/{id}/organizations")
    fun findMemberOrganizationsByUserId(@PathVariable id: UUID): Iterable<Contributor> =
        contributorService.findUsersByOrganizationId(id)
}
