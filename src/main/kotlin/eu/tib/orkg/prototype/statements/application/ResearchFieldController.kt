package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.ResearchFieldService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/research-fields/")
class ResearchFieldController(
    private val service: ResearchFieldService,
    private val resourceService: ResourceService
) {

    @GetMapping("/{fieldId}/problems")
    fun getResearchProblemsOfField(
        @PathVariable fieldId: ResourceId,
        pageable: Pageable
    ): ResponseEntity<Iterable<Any>> {
        resourceService.findById(fieldId)
            .orElseThrow { ResourceNotFound() }
        return ok(service.getResearchProblemsOfField(fieldId, pageable))
    }
}
