package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/organizations/")
@CrossOrigin(origins = ["*"])
class OrganizationController(private val service: OrganizationService) {

    @PostMapping("/")
    fun addOrganization(@RequestBody organization: CreateOrganizationRequest): OrganizationEntity {
        return (service.create(organization.organizationName, organization.organizationLogo))
    }

    data class CreateOrganizationRequest(
        val organizationName: String,
        val organizationLogo: String
    )
}
