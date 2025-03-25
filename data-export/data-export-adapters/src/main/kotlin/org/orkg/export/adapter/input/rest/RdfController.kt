package org.orkg.export.adapter.input.rest

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.annotations.RequireAdminRole
import org.orkg.export.input.ExportRDFUseCase
import org.orkg.graph.adapter.input.rest.ThingRepresentation
import org.orkg.graph.adapter.input.rest.mapping.ClassRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.PredicateRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.SearchString
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.task.TaskExecutor
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
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
    private val rdfService: ExportRDFUseCase,
    private val taskExecutor: TaskExecutor,
    @Value("\${orkg.export.rdf.file-name:#{null}}")
    private val path: String?,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : ResourceRepresentationAdapter,
    PredicateRepresentationAdapter,
    ClassRepresentationAdapter {
    @GetMapping(DUMP_ENDPOINT, produces = ["application/n-triples"])
    fun dumpToRdf(uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
            .location(
                uriComponentsBuilder
                    .path("/files/rdf-dumps/rdf-export-orkg.nt")
                    .build()
                    .toUri()
            )
            .build()

    @RequireAdminRole
    @PostMapping("/api/admin/rdf/dump")
    fun createRdfDump(): ResponseEntity<Any> {
        taskExecutor.execute { rdfService.dumpToNTriple(path) }
        return noContent().build()
    }

    @GetMapping(HINTS_ENDPOINT)
    fun getHints(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("type", required = false, defaultValue = "item") type: String,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Iterable<ThingRepresentation> =
        if (searchString != null) {
            val labelSearchString = SearchString.of(searchString, exactMatch = exactMatch)
            when (type) {
                "property" -> predicateRepository.findAll(label = labelSearchString, pageable = pageable)
                    .mapToPredicateRepresentation()

                "class" -> classRepository.findAll(label = labelSearchString, pageable = pageable)
                    .mapToClassRepresentation()

                else -> resourceRepository.findAll(label = labelSearchString, pageable = pageable)
                    .mapToResourceRepresentation(capabilities)
            }
        } else {
            when (type) {
                "property" -> predicateRepository.findAll(pageable).mapToPredicateRepresentation()
                "class" -> classRepository.findAll(pageable).mapToClassRepresentation()
                else -> resourceRepository.findAll(pageable).mapToResourceRepresentation(capabilities)
            }
        }

    companion object {
        private const val BASE_ENDPOINT = "/api/rdf"
        const val DUMP_ENDPOINT = "$BASE_ENDPOINT/dump"
        const val HINTS_ENDPOINT = "$BASE_ENDPOINT/hints"
    }
}
