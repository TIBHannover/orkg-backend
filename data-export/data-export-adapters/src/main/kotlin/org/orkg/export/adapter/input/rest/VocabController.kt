package org.orkg.export.adapter.input.rest

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
import java.io.StringWriter
import java.net.URI

private val supportedRdfFormats = listOf(
    RDFFormat.NTRIPLES,
    RDFFormat.RDFXML,
    RDFFormat.N3,
    RDFFormat.JSONLD,
    RDFFormat.TRIG,
    RDFFormat.NQUADS,
    RDFFormat.TURTLE,
    RDFFormat.TURTLE,
).flatMap { format -> format.mimeTypes.map { mimeType -> mimeType to format } }.toMap()

@RestController
@RequestMapping("/api/vocab", produces = [MediaType.APPLICATION_JSON_VALUE])
class VocabController(
    private val service: ExportRDFUseCase,
    private val rdfConfiguration: RdfConfiguration,
) {
    @GetMapping(
        "/resource/{id}",
        produces = [
            "application/n-triples", "text/plain", // N-Triples
            "application/rdf+xml", "application/xml", "text/xml", // RDF XML
            "text/n3", "text/rdf+n3", // N3
            "application/ld+json", // JSON-LD
            "application/trig", "application/x-trig", // TriG
            "application/n-quads", "text/x-nquads", "text/nquads", // N-Quads
            "text/turtle", "application/x-turtle", // Turtle
        ]
    )
    fun resource(
        @PathVariable id: ThingId,
        @RequestHeader("Accept") acceptHeader: String,
        uriComponentsBuilder: UriComponentsBuilder,
    ): ResponseEntity<String> {
        if (!checkAcceptHeader(acceptHeader)) {
            return createRedirectResponse("resource", id.value, uriComponentsBuilder)
        }
        val model = service.rdfModelForResource(id)
            .orElseThrow { ResourceNotFound(id) }
        val response = getRdfSerialization(model, acceptHeader)
        return ResponseEntity.ok()
            .body(response)
    }

    @GetMapping(
        "/predicate/{id}",
        produces = [
            "application/n-triples", "text/plain", // N-Triples
            "application/rdf+xml", "application/xml", "text/xml", // RDF XML
            "text/n3", "text/rdf+n3", // N3
            "application/ld+json", // JSON-LD
            "application/trig", "application/x-trig", // TriG
            "application/n-quads", "text/x-nquads", "text/nquads", // N-Quads
            "text/turtle", "application/x-turtle", // Turtle
        ]
    )
    fun predicate(
        @PathVariable id: ThingId,
        @RequestHeader("Accept") acceptHeader: String,
        uriComponentsBuilder: UriComponentsBuilder,
    ): ResponseEntity<String> {
        if (!checkAcceptHeader(acceptHeader)) {
            return createRedirectResponse("predicate", id.value, uriComponentsBuilder)
        }
        val model = service.rdfModelForPredicate(id)
            .orElseThrow { PredicateNotFound(id) }
        val response = getRdfSerialization(model, acceptHeader)
        return ResponseEntity.ok()
            .body(response)
    }

    @GetMapping(
        "/class/{id}",
        produces = [
            "application/n-triples", "text/plain", // N-Triples
            "application/rdf+xml", "application/xml", "text/xml", // RDF XML
            "text/n3", "text/rdf+n3", // N3
            "application/ld+json", // JSON-LD
            "application/trig", "application/x-trig", // TriG
            "application/n-quads", "text/x-nquads", "text/nquads", // N-Quads
            "text/turtle", "application/x-turtle", // Turtle
        ]
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

    private fun checkAcceptHeader(acceptHeader: String): Boolean = (
        acceptHeader.split(Regex(";|,"))[0] in supportedRdfFormats
    )

    private fun createRedirectResponse(
        destination: String,
        id: String,
        uriComponentsBuilder: UriComponentsBuilder,
    ): ResponseEntity<String> = ResponseEntity
        .status(HttpStatus.TEMPORARY_REDIRECT)
        .location(
            uriComponentsBuilder
                .uri(URI.create(rdfConfiguration.frontendUri!!))
                .path("/$destination/{id}")
                .buildAndExpand(id)
                .toUri()
        ).build()

    private fun getRdfSerialization(model: Model, accept: String): String {
        val writer = StringWriter()
        val format = supportedRdfFormats.getOrDefault(accept.split(Regex(";|,"))[0], RDFFormat.TURTLE)
        Rio.write(model, writer, format)
        return writer.toString()
    }
}
