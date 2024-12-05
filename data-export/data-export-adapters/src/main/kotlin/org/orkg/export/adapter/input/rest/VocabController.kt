package org.orkg.export.adapter.input.rest

import java.io.StringWriter
import java.net.URI
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.orkg.common.ThingId
import org.orkg.export.adapter.input.rest.configuration.RdfConfiguration
import org.orkg.export.input.ExportRDFUseCase
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.ResourceNotFound
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/vocab", produces = [MediaType.APPLICATION_JSON_VALUE])
class VocabController(
    private val service: ExportRDFUseCase,
    private val rdfConfiguration: RdfConfiguration,
) {
    @GetMapping(
        "/resource/{id}",
        produces = ["text/plain", "application/n-triples", "application/rdf+xml", "text/n3", "text/turtle", "application/json", "application/turtle", "application/trig", "application/n-quads"]
    )
    fun resource(
        @PathVariable id: ThingId,
        @RequestHeader("Accept") acceptHeader: String,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<String> {
        if (!checkAcceptHeader(acceptHeader))
            return createRedirectResponse("resource", id.value, uriComponentsBuilder)
        val model = service.rdfModelForResource(id)
            .orElseThrow { ResourceNotFound.withId(id) }
        val response = getRdfSerialization(model, acceptHeader)
        return ResponseEntity.ok()
            .body(response)
    }

    @GetMapping(
        "/predicate/{id}",
        produces = ["text/plain", "application/n-triples", "application/rdf+xml", "text/n3", "text/turtle", "application/json", "application/turtle", "application/trig", "application/n-quads"]
    )
    fun predicate(
        @PathVariable id: ThingId,
        @RequestHeader("Accept") acceptHeader: String,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<String> {
        if (!checkAcceptHeader(acceptHeader))
            return createRedirectResponse("predicate", id.value, uriComponentsBuilder)
        val model = service.rdfModelForPredicate(id)
            .orElseThrow { PredicateNotFound(id) }
        val response = getRdfSerialization(model, acceptHeader)
        return ResponseEntity.ok()
            .body(response)
    }

    @GetMapping(
        "/class/{id}",
        produces = ["text/plain", "application/n-triples", "application/rdf+xml", "text/n3", "text/turtle", "application/json", "application/turtle", "application/trig", "application/n-quads"]
    )
    fun `class`(
        @PathVariable id: ThingId,
        @RequestHeader("Accept") acceptHeader: String,
    ): ResponseEntity<String> {
        val model = service.rdfModelForClass(id)
            .orElseThrow { ClassNotFound.withThingId(id) }
        val response = getRdfSerialization(model, acceptHeader)
        return ResponseEntity.ok()
            .body(response)
    }

    private fun checkAcceptHeader(acceptHeader: String): Boolean {
        return (acceptHeader in arrayOf(
            "application/n-triples",
            "application/rdf+xml",
            "text/n3",
            "text/turtle",
            "application/json",
            "application/turtle",
            "application/trig",
            "application/x-trig",
            "application/n-quads",
            "text/x-nquads",
            "text/nquads"
        ))
    }

    private fun createRedirectResponse(
        destination: String,
        id: String,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<String> {
        return ResponseEntity
            .status(HttpStatus.TEMPORARY_REDIRECT)
            .location(
                uriComponentsBuilder
                    .uri(URI.create(rdfConfiguration.frontendUri!!))
                    .path("/$destination/{id}")
                    .buildAndExpand(id)
                    .toUri()
            ).build()
    }

    private fun getRdfSerialization(
        model: Model?,
        accept: String
    ): String {
        val writer = StringWriter()
        val format = when (accept) {
            "application/n-triples" -> RDFFormat.NTRIPLES
            "application/rdf+xml" -> RDFFormat.RDFXML
            "text/n3" -> RDFFormat.N3
            "application/json" -> RDFFormat.JSONLD
            "application/trig" -> RDFFormat.TRIG
            "application/x-trig" -> RDFFormat.TRIG
            "application/n-quads" -> RDFFormat.NQUADS
            "text/x-nquads" -> RDFFormat.NQUADS
            "text/nquads" -> RDFFormat.NQUADS
            else -> RDFFormat.TURTLE
        }
        Rio.write(model, writer, format)
        return writer.toString()
    }
}
