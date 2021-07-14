package eu.tib.orkg.prototype.statements.application.rdf

import eu.tib.orkg.prototype.statements.application.ClassController
import eu.tib.orkg.prototype.statements.application.PredicateController
import eu.tib.orkg.prototype.statements.application.ResourceController
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.rdf.RdfService
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rdf")
class RdfController(
    private val rdfService: RdfService,
    private val resourceController: ResourceController,
    private val predicateController: PredicateController,
    private val classController: ClassController
) {
    @GetMapping("/dump",
        produces = ["application/n-triples"]
    )
    fun dumpToRdf(): ResponseEntity<String> {
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"dump.nt\"")
            .body(rdfService.dumpToNTriple())
    }

    @GetMapping("/hints")
    fun getHints(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("type", required = false, defaultValue = "item") type: String,
        pageable: Pageable
    ): Iterable<Thing> {
        return when (type) {
            "property" -> predicateController.findByLabel(searchString, exactMatch, pageable)
            "class" -> classController.findByLabel(searchString, exactMatch, pageable)
            else -> resourceController.findByLabel(searchString, exactMatch, arrayOf(), pageable)
        }
    }
}
