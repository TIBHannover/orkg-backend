package eu.tib.orkg.prototype.export.rdf.adapter.input.rest

import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.ClassRepresentationAdapter
import eu.tib.orkg.prototype.statements.PredicateRepresentationAdapter
import eu.tib.orkg.prototype.statements.ResourceRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.api.ThingRepresentation
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
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
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class RdfController(
    private val resourceRepository: ResourceRepository,
    private val predicateRepository: PredicateRepository,
    private val classRepository: ClassRepository,
    override val statementService: StatementUseCases,
    override val templateRepository: TemplateRepository,
    override val flags: FeatureFlagService
) : ResourceRepresentationAdapter, PredicateRepresentationAdapter, ClassRepresentationAdapter {
    @GetMapping(DUMP_ENDPOINT, produces = ["application/n-triples"])
    fun dumpToRdf(uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
            .location(uriComponentsBuilder
                .path("/files/rdf-dumps/rdf-export-orkg.nt")
                .build()
                .toUri())
            .build()

    @GetMapping(HINTS_ENDPOINT)
    fun getHints(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("type", required = false, defaultValue = "item") type: String,
        pageable: Pageable
    ): Iterable<ThingRepresentation> =
        if (searchString != null) {
            val labelSearchString = SearchString.of(searchString, exactMatch = exactMatch)
            when (type) {
                "property" -> predicateRepository.findAllByLabel(labelSearchString, pageable)
                    .mapToPredicateRepresentation()

                "class" -> classRepository.findAllByLabel(labelSearchString, pageable)
                    .mapToClassRepresentation()

                else -> resourceRepository.findAllByLabel(labelSearchString, pageable)
                    .mapToResourceRepresentation()
            }
        } else when (type) {
            "property" -> predicateRepository.findAll(pageable).mapToPredicateRepresentation()
            "class" -> classRepository.findAll(pageable).mapToClassRepresentation()
            else -> resourceRepository.findAll(pageable).mapToResourceRepresentation()
        }

    companion object {
        private const val BASE_ENDPOINT = "/api/rdf"
        const val DUMP_ENDPOINT = "$BASE_ENDPOINT/dump"
        const val HINTS_ENDPOINT = "$BASE_ENDPOINT/hints"
    }
}
