package eu.tib.orkg.prototype.statements.application

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
    private val observatoryService: ObservatoryService,

    @Value("\${directory.path}")
    val directoryPath: String
) {

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
        var path: String = "$directoryPath/"
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
        val organizationId: UUID,
        val organizationName: String?,
        val organizationLogo: String?,
        val createdBy: UUID?
    )

    fun decoder(base64Str: String, name: UUID?) {
        val strings: List<String> = base64Str.split(",")
        var extension: String = ""

        when {
            strings[0] == "data:image/jpeg;base64" -> extension = "jpeg"
            strings[0] == "data:image/png;base64" -> extension = "png"
            strings[0] == "data:image/jpg;base64" -> extension = "jpg"
        }

        writeImage(strings[1], extension, name)
    }

        fun writeImage(image: String, imageExtension: String, name: UUID?) {
            if (!File(directoryPath).isDirectory)
                File(directoryPath).mkdir()
            var imagePath: String = "$directoryPath/$name.$imageExtension"
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
                when (ext) {
                    "jpeg" -> ext = "data:image/jpeg;base64"
                    "png" -> ext = "data:image/png;base64"
                    "jpg" -> ext = "data:image/jpg;base64"
                }

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
