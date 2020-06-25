package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.rest.UserController
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import java.io.File
import java.util.Base64
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/organizations/")
@CrossOrigin(origins = ["*"])
class OrganizationController(
    private val service: OrganizationService,
    private val userService: UserService,
    private val observatoryService: ObservatoryService
) {
    @Value("\${orkg.storage.images.dir}")
    var imageStoragePath: String? = null

    @PostMapping("/")
    fun addOrganization(@RequestBody organization: CreateOrganizationRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        val (mimeType, _) = organization.organizationLogo.split(",")
        return if (!mimeType.contains("image/")) {
            ResponseEntity.badRequest().body("Please upload valid image")
        } else {
            var response = (service.create(organization.organizationName, organization.createdBy, organization.url))
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
        return service.listOrganizations()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): Organization {
        var response = service.findById(id)
        var path: String = "$imageStoragePath/"
        var logo = encoder(path, response.get().id.toString())

        return (
            Organization(
                id = response.get().id,
                name = response.get().name,
                logo = logo,
                createdBy = response.get().createdBy,
                url = response.get().url,
                observatories = response.get().observatories
            )
        )
    }

    @GetMapping("{id}/observatories")
    fun findObservatoriesByOrganization(@PathVariable id: UUID): List<ObservatoryEntity> {
        return observatoryService.findObservatoriesByOrganizationId(id)
    }

    @GetMapping("{id}/users")
    fun findUsersByOrganizationId(@PathVariable id: UUID): Iterable<UserController.UserDetails> {
        return userService.findUsersByOrganizationId(id)
            .map(UserController::UserDetails)
    }

    data class CreateOrganizationRequest(
        val organizationName: String,
        var organizationLogo: String,
        val createdBy: UUID,
        val url: String
    )

    data class UpdateOrganizationResponse(
        @JsonProperty("organization_id")
        val organizationId: UUID,
        @JsonProperty("organization_name")
        val organizationName: String?,
        @JsonProperty("organization_logo")
        val organizationLogo: String?,
        @JsonProperty("created_by")
        val createdBy: UUID?
    )

    fun decoder(base64Str: String, name: UUID?) {
        val (mimeType, encodedString) = base64Str.split(",")
        val (extension, _) = (mimeType.substring(mimeType
                                .indexOf("/") + 1))
                                .split(";")

        writeImage(encodedString, extension, name)
    }

        fun writeImage(image: String, imageExtension: String, name: UUID?) {
            if (!File(imageStoragePath).isDirectory)
                File(imageStoragePath).mkdir()
            var imagePath: String = "$imageStoragePath/$name.$imageExtension"
            var base64Image = image
            val imageByteArray = Base64.getDecoder().decode(base64Image)
            File(imagePath).writeBytes(imageByteArray)
    }

    fun encoder(filePath: String, id: String): String {
        var file: String = ""
        var ext: String = ""
        File(filePath).walk().forEach {
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
}
