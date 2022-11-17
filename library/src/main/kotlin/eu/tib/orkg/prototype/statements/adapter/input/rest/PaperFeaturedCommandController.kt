package eu.tib.orkg.prototype.statements.adapter.input.rest

import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.application.port.`in`.MarkFeaturedService
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
class PaperFeaturedCommandController(
    @Qualifier("resourceService")
    private val service: MarkFeaturedService
) {

    @PutMapping("/{id}/metadata/featured")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun markFeatured(@PathVariable id: ResourceId) {
        service.markAsFeatured(id).orElseThrow { ResourceNotFound(id.toString()) }
    }
    @DeleteMapping("/{id}/metadata/featured")
    fun unmarkFeatured(@PathVariable id: ResourceId) {
        service.markAsNonFeatured(id).orElseThrow { ResourceNotFound(id.toString()) }
    }
}
