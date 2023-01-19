package eu.tib.orkg.prototype.community.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.community.api.OrganizationUseCases
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.Organization
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.OrganizationType
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.files.api.CreateImageUseCase
import eu.tib.orkg.prototype.files.api.ImageUseCases
import eu.tib.orkg.prototype.files.application.InvalidImageData
import eu.tib.orkg.prototype.files.application.InvalidMimeType
import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageData
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.application.BaseController
import java.io.ByteArrayInputStream
import java.util.*
import javax.activation.MimeType
import javax.activation.MimeTypeParseException
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import org.apache.commons.io.IOUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

private val encodedImagePattern = Regex("""^data:(.*);base64,([A-Za-z0-9+/]+=*)$""").toPattern()

@RestController
@RequestMapping("/api/organizations/")
class OrganizationController(
    private val service: OrganizationUseCases,
    private val observatoryService: ObservatoryUseCases,
    private val contributorService: ContributorService,
    private val imageService: ImageUseCases,
    private val resourceService: ResourceUseCases
) : BaseController() {
    @PostMapping("/")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun addOrganization(
        @RequestBody @Valid organization: CreateOrganizationRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        val decodedLogo = organization.organizationLogo.decodeBase64()
        if (service.findByName(organization.organizationName).isPresent) {
            throw OrganizationAlreadyExists.withName(organization.organizationName)
        } else if (service.findByDisplayId(organization.displayId).isPresent) {
            throw OrganizationAlreadyExists.withDisplayId(organization.displayId)
        }
        val response = service.create(
            organization.organizationName,
            organization.createdBy,
            organization.url,
            organization.displayId,
            OrganizationType.fromOrNull(organization.type)!!
        )
        imageService.create(CreateImageUseCase.CreateCommand(decodedLogo.data, decodedLogo.mimeType, organization.createdBy))
        val location = uriComponentsBuilder
            .path("api/organizations/{id}")
            .buildAndExpand(response.id)
            .toUri()
        return ResponseEntity.created(location).body(service.findById(response.id!!).get())
    }

    @GetMapping("/")
    fun findOrganizations(): List<Organization> {
        return service.listOrganizations().onEach { expandLogo(it) }
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): Organization {
        val response: Organization = if (id.isValidUUID(id)) {
            service
                .findById(OrganizationId(id))
                .orElseThrow { OrganizationNotFound(id) }
        } else {
            service
                .findByDisplayId(id)
                .orElseThrow { OrganizationNotFound(id) }
        }
        expandLogo(response)
        return response
    }

    @GetMapping("{id}/observatories")
    fun findObservatoriesByOrganization(@PathVariable id: OrganizationId): List<Observatory> {
        return observatoryService.findObservatoriesByOrganizationId(id)
    }

    @GetMapping("{id}/users")
    fun findUsersByOrganizationId(@PathVariable id: OrganizationId): Iterable<Contributor> =
        contributorService.findUsersByOrganizationId(id)

    @GetMapping("/conferences")
    fun findOrganizationsConferences(): Iterable<Organization> {
        return service.listConferences().onEach { expandLogo(it) }
    }

    @GetMapping("{id}/comparisons")
    fun findComparisonsByOrganizationId(@PathVariable id: OrganizationId, pageable: Pageable): Page<ResourceRepresentation> {
        return resourceService.findComparisonsByOrganizationId(id, pageable)
    }
    @GetMapping("{id}/problems")
    fun findProblemsByOrganizationId(@PathVariable id: OrganizationId, pageable: Pageable): Page<ResourceRepresentation> {
        return resourceService.findProblemsByOrganizationId(id, pageable)
    }

    @RequestMapping("{id}/name", method = [RequestMethod.POST, RequestMethod.PUT])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateOrganizationName(
        @PathVariable id: OrganizationId,
        @RequestBody @Valid name: UpdateRequest,
    ): ResponseEntity<Any> {
        val response = findOrganization(id)
        response.name = name.value
        service.updateOrganization(response)
        return ok().body(response)
    }

    @RequestMapping("{id}/url", method = [RequestMethod.POST, RequestMethod.PUT])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateOrganizationUrl(
        @PathVariable id: OrganizationId,
        @RequestBody @Valid url: UpdateRequest
    ): ResponseEntity<Any> {
        val response = findOrganization(id)
        response.homepage = url.value
        service.updateOrganization(response)
        return ok().body(response)
    }

    @RequestMapping("{id}/type", method = [RequestMethod.POST, RequestMethod.PUT])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateOrganizationType(
        @PathVariable id: OrganizationId,
        @RequestBody @Valid type: UpdateRequest
    ): ResponseEntity<Any> {
        val response = findOrganization(id)
        response.type = OrganizationType.fromOrNull(type.value)!!
        service.updateOrganization(response)
        return ok().body(response)
    }

    @RequestMapping("{id}/logo", method = [RequestMethod.POST, RequestMethod.PUT])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateOrganizationLogo(
        @PathVariable id: OrganizationId,
        @RequestBody @Valid submittedLogo: EncodedImage,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        val userId = authenticatedUserId()
        val image = submittedLogo.decodeBase64()
        service.updateLogo(id, image.data, image.mimeType, ContributorId(userId))
        val location = uriComponentsBuilder
            .path("api/organizations/{id}/logo")
            .buildAndExpand(id)
            .toUri()
        return ResponseEntity.created(location).body(submittedLogo.value)
    }

    @GetMapping("{id}/logo")
    fun findOrganizationLogo(
        @PathVariable id: OrganizationId,
        response: HttpServletResponse
    ) {
        val logo = service.findLogo(id).orElseThrow { LogoNotFound(id) }
        response.contentType = logo.mimeType.toString()
        IOUtils.copy(ByteArrayInputStream(logo.data.bytes), response.outputStream)
    }

    fun findOrganization(id: OrganizationId): Organization {
        return service
            .findById(id)
            .orElseThrow { OrganizationNotFound(id) }
    }

    private fun expandLogo(organization: Organization) {
        if (organization.logoId != null) {
            service.findLogo(organization.id!!).ifPresent {
                organization.logo = it.encodeBase64()
            }
        }
    }

    fun String.isValidUUID(id: String): Boolean = try { UUID.fromString(id) != null } catch (e: IllegalArgumentException) { false }

    data class CreateOrganizationRequest(
        @JsonProperty("organization_name")
        val organizationName: String,
        @JsonProperty("organization_logo")
        var organizationLogo: EncodedImage,
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
        val type: String
    )

    data class UpdateRequest(
        @field:NotBlank
        @field:Size(min = 1)
        val value: String
    )

    data class RawImage(
        val data: ImageData,
        val mimeType: MimeType
    )
}

@JvmInline
value class EncodedImage(val value: String) {
    fun decodeBase64(): OrganizationController.RawImage {
        val matcher = encodedImagePattern.matcher(value)
        if (!matcher.matches()) {
            throw InvalidImageEncoding()
        }
        val mimeType = matcher.group(1).let {
            try {
                MimeType(it)
            } catch (e: MimeTypeParseException) {
                throw InvalidMimeType(it, e)
            }
        }
        val data = try {
            ImageData(Base64.getDecoder().decode(matcher.group(2)))
        } catch (e: IllegalArgumentException) {
            throw InvalidImageData()
        }
        return OrganizationController.RawImage(data, mimeType)
    }
}

fun Image.encodeBase64(): String {
    val encoded = Base64.getEncoder().encodeToString(data.bytes)
    return "data:$mimeType;base64,$encoded"
}
