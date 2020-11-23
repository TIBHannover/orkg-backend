package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import java.io.File
import java.util.Base64
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/organizations/")
class OrganizationController(
    private val service: OrganizationService,
    private val observatoryService: ObservatoryService,
    private val contributorService: ContributorService
) {
    @Value("\${orkg.storage.images.dir}")
    var imageStoragePath: String? = null

    @PostMapping("/")
    fun addOrganization(@RequestBody organization: CreateOrganizationRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        return if (!isValidLogo(organization.organizationLogo)) {
            ResponseEntity.badRequest().body(
                    ErrorMessage(message = "Please upload a valid image"))
        } else {
            val response = (service.create(organization.organizationName, organization.createdBy, organization.url))
            decoder(organization.organizationLogo, response.id)
            val location = uriComponentsBuilder
                .path("api/organizations/{id}")
                .buildAndExpand(response.id)
                .toUri()
            ResponseEntity.created(location).body(service.findById(response.id!!).get())
        }
    }
    @GetMapping("/")
    fun findOrganizations(): List<Organization> {
        val response = service.listOrganizations()
        response.forEach {
            it.logo = encoder(it.id.toString())
        }
        return response
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: OrganizationId): Organization {
        val response = service
            .findById(id)
            .orElseThrow { OrganizationNotFound(id) }
        val logo = encoder(response.id.toString())

        return response.copy(logo = logo)
    }

    @GetMapping("{id}/observatories")
    fun findObservatoriesByOrganization(@PathVariable id: OrganizationId): List<Observatory> {
        return observatoryService.findObservatoriesByOrganizationId(id)
    }

    @GetMapping("{id}/users")
    fun findUsersByOrganizationId(@PathVariable id: OrganizationId): Iterable<Contributor> =
        contributorService.findUsersByOrganizationId(id)

    @RequestMapping("{id}/name", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateOrganizationName(@PathVariable id: OrganizationId, @RequestBody @Valid name: UpdateRequest): Organization {
        val response = findOrganization(id)
        response.name = name.value

        val updatedOrganization = service.updateOrganization(response)
        updatedOrganization.logo = encoder(response.id.toString())
        return updatedOrganization
    }

    @RequestMapping("{id}/url", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateOrganizationUrl(@PathVariable id: OrganizationId, @RequestBody @Valid url: UpdateRequest): Organization {
        val response = findOrganization(id)
        response.homepage = url.value

        val updatedOrganization = service.updateOrganization(response)
        updatedOrganization.logo = encoder(updatedOrganization.id.toString())
        return updatedOrganization
    }

    @RequestMapping("{id}/logo", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateOrganizationLogo(@PathVariable id: OrganizationId, @RequestBody @Valid submittedLogo: UpdateRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        val response = findOrganization(id)
        val logo = submittedLogo.value
        return if (!isValidLogo(logo)) {
            ResponseEntity.badRequest().body(
                ErrorMessage(message = "Please upload a valid image")
            )
        } else {
            decoder(logo, response.id)
            response.logo = logo

            val location = uriComponentsBuilder
                .path("api/organizations/{id}")
                .buildAndExpand(response.id)
                .toUri()
            ResponseEntity.created(location).body(response)
        }
    }

    fun findOrganization(id: OrganizationId): Organization {
        return service
            .findById(id)
            .orElseThrow { OrganizationNotFound(id) }
    }

    fun decoder(base64Str: String, name: OrganizationId?) {
        val (mimeType, encodedString) = base64Str.split(",")
        val (extension, _) = (mimeType.substring(mimeType
                                .indexOf("/") + 1))
                                .split(";")

        writeImage(encodedString, extension, name)
    }

        fun writeImage(image: String, imageExtension: String, name: OrganizationId?) {
            if (!File(imageStoragePath).isDirectory)
                File(imageStoragePath).mkdir()
            // check if logo already exist then delete it
            File(imageStoragePath).walk().forEach {
                if (it.name.substringBeforeLast(".") == name.toString()) {
                    it.delete()
                }
            }
            val imagePath = "$imageStoragePath/$name.$imageExtension"
            val imageByteArray = Base64.getDecoder().decode(image)
            File(imagePath).writeBytes(imageByteArray)
    }

    fun encoder(id: String): String {
        var file = ""
        var ext = ""
        val path = "$imageStoragePath/"
        File(path).walk().forEach {
            if (it.name.substringBeforeLast(".") == id) {
                ext = it.name.substringAfterLast(".")
                ext = "data:image/$ext;base64"
                file = it.toString()
            }
        }
        return readImage(file, ext)
    }
        fun readImage(file: String, imageExtension: String): String {
        if (file != "") {
            val bytes = File(file).readBytes()
            val base64 = Base64.getEncoder().encodeToString(bytes)
            return "$imageExtension,$base64"
        } else
            return ""
    }

    fun isValidLogo(logo: String): Boolean {
        val (mimeType, _) = logo.split(",")
        return mimeType.contains("image/")
    }

    data class CreateOrganizationRequest(
        val organizationName: String,
        var organizationLogo: String,
        val createdBy: ContributorId,
        val url: String
    )

    data class ErrorMessage(
        val message: String
    )

    data class UpdateRequest(
        @field:NotBlank
        @field:Size(min = 1)
        val value: String
    )
}
