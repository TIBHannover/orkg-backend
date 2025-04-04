package org.orkg.community.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.apache.tomcat.util.http.fileupload.IOUtils
import org.orkg.common.ContributorId
import org.orkg.common.OrganizationId
import org.orkg.common.annotations.RequireCuratorRole
import org.orkg.common.contributorId
import org.orkg.community.adapter.input.rest.mapping.ObservatoryRepresentationAdapter
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.InvalidImageEncoding
import org.orkg.community.domain.LogoNotFound
import org.orkg.community.domain.Organization
import org.orkg.community.domain.OrganizationAlreadyExists
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.domain.OrganizationType
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.community.input.UpdateOrganizationUseCases
import org.orkg.community.output.OrganizationRepository
import org.orkg.graph.input.ResourceUseCases
import org.orkg.mediastorage.domain.ImageData
import org.orkg.mediastorage.domain.InvalidImageData
import org.orkg.mediastorage.domain.InvalidMimeType
import org.orkg.mediastorage.domain.RawImage
import org.orkg.mediastorage.input.CreateImageUseCase
import org.orkg.mediastorage.input.ImageUseCases
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.Authentication
import org.springframework.util.MimeType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.util.UriComponentsBuilder
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.UUID

private val encodedImagePattern = Regex("""^data:(.*);base64,([A-Za-z0-9+/]+=*)$""").toPattern()

