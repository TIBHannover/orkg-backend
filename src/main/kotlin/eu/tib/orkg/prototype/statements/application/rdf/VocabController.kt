package eu.tib.orkg.prototype.statements.application.rdf

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.io.StringWriter
import java.net.URI

@RestController
@RequestMapping("/vocab")
@CrossOrigin(origins = ["*"])
class VocabController(
    private val resourceService: ResourceService,
    private val predicateService: PredicateService,
    private val classService: ClassService
) {

    @GetMapping(
        "/resource/{id}",
        produces = ["text/plain", "application/n-triples", "application/rdf+xml", "text/n3", "text/turtle", "application/json", "application/turtle"]
    )
    fun resource(
        @PathVariable id: ResourceId,
        @RequestHeader("Accept") acceptHeader: String,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<String> {
        if (!checkAcceptHeader(acceptHeader))
            return createRedirectResponse("resource", id.value, uriComponentsBuilder)
        val resource = resourceService.findById(id)
        val response = getRdfSerialization(resource.get().rdf, acceptHeader)
        return ResponseEntity.ok()
            .body(response)
    }

    @GetMapping(
        "/predicate/{id}",
        produces = ["text/plain", "application/n-triples", "application/rdf+xml", "text/n3", "text/turtle", "application/json", "application/turtle"]
    )
    fun predicate(
        @PathVariable id: PredicateId,
        @RequestHeader("Accept") acceptHeader: String,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<String> {
        if (!checkAcceptHeader(acceptHeader))
            return createRedirectResponse("predicate", id.value, uriComponentsBuilder)
        val predicate = predicateService.findById(id)
        val response = getRdfSerialization(predicate.get().rdf, acceptHeader)
        return ResponseEntity.ok()
            .body(response)
    }

    @GetMapping(
        "/class/{id}",
        produces = ["text/plain", "application/n-triples", "application/rdf+xml", "text/n3", "text/turtle", "application/json", "application/turtle"]
    )
    fun `class`(
        @PathVariable id: ClassId,
        @RequestHeader("Accept") acceptHeader: String,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<String> {
        if (!checkAcceptHeader(acceptHeader))
            return createRedirectResponse("class", id.value, uriComponentsBuilder)
        val clazz = classService.findById(id)
        val response = getRdfSerialization(clazz.get().rdf, acceptHeader)
        return ResponseEntity.ok()
            .body(response)
    }

    private fun checkAcceptHeader(acceptHeader: String): Boolean {
        return (acceptHeader in arrayOf("application/n-triples", "application/rdf+xml", "text/n3", "text/turtle", "application/json", "application/turtle"))
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
                    .uri(URI.create(FRONTEND_URI))
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
            else -> RDFFormat.TURTLE
        }
        Rio.write(model, writer, format)
        return writer.toString()
    }
}
