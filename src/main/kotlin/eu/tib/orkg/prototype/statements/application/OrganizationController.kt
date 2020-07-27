package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.rest.UserController
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import java.io.File
import java.util.Base64
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
        // return if (!mimeType.contains("image/")) {
        return if (!isValidLogo(organization.organizationLogo)) {
            ResponseEntity.badRequest().body(
                    ErrorMessage(message = "Please upload a valid image"))
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
        var response = service.listOrganizations()
        response.forEach {
            it.logo = encoder(it.id.toString())
        }
        return response
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): Organization {
        var response = service
            .findById(id)
            .orElseThrow { OrganizationNotFound() }
        var logo = encoder(response.id.toString())

        return (
                Organization(
                    id = response.id,
                    name = response.name,
                    logo = logo,
                    createdBy = response.createdBy,
                    url = response.url,
                    observatories = response.observatories
                )
            )
    }

    @GetMapping("{id}/observatories")
    fun findObservatoriesByOrganization(@PathVariable id: UUID): List<Observatory> {
        return observatoryService.findObservatoriesByOrganizationId(id)
    }

    @GetMapping("{id}/users")
    fun findUsersByOrganizationId(@PathVariable id: UUID): Iterable<UserController.UserDetails> {
        return userService.findUsersByOrganizationId(id)
            .map(UserController::UserDetails)
    }

    @PutMapping("/updateName")
    fun updateOrganizationName(@RequestBody organization: UpdateOrganizationNameRequest): Organization {
        var response = findOrganization(organization.id)
        response.name = organization.organizationName

        var updatedOrganization = service.updateOrganization(response)
        updatedOrganization.logo = encoder(response.id.toString())
        return updatedOrganization
    }

    @PutMapping("/updateUrl")
    fun updateOrganizationUrl(@RequestBody organization: UpdateOrganizationUrlRequest): Organization {
        var response = findOrganization(organization.id)
        response.url = organization.url

        var updatedOrganization = service.updateOrganization(response)
        updatedOrganization.logo = encoder(updatedOrganization.id.toString())
        return updatedOrganization
    }

    @PutMapping("/updateLogo")
    fun updateOrganizationLogo(@RequestBody organization: UpdateOrganizationLogoRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        var response = findOrganization(organization.id).toOrganization()
        return if (!isValidLogo(organization.organizationLogo)) {
            ResponseEntity.badRequest().body(
                ErrorMessage(message = "Please upload a valid image")
            )
        } else {
            decoder(organization.organizationLogo, response.id)
            response.logo = organization.organizationLogo

            val location = uriComponentsBuilder
                .path("api/organizations/{id}")
                .buildAndExpand(response.id)
                .toUri()
            ResponseEntity.created(location).body(response)
        }
    }

    fun findOrganization(id: UUID): OrganizationEntity {
        return service
            .findById(id)
            .orElseThrow { OrganizationNotFound() }
    }

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

    fun encoder(id: String): String {
        var file: String = ""
        var ext: String = ""
        var path: String = "$imageStoragePath/"
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
        // !mimeType.contains("image/")
    }

    data class CreateOrganizationRequest(
        val organizationName: String,
        var organizationLogo: String,
        val createdBy: UUID,
        val url: String
    )

    data class UpdateOrganizationNameRequest(
        val id: UUID,
        val organizationName: String
    )

    data class UpdateOrganizationLogoRequest(
        val id: UUID,
        var organizationLogo: String
    )

    data class UpdateOrganizationUrlRequest(
        val id: UUID,
        val url: String?
    )

    data class ErrorMessage(
        val message: String
    )
}