@RestController
@RequestMapping("/api/organizations", produces = [MediaType.APPLICATION_JSON_VALUE])
class OrganizationController(
    private val service: OrganizationUseCases,
    private val observatoryService: ObservatoryUseCases,
    private val imageService: ImageUseCases,
    override val resourceRepository: ResourceUseCases,
    private val organizationRepository: OrganizationRepository,
) : ObservatoryRepresentationAdapter {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun addOrganization(
        @RequestBody @Valid organization: CreateOrganizationRequest,
        uriComponentsBuilder: UriComponentsBuilder,
    ): ResponseEntity<Any> {
        val decodedLogo = EncodedImage(organization.organizationLogo).decodeBase64()
        if (service.findByName(organization.organizationName).isPresent) {
            throw OrganizationAlreadyExists.withName(organization.organizationName)
        } else if (service.findByDisplayId(organization.displayId).isPresent) {
            throw OrganizationAlreadyExists.withDisplayId(organization.displayId)
        }
        val imageId = imageService.create(CreateImageUseCase.CreateCommand(decodedLogo.data, decodedLogo.mimeType, organization.createdBy))
        val id = service.create(
            id = null,
            organization.organizationName,
            organization.createdBy,
            organization.url,
            organization.displayId,
            OrganizationType.fromOrNull(organization.type)!!,
            imageId
        )
        val location = uriComponentsBuilder
            .path("/api/organizations/{id}")
            .buildAndExpand(id)
            .toUri()
        return ResponseEntity.created(location).body(service.findById(id).get())
    }

    @GetMapping
    fun findOrganizations(): List<Organization> = service.findAll()

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: String,
    ): Organization = if (id.isValidUUID(id)) {
        service
            .findById(OrganizationId(id))
            .orElseThrow { OrganizationNotFound(id) }
    } else {
        service
            .findByDisplayId(id)
            .orElseThrow { OrganizationNotFound(id) }
    }

    @GetMapping("/{id}/observatories")
    fun findObservatoriesByOrganization(
        @PathVariable id: OrganizationId,
    ): List<ObservatoryRepresentation> =
        observatoryService.findAllByOrganizationId(id, PageRequest.of(0, Int.MAX_VALUE))
            .mapToObservatoryRepresentation()
            .content

    @GetMapping("/{id}/users")
    fun findUsersByOrganizationId(
        @PathVariable id: OrganizationId,
    ): Iterable<Contributor> =
        organizationRepository.findAllMembersByOrganizationId(id, PageRequest.of(0, Int.MAX_VALUE)).content

    @GetMapping("/conferences")
    fun findOrganizationsConferences(): Iterable<Organization> = service.findAllConferences()

    @PatchMapping("{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @RequireCuratorRole
    fun updateOrganization(
        @PathVariable id: OrganizationId,
        @RequestPart("properties", required = false) @Valid request: UpdateOrganizationRequest?,
        @RequestPart("logo", required = false) logo: MultipartFile?,
        currentUser: Authentication?,
        uriComponentsBuilder: UriComponentsBuilder,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        service.update(
            contributorId,
            UpdateOrganizationUseCases.UpdateOrganizationCommand(
                id = id,
                name = request?.name,
                url = request?.url,
                type = request?.type,
                logo = logo?.let {
                    val bytes = logo.bytes
                    val mimeType = try {
                        MimeType.valueOf(logo.contentType!!)
                    } catch (e: Exception) {
                        throw InvalidMimeType(logo.contentType, e)
                    }
                    RawImage(
                        data = ImageData(bytes),
                        mimeType = mimeType
                    )
                }
            )
        )
        val location = uriComponentsBuilder
            .path("/api/organizations/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @RequestMapping("/{id}/name", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun updateOrganizationName(
        @PathVariable id: OrganizationId,
        @RequestBody @Valid name: UpdateRequest,
    ): ResponseEntity<Any> {
        val response = findOrganization(id)
        response.name = name.value
        service.update(response)
        return ok().body(response)
    }

    @RequestMapping("/{id}/url", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun updateOrganizationUrl(
        @PathVariable id: OrganizationId,
        @RequestBody @Valid url: UpdateRequest,
    ): ResponseEntity<Any> {
        val response = findOrganization(id)
        response.homepage = url.value
        service.update(response)
        return ok().body(response)
    }

    @RequestMapping("/{id}/type", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun updateOrganizationType(
        @PathVariable id: OrganizationId,
        @RequestBody @Valid type: UpdateRequest,
    ): ResponseEntity<Any> {
        val response = findOrganization(id)
        response.type = OrganizationType.fromOrNull(type.value)!!
        service.update(response)
        return ok().body(response)
    }

    @RequestMapping("/{id}/logo", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun updateOrganizationLogo(
        @PathVariable id: OrganizationId,
        @RequestBody @Valid submittedLogo: UpdateRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val organization = service.findById(id).orElseThrow { OrganizationNotFound(id) }
        val image = EncodedImage(submittedLogo.value).decodeBase64()
        service.updateLogo(id, image, currentUser.contributorId())
        val location = uriComponentsBuilder
            .path("/api/organizations/{id}/logo")
            .buildAndExpand(id)
            .toUri()
        return ResponseEntity.created(location).body(organization)
    }

    @GetMapping("/{id}/logo")
    fun findOrganizationLogo(
        @PathVariable id: OrganizationId,
        response: HttpServletResponse,
    ) {
        val logo = service.findLogoById(id).orElseThrow { LogoNotFound(id) }
        response.contentType = logo.mimeType.toString()
        IOUtils.copy(ByteArrayInputStream(logo.data.bytes), response.outputStream)
    }

    fun findOrganization(id: OrganizationId): Organization = service
        .findById(id)
        .orElseThrow { OrganizationNotFound(id) }

    fun String.isValidUUID(id: String): Boolean = try {
        UUID.fromString(id) != null
    } catch (e: IllegalArgumentException) {
        false
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
    )

    data class UpdateRequest(
        @field:NotBlank
        val value: String,
    )

    data class UpdateOrganizationRequest(
        @field:Size(min = 1)
        val name: String?,
        @field:Size(min = 1)
        val url: String?,
        val type: OrganizationType?,
    )
}

@JvmInline
value class EncodedImage(val value: String) {
    fun decodeBase64(): RawImage {
        val matcher = encodedImagePattern.matcher(value)
        if (!matcher.matches()) {
            throw InvalidImageEncoding()
        }
        val mimeType = matcher.group(1).let {
            try {
                MimeType.valueOf(it)
            } catch (e: Exception) {
                throw InvalidMimeType(it, e)
            }
        }
        val data = try {
            ImageData(Base64.getDecoder().decode(matcher.group(2)))
        } catch (e: IllegalArgumentException) {
            throw InvalidImageData()
        }
        return RawImage(data, mimeType)
    }
}
