package eu.tib.orkg.prototype.statements.application.rdf

import eu.tib.orkg.prototype.statements.domain.model.rdf.RdfService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dump")
@CrossOrigin(origins = ["*"])
class RdfController(private val rdfService: RdfService) {
    @GetMapping("/rdf",
        produces = ["application/n-triples"]
    )
    fun dumpToRdf(): ResponseEntity<String> {
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"dump.nt\"")
            .body(rdfService.dumpToNTriple())
    }
}
