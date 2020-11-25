package eu.tib.orkg.prototype.statements.adapter.input.rest

import eu.tib.orkg.prototype.statements.application.BaseController
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.application.port.input.MarkAsVerifiedUseCase
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/resources")
class ResourceVerificationController(
    private val service: MarkAsVerifiedUseCase
) : BaseController() {

    @PutMapping("/{id}/verified")
    @ResponseStatus(NO_CONTENT)
    fun markVerified(@PathVariable id: ResourceId) {
        service
            .markAsVerified(id)
            .orElseThrow { ResourceNotFound(id.toString()) }
    }

    @DeleteMapping("/{id}/verified")
    @ResponseStatus(NO_CONTENT)
    fun unmarkVerified(@PathVariable id: ResourceId) {
        service
            .markAsUnverified(id)
            .orElseThrow { ResourceNotFound(id.toString()) }
    }
}
