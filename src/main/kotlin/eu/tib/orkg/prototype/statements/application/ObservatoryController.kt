package eu.tib.orkg.prototype.statements.application
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import java.util.UUID
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
@RestController
@RequestMapping("/api/observatories/")
@CrossOrigin(origins = ["*"])
class ObservatoryController(private val service: ObservatoryService) {

    @PostMapping("/")
    fun addObservatory(@RequestBody observatory: CreateObservatoryRequest): ObservatoryEntity {
        return (service.create(observatory.observatoryName, observatory.organizationId))
    }

    @GetMapping("/")
    fun listObservatories(): List<ObservatoryEntity> {
        return service.listObservatories()
    }

    data class CreateObservatoryRequest(
        val observatoryName: String,
        val organizationId: UUID
    )
}
