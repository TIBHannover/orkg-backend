package eu.tib.orkg.prototype.statements.adapter.input.rest

import eu.tib.orkg.prototype.statements.application.port.`in`.MarkAsUnlistedService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/papers")
class PaperUnlistedCommandController(
    @Qualifier("resourceService")
    private val service: MarkAsUnlistedService
) {
    @PutMapping("/{id}/metadata/unlisted")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun markUnlisted(@PathVariable id: ResourceId) {
        service.markAsUnlisted(id)
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    fun markListed(@PathVariable id: ResourceId) {
        service.markAsListed(id)
    }
}
