package org.orkg.export.adapter.input.rest

import org.orkg.common.annotations.RequireAdminRole
import org.orkg.export.input.ExportRDFUseCase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.task.TaskExecutor
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class RdfController(
    private val rdfService: ExportRDFUseCase,
    private val taskExecutor: TaskExecutor,
    @Value("\${orkg.export.rdf.file-name:#{null}}")
    private val path: String?,
) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

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
        taskExecutor.execute {
            logger.info("Starting rdf dump...")
            try {
                rdfService.dumpToNTriple(path)
                logger.info("Finished rdf dump")
            } catch (e: Exception) {
                logger.error("Error creating rdf dump", e)
            }
        }
        return noContent().build()
    }

    companion object {
        private const val BASE_ENDPOINT = "/api/rdf"
        const val DUMP_ENDPOINT = "$BASE_ENDPOINT/dump"
    }
}
