package eu.tib.orkg.prototype.statements.adapter.input.rest

import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/papers")
class PaperFeaturedCommandController(
    private val service: ResourceUseCases
) {
    @PutMapping("/{id}/metadata/featured")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun markFeatured(@PathVariable id: ThingId) {
        service.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun unmarkFeatured(@PathVariable id: ThingId) {
        service.markAsNonFeatured(id)
    }
}
