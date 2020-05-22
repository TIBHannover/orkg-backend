package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import java.io.File
import java.util.Base64
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/organizations/")
@CrossOrigin(origins = ["*"])
class OrganizationController(
    private val service: OrganizationService,
    private val observatoryService: ObservatoryService
) {
    @Value("\${orkg.storage.images.dir}")
    var imageStoragePath: String? = null

    @PostMapping("/")
    fun addOrganization(@RequestBody organization: CreateOrganizationRequest): OrganizationEntity {
        var response = (service.create(organization.organizationName, organization.createdBy))
        decoder(organization.organizationLogo, response.id)
        return response
    }
    @GetMapping("/")
    fun findOrganizations(): List<OrganizationEntity> {
        return service.listOrganizations()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): UpdateOrganizationResponse {
        var response = service.findById(id)
        var path: String = "$imageStoragePath/"
        var logo = encoder(path, response.get().id.toString())

        return (
                OrganizationController.UpdateOrganizationResponse(
                organizationId = id,
                organizationName = response.get().name,
                organizationLogo = logo,
                    createdBy = response.get().createdBy
            )
        )
    }

    @GetMapping("{id}/observatories")
    fun findObservatoriesByOrganization(@PathVariable id: UUID): List<ObservatoryEntity> {
        return observatoryService.findObservatoriesByOrganizationId(id)
    }

    data class CreateOrganizationRequest(
        val organizationName: String,
        var organizationLogo: String,
        val createdBy: UUID
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
        //var a: String = "data:image/svg;base64"
        val (extension, data ) = (mimeType.substring(mimeType
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
