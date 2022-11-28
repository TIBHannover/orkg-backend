package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationType
import java.io.File
import java.util.Base64
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
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
import java.time.LocalDate

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
    fun addOrganization(
        @RequestBody @Valid organization: CreateOrganizationRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        if (!isValidLogo(organization.organizationLogo)) {
            return ResponseEntity.badRequest().body(
                ErrorMessage(message = "Please upload a valid image")
            )
        } else {
            return if (service.findByName(organization.organizationName).isEmpty && service.findByDisplayId(organization.displayId).isEmpty) {
                val response = (service.create(
                    organization.organizationName,
                    organization.createdBy,
                    organization.url,
                    organization.displayId,
                    OrganizationType.fromOrNull(organization.type)!!
                ))
                decoder(organization.organizationLogo, response.id)
                val location = uriComponentsBuilder
                    .path("api/organizations/{id}")
                    .buildAndExpand(response.id)
                    .toUri()
                ResponseEntity.created(location).body(service.findById(response.id!!).get())
            } else {
                ResponseEntity.badRequest().body(
                    ErrorMessage(message = "Organization with same name or URL already exist")
                )
            }
        }
    }

    @PostMapping("/conference")
    fun addConference(
        @RequestBody @Valid organization: CreateOrganizationRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        val orgType = OrganizationType.fromOrNull(organization.type)!!
        if (orgType != OrganizationType.CONFERENCE) {
            return conferenceTypeError()
        } else if (organization.metadata?.date == null || organization.metadata.isDoubleBlind == null) {
            return ResponseEntity.badRequest().body(
                ErrorMessage(message = "Conference metadata is missing")
            )
        } else if (!isValidLogo(organization.organizationLogo)) {
            return ResponseEntity.badRequest().body(
                ErrorMessage(message = "Please upload a valid image")
            )
        } else {
            return if (service.findByName(organization.organizationName).isEmpty && service.findByDisplayId(organization.displayId).isEmpty) {
                val response = (service.createConference(
                    organization.organizationName,
                    organization.createdBy,
                    organization.url,
                    organization.displayId,
                    OrganizationType.fromOrNull(organization.type)!!,
                    organization.metadata
                ))
                decoder(organization.organizationLogo, response.id)
                val location = uriComponentsBuilder
                    .path("api/organizations/{id}")
                    .buildAndExpand(response.id)
                    .toUri()
                ResponseEntity.created(location).body(service.findById(response.id!!).get())
            } else {
                ResponseEntity.badRequest().body(
                    ErrorMessage(message = "Organization with same name or URL already exist")
                )
            }
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
    fun findById(@PathVariable id: String): Organization {
        val response: Organization = if (isValidUUID(id)) {
            service
                .findById(OrganizationId(id))
                .orElseThrow { OrganizationNotFound(id) }
        } else {
            service
                .findByDisplayId(id)
                .orElseThrow { OrganizationNotFound(id) }
        }
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

    @GetMapping("/conferences")
    fun findConferences(): Iterable<Organization> {
        val response = service.listConferences()
        response.forEach {
            it.logo = encoder(it.id.toString())
        }
        return response
    }

    @RequestMapping("{id}/name", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateOrganizationName(
        @PathVariable id: OrganizationId,
        @RequestBody @Valid name: UpdateRequest
    ): Organization {
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

    @RequestMapping("{id}/type", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateOrganizationType(@PathVariable id: OrganizationId, @RequestBody @Valid type: UpdateRequest): Organization {
        val response = findOrganization(id)
        response.type = OrganizationType.fromOrNull(type.value)!!

        val updatedOrganization = service.updateOrganization(response)
        updatedOrganization.logo = encoder(updatedOrganization.id.toString())
        return updatedOrganization
    }

    @RequestMapping("{id}/date", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateConferenceDate(@PathVariable id: OrganizationId, @RequestBody @Valid date: UpdateRequest): ResponseEntity<Any> {
        val response = findOrganization(id)
        return if (response.type == OrganizationType.CONFERENCE) {
            response.metadata?.date = LocalDate.parse(date.value)
            val updatedOrganization = service.updateOrganization(response)
            updatedOrganization.logo = encoder(updatedOrganization.id.toString())
            ResponseEntity.ok(updatedOrganization)
        } else {
            conferenceTypeError()
        }
    }

    @RequestMapping("{id}/process", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateConferenceProcess(@PathVariable id: OrganizationId, @RequestBody @Valid date: UpdateRequest): ResponseEntity<Any> {
        val response = findOrganization(id)
        return if (response.type == OrganizationType.CONFERENCE) {
            response.metadata?.isDoubleBlind = date.value.toBoolean()
            val updatedOrganization = service.updateOrganization(response)
            updatedOrganization.logo = encoder(updatedOrganization.id.toString())
            ResponseEntity.ok(updatedOrganization)
        } else {
            conferenceTypeError()
        }
    }

    @RequestMapping("{id}/logo", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateOrganizationLogo(
        @PathVariable id: OrganizationId,
        @RequestBody @Valid submittedLogo: UpdateRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
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
        val (extension, _) = (mimeType.substring(mimeType.indexOf("/") + 1)).split(";")

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

    fun isValidUUID(id: String): Boolean {
        return try {
            UUID.fromString(id) != null
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun conferenceTypeError(): ResponseEntity<Any> {
        return ResponseEntity.badRequest().body(
            ErrorMessage(message = "Must be a conference type")
        )
    }

    data class CreateOrganizationRequest(
        @JsonProperty("organization_name")
        val organizationName: String,
        @JsonProperty("organization_logo")
        var organizationLogo: String,
        @JsonProperty("created_by")
        val createdBy: ContributorId,
        val url: String,
        @field:Pattern(
            regexp = "^[a-zA-Z0-9_]+\$",
            message = "Only underscores ( _ ), numbers, and letters are allowed in the permalink field"
        )
        @field:NotBlank
        @JsonProperty("display_id")
        val displayId: String,
        @field:NotBlank
        val type: String,
        val metadata: Metadata?
    )

    data class ErrorMessage(
        val message: String
    )

    data class UpdateRequest(
        @field:NotBlank
        @field:Size(min = 1)
        val value: String
    )

    data class Metadata(
        val date: LocalDate?,
        @JsonProperty("is_double_blind")
        val isDoubleBlind: Boolean?
    )
}
