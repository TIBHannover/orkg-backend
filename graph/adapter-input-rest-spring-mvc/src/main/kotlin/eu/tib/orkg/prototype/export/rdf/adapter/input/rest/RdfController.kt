package eu.tib.orkg.prototype.export.rdf.adapter.input.rest

import eu.tib.orkg.prototype.statements.application.ClassController
import eu.tib.orkg.prototype.statements.application.PredicateController
import eu.tib.orkg.prototype.statements.application.ResourceController
import eu.tib.orkg.prototype.export.rdf.api.ExportRDFUseCase
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/rdf", produces = [MediaType.APPLICATION_JSON_VALUE])
class RdfController(
    private val service: ExportRDFUseCase,
    private val resourceController: ResourceController,
    private val predicateController: PredicateController,
    private val classController: ClassController
) {
    @GetMapping("/dump", produces = ["application/n-triples"])
    fun dumpToRdf(uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
            .location(uriComponentsBuilder
                .path("/files/rdf-dumps/rdf-export-orkg.nt")
                .build()
                .toUri())
            .build()

    @GetMapping("/hints")
    fun getHints(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("type", required = false, defaultValue = "item") type: String,
        pageable: Pageable
    ): Iterable<Any> { // FIXME: should be ThingRepresentation
        return when (type) {
            "property" -> predicateController.findByLabel(searchString, exactMatch, pageable)
            "class" -> classController.findByLabel(searchString, exactMatch, pageable)
            else -> resourceController.findByLabel(searchString, exactMatch, setOf(), setOf(), pageable)
        }
    }
}
