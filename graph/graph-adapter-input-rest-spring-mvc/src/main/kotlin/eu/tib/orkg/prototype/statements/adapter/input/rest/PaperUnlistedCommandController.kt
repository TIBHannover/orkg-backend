package eu.tib.orkg.prototype.statements.adapter.input.rest

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.shared.annotations.PreAuthorizeCurator
import eu.tib.orkg.prototype.statements.application.BaseController
import eu.tib.orkg.prototype.statements.application.port.`in`.MarkAsUnlistedService
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/papers", produces = [MediaType.APPLICATION_JSON_VALUE])
class PaperUnlistedCommandController(
    @Qualifier("resourceService")
    private val service: MarkAsUnlistedService
) : BaseController() {
    @PutMapping("/{id}/metadata/unlisted")
    @PreAuthorizeCurator
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun markUnlisted(@PathVariable id: ThingId) {
        service.markAsUnlisted(id, ContributorId(authenticatedUserId()))
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    @PreAuthorizeCurator
    fun markListed(@PathVariable id: ThingId) {
        service.markAsListed(id)
    }
}
