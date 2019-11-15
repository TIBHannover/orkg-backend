package eu.tib.orkg.prototype.statements.application.rdf

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
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

const val FRONTEND_URI = "http://localhost:3000"

@RestController
@RequestMapping("/vocab")
@CrossOrigin(origins = ["*"])
class VocabController(private val resourceService: ResourceService) {

    @GetMapping("/resource/{id}",
        produces = ["text/plain", "application/n-triples", "application/rdf+xml", "text/n3", "text/turtle", "application/json", "application/turtle"]
    )
    fun resource(
        @PathVariable id: ResourceId,
        @RequestHeader("Accept") accept: String,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<String> {
        if (accept !in arrayOf("application/n-triples", "application/rdf+xml", "text/n3", "text/turtle", "application/json", "application/turtle"))
            return ResponseEntity
                .status(HttpStatus.TEMPORARY_REDIRECT)
                .location(
                    uriComponentsBuilder
                        .uri(URI.create(FRONTEND_URI))
                        .path("/resource/{id}/description")
                        .buildAndExpand(id)
                        .toUri()
                ).build()
        val resource = resourceService.findById(id)
        val writer = StringWriter()
        if (resource.isPresent) {
            val format = when (accept) {
                "application/n-triples" -> RDFFormat.NTRIPLES
                "application/rdf+xml" -> RDFFormat.RDFXML
                "text/n3" -> RDFFormat.N3
                "application/json" -> RDFFormat.JSONLD
                else -> RDFFormat.TURTLE
            }
            Rio.write(resource.get().rdf, writer, format)
        }
        return ResponseEntity.ok()
            .body(writer.toString())
    }
}
