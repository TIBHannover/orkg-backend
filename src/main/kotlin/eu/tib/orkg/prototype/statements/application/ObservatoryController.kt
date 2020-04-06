package eu.tib.orkg.prototype.statements.application
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/observatories/")
@CrossOrigin(origins = ["*"])
class ObservatoryController(private val service: ObservatoryService) {

    @PostMapping("/")
    fun addObservatory(@RequestBody observatory: CreateObservatoryRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        if (service.findByName(observatory.observatoryName).isEmpty) {
            val id = service.create(observatory.observatoryName, observatory.organizationId).id
            // return "Observatory created successfully"
            val location = uriComponentsBuilder
                .path("api/observatories/{id}")
                .buildAndExpand(id)
                .toUri()
            return ResponseEntity.created(location).body(service.findById(id!!).get())
        } else
            return ResponseEntity.badRequest().body("Observatory already exist")
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ObservatoryEntity? =
        service
            .findById(id)
            .orElseThrow()

    @GetMapping("/")
    fun listObservatories(): List<ObservatoryEntity> {
        return service.listObservatories()
    }

    @GetMapping("search/")
    fun listObservatoriesByOrganization(@RequestParam("id", required = true) searchString: UUID): List<ObservatoryEntity> {
        return service.listObservatoriesByOrganizationId(searchString)
    }

    data class CreateObservatoryRequest(
        val observatoryName: String,
        val organizationId: UUID
    )
}
