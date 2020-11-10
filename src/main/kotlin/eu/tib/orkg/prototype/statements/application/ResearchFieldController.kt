package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.createPageable
import eu.tib.orkg.prototype.statements.domain.model.ResearchFieldService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?
    ): ResponseEntity<Iterable<Any>> {
        resourceService.findById(fieldId)
            .orElseThrow { ResourceNotFound() }
        val pagination = createPageable(page, items, null, false)
        return ok(service.getResearchProblemsOfField(fieldId, pagination))
    }
}
