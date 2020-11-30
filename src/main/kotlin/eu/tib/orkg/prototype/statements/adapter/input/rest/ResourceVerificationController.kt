package eu.tib.orkg.prototype.statements.adapter.input.rest

import eu.tib.orkg.prototype.statements.application.BaseController
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.application.port.`in`.MarkAsVerifiedUseCase
import eu.tib.orkg.prototype.statements.application.port.out.LoadResourcePort
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/resources")
class ResourceVerificationController(
    private val service: MarkAsVerifiedUseCase, // FIXME: should be service
    private val adapter: LoadResourcePort // FIXME: should be adapter
) : BaseController() {

    // TODO: Those should most likely go to a paper controller, as the use case on other entities is undefined.

    @GetMapping("/", params = ["verified=true"])
    fun loadVerifiedResources(pageable: Pageable): Page<Resource> =
        adapter.loadVerifiedResources(pageable)

    @GetMapping("/", params = ["verified=false"])
    fun loadUnverifiedResources(pageable: Pageable): Page<Resource> =
        adapter.loadUnverifiedResources(pageable)

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
