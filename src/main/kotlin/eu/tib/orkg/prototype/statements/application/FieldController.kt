package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.FieldService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/fields/")
class FieldController(
    private val service: FieldService,
    private val resourceService: ResourceService
) {

    @GetMapping("/{fieldId}/problems")
    fun getResearchProblemsOfField(
        @PathVariable fieldId: ResourceId
    ): ResponseEntity<Iterable<Any>> {
        resourceService.findById(fieldId)
            .orElseThrow { ResourceNotFound() }
        return ok(service.getResearchProblemsOfField(fieldId))
    }
}
